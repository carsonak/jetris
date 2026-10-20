package com.carsonak.jetris.model;

public interface BoardState {
  int getWidth();

  int getHeight();

  /**
   * Returns the locked Tetromino at (x, y), or null if the cell is empty. Coordinate convention:
   * (0, 0) is top-left, (width - 1, height - 1) is bottom-right.
   */
  Tetromino getLockedBlockAt(int x, int y);

  ActivePiece getActivePiece();

  Tetromino getNextPiecePreview();

  int getScore();

  int getLinesCleared();

  boolean isGameOver();
}
