package com.carsonak.jetris.model;

import java.util.List;

public record ActivePiece(Tetromino type, int x, int y, int rotationIndex) {
  /**
   * Returns the absolute board coordinates (Point(x, y)) occupied by this piece's solid blocks in
   * its current position and rotation.
   */
  public List<Point> getOccupiedCells() {
    throw new UnsupportedOperationException("TODO: Implement after AI generates tests");
  }

  public ActivePiece moveBy(int dx, int dy) {
    return new ActivePiece(type, x + dx, y + dy, rotationIndex);
  }

  public ActivePiece rotateClockwise() {
    return new ActivePiece(type, x, y, (rotationIndex + 1) % 4);
  }
}
