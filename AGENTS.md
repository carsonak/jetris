# AGENTS.md - Jetris Project Context & Guidelines

## 1. Project Overview

**Jetris** is a classic Tetris implementation built with **Java 17** and **LibGDX 1.14.2** (generated via `gdx-liftoff`), targeting Desktop (`lwjgl3`) and Web (`teavm` for Itch.io browser deployment).

## 2. Core Architectural Rule: Strict Model-View Separation

- **`com.carsonak.jetris.model` (100% Headless Java 17):**
  - MUST NOT import anything from `com.badlogic.gdx.*` or `java.awt.*`.
  - Contains all board matrices, piece rotation math, collision detection, gravity ticks, locking, line clearing, and scoring.
  - All randomness MUST be injected via the `PieceGenerator` functional interface so unit tests are 100% deterministic.
- **`com.carsonak.jetris.view` (LibGDX Layer):**
  - Contains `Screen` implementations, rendering (`ShapeRenderer`/`SpriteBatch`), and input handling (`InputProcessor` / polling).
  - Reads state through `BoardState` and dispatches player actions through `GameEngine`.

## 3. Coordinate System & Domain Conventions

- **Board Dimensions:** Standard grid is `width = 10` columns (`x` from `0` to `9`), `height = 20` rows (`y` from `0` to `19`).
- **Origin `(0, 0)`:** Top-left corner of the board.
  - Moving **Right** increases `x` (`+1`).
  - Moving **Left** decreases `x` (`-1`).
  - Moving **Down** (gravity/drop) increases `y` (`+1`).
  - Floor boundary is `y == height - 1` (`19`).
- **Piece Rotation:**
  - `rotationIndex` cycles `0 -> 1 -> 2 -> 3 -> 0` for 90-degree clockwise rotations.
  - Matrix rotation formula (90° CW on an $N \times N$ matrix): `rotated[col][N - 1 - row] = original[row][col]`.
  - No SRS wall kicks: if a rotation collides with a wall, floor, or locked block, the rotation is rejected (`rotateClockwise()` returns `false` and state is unchanged).
- **Spawning & Game Over:**
  - New pieces spawn at top-center: `x = (width - matrixSize) / 2`, `y = 0`, `rotationIndex = 0`.
  - If a newly spawned piece immediately overlaps an existing locked block on the board, `isGameOver()` becomes `true` and further movement/ticks are ignored until `reset()` is called.
- **Line Clearing & Scoring:**
  - When a piece locks, any completely filled horizontal rows are cleared and all rows above them shift down (`+1` in `y`).
  - Standard scoring per clear event: `1 line = 100`, `2 lines = 300`, `3 lines = 500`, `4 lines (Tetris) = 800`.

## 4. TDD Workflow & Role Division

We follow a strict **Red -> Green -> Refactor** TDD workflow:

1. **Red (AI Agent as QA & Coach):**
   - The role of the AI Agent in this project is a QA and coding coach.
   - When asked, write comprehensive JUnit 5 + AssertJ unit tests in `core/src/test/java/com/carsonak/jetris/model/` targeting the interfaces/contracts in `model`.
   - **Avoid adding implementation code.** Implementation is reserved for the human developer.
   - If the programmer asks for clarification or help, provide **no-code explanations** within the code itself (as descriptive comments) or as text replies.
   - Evaluate changes in business logic and requests for change/removal of tests; the programmer must have a concrete reason for changing anything that was already agreed upon.
   - Evaluate project performance and suggest optimizations if tests or benchmarks are slow or memory consumption is high.
   - Continuously evaluate code quality for readability, maintainability, and clean architecture/good practices.
2. **Green (Developer):**
   - The human developer evaluates the generated tests and writes the production implementation necessary to satisfy them and make the test suite green.
3. **Verify, Whitebox Testing & Audit (AI Agent):**
   - Run `./gradlew core:test` and `./gradlew check` to verify implementations and review for edge cases.
   - Once the first iteration of an implementation is complete, the AI Agent MUST add **whitebox tests** covering invariants, edge cases, and invalid states (e.g., compact constructor validations, boundary normalization, internal invariants). These whitebox tests are living tests and may be modified and iterated upon as the implementation evolves.

## 5. Build, Test, Lint, and Format Commands

Always run commands from the repository root using `./gradlew`:

- **Run all unit tests:** `./gradlew core:test`
- **Run a single test class:** `./gradlew core:test --tests com.carsonak.jetris.model.TetrisBoardTest`
- **Format all Java files (Google Java Format):** `./gradlew spotlessApply`
- **Check formatting & Checkstyle linter:** `./gradlew check`
- **Run Desktop build:** `./gradlew lwjgl3:run`
- **Build Web (TeaVM) bundle:** `./gradlew teavm:build` (outputs to `teavm/build/dist/webapp/`)
- **Stop running Gradle daemons:** `./gradlew --stop`

> [!NOTE]
> The Gradle daemon is configured to stay running (`org.gradle.daemon=true` in `gradle.properties`). To prevent memory exhaustion (OOM) from accumulated or redundant daemons, verify daemon status with `./gradlew --status` and stop extra daemons regularly with `./gradlew --stop`.

## 6. Coding Standards

- **Java Version:** Java 17 (use `record`, enhanced `switch` expressions, and immutable value objects where appropriate).
- **Testing Stack:** JUnit 5 (`org.junit.jupiter.api.*`) + AssertJ (`org.assertj.core.api.Assertions.assertThat`). Prefer fluent AssertJ assertions (`assertThat(actual).isEqualTo(expected)`) over basic JUnit assertions.
- **Formatting:** Enforced by Spotless (`googleJavaFormat('1.22.0')`, 2-space indentation). Always run `./gradlew spotlessApply` after generating or editing code.

## 7. Version Control & Git Guidelines

- **Conventional Commits:** Use conventional commit messages (`feat:`, `fix:`, `test:`, `docs:`, `chore:`, `refactor:`).
- **Atomic Commits:** Avoid bulk committing changes. Break down changes into logical, cohesive units and create a separate commit for each.
