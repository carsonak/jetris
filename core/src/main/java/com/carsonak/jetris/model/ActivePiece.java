package com.carsonak.jetris.model;

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
    // TODO: Update getOccupiedCells() to use Normalized Top-Left Coordinates from
    // type.getBaseCells() instead of the removed int[][] getBaseShape().
    //
    // NOTE (Design Update & Algorithm Guide):
    // 1. Start with the 4 local points from `type.getBaseCells()` (which are at rotation 0).
    // 2. For each of the 4 points, rotate it 90 degrees clockwise `rotationIndex` times using the
    //    2D screen-coordinate rotation formula (where +x is right and +y is down):
    //      (rotX, rotY) -> (-rotY, rotX)
    // 3. Because `(-rotY, rotX)` produces negative local coordinates, find the minimum rotated X
    //    (`minX`) and minimum rotated Y (`minY`) across all 4 rotated points.
    // 4. Normalize each rotated point back to the top-left origin `(0, 0)` by subtracting `minX`
    //    and `minY`:
    //      normX = rotX - minX
    //      normY = rotY - minY
    //    This guarantees there are NEVER any blank leading rows or columns (`minX == 0, minY == 0`)
    //    in any orientation!
    // 5. Translate to absolute board coordinates by adding the piece's board origin `(x, y)`:
    //      new Point(normX + x, normY + y)
    //
    // (Optional optimization: if you want to cache the 4 normalized local points so rotation only
    // runs once per piece creation, you can compute them or keep this method clean and stateless.)
    throw new UnsupportedOperationException(
        "TODO: Implement normalized top-left rotation using type.getBaseCells()");
  }

  public ActivePiece moveBy(int dx, int dy) {
    return new ActivePiece(type, x + dx, y + dy, rotationIndex);
  }

  public ActivePiece rotateClockwise() {
    return new ActivePiece(type, x, y, rotationIndex + 1);
  }

  /**
   * Returns a new {@link ActivePiece} rotated 90 degrees anticlockwise (counter-clockwise).
   *
   * <p>TODO: Implement by decrementing {@code rotationIndex} (the compact constructor already
   * normalizes negative values via {@code Math.floorMod(rotationIndex, 4)}).
   */
  public ActivePiece rotateAnticlockwise() {
    throw new UnsupportedOperationException("TODO: Implement rotateAnticlockwise()");
  }
}
