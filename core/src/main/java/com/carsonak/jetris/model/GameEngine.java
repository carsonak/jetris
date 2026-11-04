package com.carsonak.jetris.model;

/** Target gameplay contract; see docs/game-rules.md. Implementations may still be in progress. */
public interface GameEngine extends BoardState {
  /**
   * Checks current overlap before gravity: unresolved overlap ends the game without locking or
   * changing the grid, score, lines, preview, or generator. Otherwise moves down one row or locks,
   * clears, scores, and spawns. Never ticks the newly spawned piece in the same call. No-op after
   * game over. The view schedules a full gravity interval after every spawn.
   */
  void tick();

  /**
   * Attempts to shift left one column, including during spawn recovery. Returns true only for a
   * fully clear, in-bounds destination; otherwise returns false without changing state.
   */
  boolean moveLeft();

  /**
   * Attempts to shift right one column, including during spawn recovery. Returns true only for a
   * fully clear, in-bounds destination; otherwise returns false without changing state.
   */
  boolean moveRight();

  /**
   * Attempts to rotate active piece 90 degrees clockwise, applying wall/floor kick offsets if
   * obstructed, including during spawn recovery. Tries (0,0), (-1,0), (+1,0), (-2,0), (+2,0),
   * (0,-1) in order; the first fully clear, in-bounds destination wins, without swept-path checks.
   * Returns true if rotated, false with unchanged state if all offsets fail or game is over.
   */
  boolean rotateClockwise();

  /**
   * Attempts to rotate active piece 90 degrees anticlockwise (counter-clockwise), applying the same
   * ordered, destination-only kicks as clockwise rotation, including during spawn recovery. Returns
   * true if rotated, false with unchanged state if all offsets fail or game is over.
   */
  boolean rotateAnticlockwise();

  /**
   * No-op during unresolved overlap or after game over. Otherwise moves down one row; if blocked,
   * locks, clears, scores, and spawns. An ignored drop preserves all state and the recovery window.
   */
  void softDrop();

  /**
   * No-op during unresolved overlap or after game over. Otherwise drops to the lowest reachable
   * valid position without skipping locked cells, locks, clears, scores, and spawns. An ignored
   * drop preserves all state and the recovery window.
   */
  void hardDrop();

  /** Clears the board, resets score/lines to 0, and spawns a fresh piece. */
  void reset();
}
