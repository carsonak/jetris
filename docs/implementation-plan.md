# Spawn recovery implementation plan

## Contract and QA work

- [x] Record the agreed [gameplay rules](game-rules.md) and link project instructions.
- [x] Update API documentation without changing signatures or production behavior.
- [x] Replace immediate block-out expectations and add deterministic recovery,
  drop-safety, kick, and downward-escape regression tests.

Tests express target behavior and may remain red while the human implements the
model. Existing failures must not be removed merely to obtain a green build.

## Human implementation: model

- [ ] Complete the existing board implementation, preserving headless Java and
  injected randomness. Address existing baseline failures independently of new rules.
- [ ] Spawn at the documented top-center position without immediate game over.
- [ ] Validate complete movement/rotation destinations; keep ordered kicks and
  unchanged state on rejection. No swept-path collision algorithm is required.
- [ ] Check current overlap before gravity movement. End the game without locking
  or consuming the preview when overlap remains; otherwise move or lock normally.
- [ ] Ignore both drops during overlap; resume normal drop behavior after recovery.
- [ ] Preserve input lockout after game over and clear recovery state on reset.
- [ ] Run model tests and audit internal invariants. The isolated whitebox fixture
  deliberately seeds a top-row obstruction with clear space below; adapt fixture
  access if the private grid representation changes, retaining the behavior assertion.

## Human implementation: view and scheduling

`GameScreen` and `InputController` are currently empty. No clock implementation or
new public scheduling API is introduced by this documentation/test change.

- [ ] Implement ordinary movement/rotation input and gravity scheduling in the view.
- [ ] Provide reliable spawn detection for ticks, drops, and reset; comparing only
  piece values is insufficient when consecutive pieces are identical.
- [ ] Reset elapsed gravity time on every spawn and discard previous-piece frame
  remainder. Apply the current gravity interval to the newly spawned piece.
- [ ] Do not reset elapsed time for successful recovery or ignored drop inputs.
- [ ] Add scheduler tests with a controllable clock: just before/at the interval,
  spawn partway through an interval, identical consecutive pieces, soft/hard-drop
  spawns, reset, and a frame long enough for multiple gravity steps. A new piece
  must never receive a tick from its predecessor's accumulated time.
- [ ] Verify faster configured gravity gives a shorter full recovery interval;
  progression rules themselves remain deferred.

## Verification and delivery

Run `./gradlew core:test`, `./gradlew spotlessApply`, and `./gradlew check`.
Inspect formatter changes to avoid including unrelated work. Report model test
failures separately from formatting, dependency, or environment failures. Commit
documentation/contracts and tests separately; exclude the local discussion changes
and unrelated existing edits, then push to `origin/main`.
