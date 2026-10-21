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
- **Piece Representation, Rotation & Wall Kicks:**
  - **`Tetromino` Encapsulation:** `Tetromino` does NOT expose an `int[][]` matrix. Instead, it exposes `List<Point> getBaseCells()` (the 4 local block coordinates at `rotationIndex = 0`, flushed to the top-left origin so `minX == 0` and `minY == 0`), along with `getWidth()` and `getHeight()`.
  - **`ActivePiece` Rotation & Top-Left Normalization:**
    - `rotationIndex` is normalized into `[0, 3]` via `Math.floorMod(rotationIndex, 4)`.
    - `rotateClockwise()` increments `rotationIndex` (`0 -> 1 -> 2 -> 3 -> 0`); `rotateAnticlockwise()` decrements `rotationIndex` (`0 -> 3 -> 2 -> 1 -> 0`).
    - To compute `getOccupiedCells()`, each base point `(x, y)` from `type.getBaseCells()` is rotated 90° clockwise `rotationIndex` times via `(rotX, rotY) -> (-rotY, rotX)`, normalized by subtracting `(minX, minY)` across the 4 rotated points so `minX == 0` and `minY == 0` in every orientation (preventing blank leading rows/columns and "invisible wall/floor" effects), and finally translated by adding `(this.x, this.y)`.
  - **Simple Wall/Floor Kick Offset Table:** When `rotateClockwise()` or `rotateAnticlockwise()` is called on `TetrisBoard`, test the rotated piece at candidate `(dx, dy)` offsets in this exact order:
    1. `(0, 0)` (no offset)
    2. `(-1, 0)` (1 cell left)
    3. `(+1, 0)` (1 cell right)
    4. `(-2, 0)` (2 cells left — e.g., `Tetromino.I` against right wall)
    5. `(+2, 0)` (2 cells right — e.g., `Tetromino.I` against left wall)
    6. `(0, -1)` (1 cell up — floor kick)
  - Pick the **first** offset where all occupied cells are within board bounds (`0 <= x < width`, `0 <= y < height`) and do not overlap any locked blocks (`rotateClockwise()` / `rotateAnticlockwise()` updates `activePiece` and returns `true`). If all 6 offsets collide, the rotation is rejected (`false` and state is unchanged).
- **Spawning & Game Over:**
  - New pieces spawn at top-center: `x = (width - type.getWidth()) / 2`, `y = 0`, `rotationIndex = 0`.
  - If a newly spawned piece immediately overlaps an existing locked block on the board, `isGameOver()` becomes `true` and further movement/ticks are ignored until `reset()` is called.
- **Line Clearing & Scoring:**
  - When a piece locks, any completely filled horizontal rows are cleared and all rows above them shift down (`+1` in `y`).
  - **4-Line Maximum Invariant & Fixed Scoring:** Because the tallest standard `Tetromino` (`I`) has a maximum height of 4 blocks, a single piece lock can clear at most 4 lines (`0 <= linesCleared <= 4`). Standard fixed scoring per clear event:
    - `1 line = 100`
    - `2 lines = 300`
    - `3 lines = 500`
    - `4 lines (Tetris) = 800`

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
