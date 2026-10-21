package com.carsonak.jetris.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record ActivePiece(Tetromino type, int x, int y, int rotationIndex) {
  public ActivePiece {
    Objects.requireNonNull(type, "Tetromino type cannot be null");
    rotationIndex = Math.floorMod(rotationIndex, 4);
  }

  /**
   * Returns the absolute board coordinates (Point(x, y)) occupied by this piece's solid blocks in
   * its current position and rotation.
   */
  public List<Point> getOccupiedCells() {
    var positions = new ArrayList<Point>(4);
    var shape = type.getBaseShape();

    for (int r = 0; r < shape.length; r++) {
      var row = shape[r];

      for (int c = 0; c < row.length; c++) {
        if (row[c] == 0) {
          continue;
        }

        int posX = c;
        int posY = r;

        for (int rot = 0; rot < rotationIndex; rot++) {
          // Rotation formula: (X, Y) → (N - 1 - Y, X)
          var tmpX = posX;

          posX = shape.length - 1 - posY;
          posY = tmpX;
        }

        positions.add(new Point(posX + x, posY + y));
      }
    }

    return positions;
  }

  public ActivePiece moveBy(int dx, int dy) {
    return new ActivePiece(type, x + dx, y + dy, rotationIndex);
  }

  public ActivePiece rotateClockwise() {
    return new ActivePiece(type, x, y, rotationIndex + 1);
  }
}
