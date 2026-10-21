package com.carsonak.jetris.model;

import java.util.List;

/**
 * Represents the 7 standard Tetris pieces using normalized top-left local coordinates.
 *
 * <p>NOTE (Design Update): We removed {@code int[][] getBaseShape()} so internal matrix padding
 * (blank rows/columns) is never exposed. Instead, each Tetromino stores its 4 solid block
 * coordinates in rotation 0 flushed to the top-left origin ({@code minX = 0, minY = 0}) via {@link
 * #getBaseCells()}, along with its base bounding dimensions {@link #getWidth()} and {@link
 * #getHeight()}.
 */
public enum Tetromino {
  I(List.of(new Point(0, 0), new Point(1, 0), new Point(2, 0), new Point(3, 0)), 0x00FFFFff),
  O(List.of(new Point(0, 0), new Point(1, 0), new Point(0, 1), new Point(1, 1)), 0xFFFF00ff),
  T(List.of(new Point(1, 0), new Point(0, 1), new Point(1, 1), new Point(2, 1)), 0x800080ff),
  S(List.of(new Point(1, 0), new Point(2, 0), new Point(0, 1), new Point(1, 1)), 0x00FF00ff),
  Z(List.of(new Point(0, 0), new Point(1, 0), new Point(1, 1), new Point(2, 1)), 0xFF0000ff),
  J(List.of(new Point(0, 0), new Point(0, 1), new Point(1, 1), new Point(2, 1)), 0x0000FFff),
  L(List.of(new Point(2, 0), new Point(0, 1), new Point(1, 1), new Point(2, 1)), 0xFFA500ff);

  private final List<Point> baseCells;
  private final int width;
  private final int height;
  private final int rgbaColor;

  Tetromino(List<Point> baseCells, int rgbaColor) {
    this.baseCells = List.copyOf(baseCells);
    this.width = baseCells.stream().mapToInt(Point::x).max().orElse(0) + 1;
    this.height = baseCells.stream().mapToInt(Point::y).max().orElse(0) + 1;
    this.rgbaColor = rgbaColor;
  }

  /**
   * Returns the unmodifiable list of 4 local {@link Point} coordinates for this piece in its
   * default orientation (rotation 0), normalized so that {@code minX == 0} and {@code minY == 0}.
   */
  public List<Point> getBaseCells() {
    return baseCells;
  }

  /** Returns the horizontal width of this piece in its default orientation (rotation 0). */
  public int getWidth() {
    return width;
  }

  /** Returns the vertical height of this piece in its default orientation (rotation 0). */
  public int getHeight() {
    return height;
  }

  public int getRgbaColor() {
    return rgbaColor;
  }
}
