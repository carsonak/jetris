package com.carsonak.jetris.model;

import java.util.ArrayList;
import java.util.List;

/**
 * The seven standard Tetris shapes, with four precomputed clockwise orientations.
 *
 * <p>Local coordinates use a top-left origin, with x increasing rightward and y increasing
 * downward. Each orientation is normalized independently so its minimum x and y are both zero.
 * Rotation uses (x, y) -> (-y, x) followed by normalization. Symmetric shapes still have four
 * rotation indices.
 */
public enum Tetromino {
  I(List.of(new Point(0, 0), new Point(1, 0), new Point(2, 0), new Point(3, 0)), 0x00FFFFff),
  O(List.of(new Point(0, 0), new Point(1, 0), new Point(0, 1), new Point(1, 1)), 0xFFFF00ff),
  T(List.of(new Point(1, 0), new Point(0, 1), new Point(1, 1), new Point(2, 1)), 0x800080ff),
  S(List.of(new Point(1, 0), new Point(2, 0), new Point(0, 1), new Point(1, 1)), 0x00FF00ff),
  Z(List.of(new Point(0, 0), new Point(1, 0), new Point(1, 1), new Point(2, 1)), 0xFF0000ff),
  J(List.of(new Point(0, 0), new Point(0, 1), new Point(1, 1), new Point(2, 1)), 0x0000FFff),
  L(List.of(new Point(2, 0), new Point(0, 1), new Point(1, 1), new Point(2, 1)), 0xFFA500ff);

  private final List<List<Point>> orientations;
  private final int width;
  private final int height;
  private final int rgbaColor;

  Tetromino(List<Point> baseCells, int rgbaColor) {
    width = baseCells.stream().mapToInt(Point::x).max().orElse(0) + 1;
    height = baseCells.stream().mapToInt(Point::y).max().orElse(0) + 1;
    this.rgbaColor = rgbaColor;

    // Calculate and cache the 4 rotated variations of a piece.

    var cells = new ArrayList<ArrayList<Point>>(4);

    cells.add(new ArrayList<>(baseCells));
    for (int rotationIndex = 1; rotationIndex < 4; rotationIndex++) {
      final var previousRotation = cells.get(rotationIndex - 1);
      final var currentRotation = new ArrayList<Point>(4);
      int minX = 0, minY = 0;

      for (int i = 0; i < previousRotation.size(); i++) {
        final var pos = previousRotation.get(i);
        final int x = -pos.y(), y = pos.x();

        currentRotation.add(new Point(x, y));
        if (x < minX) {
          minX = x;
        }

        if (y < minY) {
          minY = y;
        }
      }

      // Normalise piece to the origin.
      for (int i = 0; i < currentRotation.size(); i++) {
        final var pos = currentRotation.get(i);

        currentRotation.set(i, new Point(pos.x() - minX, pos.y() - minY));
      }

      cells.add(currentRotation);
    }

    orientations = cells.stream().map(rotation -> List.copyOf(rotation)).toList();
  }

  /**
   * Returns four distinct, unmodifiable local cells for the requested orientation.
   *
   * <p>The index is reduced modulo four using {@link Math#floorMod(int, int)}, including negative
   * indices. Zero is the spawn orientation; increasing indices represent clockwise quarter turns.
   * Cell ordering is unspecified. The returned cells have no board-position offset.
   *
   * @param rotationIndex number of clockwise quarter turns, which may be any integer.
   * @return the normalized cells of the selected orientation.
   */
  public List<Point> getCells(int rotationIndex) {
    rotationIndex = Math.floorMod(rotationIndex, 4);
    return orientations.get(rotationIndex);
  }

  /** Returns the horizontal width of this piece in its default orientation (rotation 0). */
  public int getWidth() {
    return width;
  }

  /** Returns the vertical height of this piece in its default orientation (rotation 0). */
  public int getHeight() {
    return height;
  }

  /** Returns this shape's packed RGBA8888 color, with red in the most significant byte. */
  public int getRgbaColor() {
    return rgbaColor;
  }
}
