# Jetris gameplay rules

This is the authoritative gameplay contract. It describes the target behavior;
the model is still being implemented. See the [implementation plan](implementation-plan.md)
for remaining work. The model is headless Java 17 and receives randomness through
`PieceGenerator`.

## Board, pieces, and movement

The default board is 10 columns by 20 rows. Coordinates start at the top-left;
right increases `x`, down increases `y`. Pieces use four cached, normalized
orientations with no blank leading rows or columns. Spawning uses orientation 0
at `x = (width - type.getWidth()) / 2`, `y = 0`.

Horizontal input moves one column. Every accepted movement or rotation must end
entirely within the board and free of locked cells. A rejected action leaves the
piece and all other game state unchanged. An overlapping piece cannot take a
partially clear step in order to work its way out of an obstruction.

Both rotation directions try these offsets, relative to the current origin, in
this exact order:

1. `(0, 0)`
2. `(-1, 0)`
3. `(+1, 0)`
4. `(-2, 0)`
5. `(+2, 0)`
6. `(0, -1)`

The first valid destination wins. Each candidate is independent: a blocked earlier
candidate does not prevent trying later candidates. Collision checks use the final
occupied cells, without checking a swept rotation or translation path. This is a
custom kick system, not SRS. The final upward floor kick is allowed even though
there is no ordinary upward movement.

## Spawn recovery and game over

Spawn overlap does not immediately end the game. Before the new piece's first
gravity tick, ordinary left/right movement and rotation may rescue it, subject to
the destination rules above. There is no separate recovery timer or IRS input mode.

Each `tick()` evaluates in this order:

1. If already game over, do nothing.
2. If the current active piece overlaps locked cells, set game over without moving,
   locking, clearing rows, changing score/lines/preview, or consuming another piece.
3. Otherwise move down one row if the destination is valid.
4. If downward movement is blocked, lock the current piece, clear full rows, update
   score and line count, and spawn the next piece. Do not tick the new piece in the
   same call, even if its spawn overlaps.

Checking the current position first prevents downward escape through an obstruction.
Once game over, movement and rotation return `false`; ticks and drops do nothing.
`reset()` clears the grid, score, lines, and game-over flag and spawns a fresh piece.

## Drops and gravity timing

While the current piece overlaps locked cells, **both soft drop and hard drop are
no-ops**. They do not end the recovery window, consume pieces, or alter any state.
After recovery, soft drop moves down one row or immediately locks if blocked; hard
drop descends to the lowest reachable valid position and immediately locks. Neither
action may skip through locked cells or overwrite them.

Every spawn starts a full interval at the current gravity speed, including spawns
from ticks, drops, and reset. No leftover frame time from the previous piece may
tick the new piece early. Successful movement/rotation and ignored drop inputs do
not restart that interval. Faster gravity therefore shortens recovery time.

The model has discrete ticks and no wall clock. The view owns scheduling; its
implementation must detect each spawn, even when consecutive pieces have the same
type and position. The scheduling interface is deferred to that implementation.

## Lines, scoring, and deferred features

Locking clears complete rows and shifts rows above them down. A lock clears at most
four lines. Scores per clear event are fixed:

| Lines | Points |
| --- | --- |
| 1 | 100 |
| 2 | 300 |
| 3 | 500 |
| 4 | 800 |

IRS, SRS, T-spin detection/scoring, gravity progression, and other difficulty or
scoring changes are deferred. Rotation into a tight space uses the ordinary kick
rules and earns no special bonus.
