package com.carsonak.jetris.model;

public interface GameEngine extends BoardState {
  /** Advances gravity by 1 step: moves active piece down 1 row, or locks it if blocked. */
  void tick();

  /** Attempts to shift active piece left by 1 column. Returns true if moved, false if blocked. */
  boolean moveLeft();

  /** Attempts to shift active piece right by 1 column. Returns true if moved, false if blocked. */
  boolean moveRight();

  /** Attempts to rotate active piece 90 degrees CW. Returns true if rotated, false if blocked. */
  boolean rotateClockwise();

  /** Moves piece down 1 row; if blocked, locks piece immediately and spawns next piece. */
  void softDrop();

  /** Drops piece instantly to lowest valid position, locks it, and spawns next piece. */
  void hardDrop();

  /** Clears the board, resets score/lines to 0, and spawns a fresh piece. */
  void reset();
}
