package com.carsonak.jetris.model;

import java.util.List;
import java.util.Objects;

/**
 * An immutable shape, top-left position, and rotation on the board.
 *
 * <p>Coordinates increase rightward and downward. Positions may be outside the board: collision
 * checks, wall kicks, and movement acceptance belong to the board. Occupied cells are obtained by
 * translating the shape's cached orientation.
 *
 * @param type non-null shape
 * @param x left edge of the normalized orientation
 * @param y top edge of the normalized orientation
 * @param rotationIndex clockwise quarter-turn index; the model contract requires normalization to
 *     [0, 3]
 */
public record ActivePiece(Tetromino type, int x, int y, int rotationIndex) {
  /** Creates a piece; rejects a null type with {@link NullPointerException}. */
  public ActivePiece {
    Objects.requireNonNull(type, "Tetromino type cannot be null");
    rotationIndex = Math.floorMod(rotationIndex, 4);
  }

  /** Returns an unmodifiable list of four absolute occupied cells in unspecified order. */
  public List<Point> getOccupiedCells() {
    var piece = type.getCells(rotationIndex);

    return piece.stream().map(pos -> new Point(pos.x() + x, pos.y() + y)).toList();
  }

  /** Returns a new piece translated by the supplied offsets, without checking collisions. */
  public ActivePiece moveBy(int dx, int dy) {
    return new ActivePiece(type, x + dx, y + dy, rotationIndex);
  }

  /**
   * Returns a new {@link ActivePiece} rotated 90 degrees clockwise at the same position, without
   * wall kicks.
   */
  public ActivePiece rotateClockwise() {
    return new ActivePiece(type, x, y, rotationIndex + 1);
  }

  /**
   * Returns a new {@link ActivePiece} rotated 90 degrees anticlockwise (counter-clockwise) at the
   * same position, without wall kicks.
   */
  public ActivePiece rotateAnticlockwise() {
    return new ActivePiece(type, x, y, rotationIndex - 1);
  }
}
