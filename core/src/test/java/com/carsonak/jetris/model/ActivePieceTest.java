package com.carsonak.jetris.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("ActivePiece Model Tests")
class ActivePieceTest {

  @Nested
  @DisplayName("moveBy() Movement Tests")
  class MovementTests {

    @Test
    @DisplayName("moveBy() should shift coordinates by dx and dy without mutating original instance")
    void shouldShiftCoordinatesAndPreserveImmutability() {
      ActivePiece initial = new ActivePiece(Tetromino.T, 3, 5, 0);

      ActivePiece movedRight = initial.moveBy(1, 0);
      ActivePiece movedLeft = initial.moveBy(-1, 0);
      ActivePiece movedDown = initial.moveBy(0, 1);
      ActivePiece movedDiagonal = initial.moveBy(2, 3);

      // Verify original piece remains unchanged (immutable record contract)
      assertThat(initial.x()).isEqualTo(3);
      assertThat(initial.y()).isEqualTo(5);
      assertThat(initial.rotationIndex()).isEqualTo(0);
      assertThat(initial.type()).isEqualTo(Tetromino.T);

      // Verify new instances reflect relative displacements
      assertThat(movedRight).isEqualTo(new ActivePiece(Tetromino.T, 4, 5, 0));
      assertThat(movedLeft).isEqualTo(new ActivePiece(Tetromino.T, 2, 5, 0));
      assertThat(movedDown).isEqualTo(new ActivePiece(Tetromino.T, 3, 6, 0));
      assertThat(movedDiagonal).isEqualTo(new ActivePiece(Tetromino.T, 5, 8, 0));
    }
  }

  @Nested
  @DisplayName("rotateClockwise() Rotation Index Cycle Tests")
  class RotationCycleTests {

    @Test
    @DisplayName("rotateClockwise() should cycle rotationIndex 0 -> 1 -> 2 -> 3 -> 0 preserving position")
    void shouldCycleRotationIndexMod4() {
      ActivePiece r0 = new ActivePiece(Tetromino.I, 3, 0, 0);

      ActivePiece r1 = r0.rotateClockwise();
      ActivePiece r2 = r1.rotateClockwise();
      ActivePiece r3 = r2.rotateClockwise();
      ActivePiece r4 = r3.rotateClockwise();

      // Original remains unchanged
      assertThat(r0.rotationIndex()).isEqualTo(0);

      // Cycle verification
      assertThat(r1.rotationIndex()).isEqualTo(1);
      assertThat(r1.x()).isEqualTo(3);
      assertThat(r1.y()).isEqualTo(0);
      assertThat(r1.type()).isEqualTo(Tetromino.I);

      assertThat(r2.rotationIndex()).isEqualTo(2);
      assertThat(r3.rotationIndex()).isEqualTo(3);
      assertThat(r4.rotationIndex()).isEqualTo(0);
    }
  }

  @Nested
  @DisplayName("Tetromino.I Occupied Cells Across All 4 Orientations (4x4 Bounding Box)")
  class TetrominoIOrientationTests {

    /*
     * Tetromino.I 4x4 matrix representation:
     *
     * Rotation 0 (Horizontal, row 1):
     * [ . . . . ]
     * [ # # # # ] -> row 1, cols 0, 1, 2, 3
     * [ . . . . ]
     * [ . . . . ]
     *
     * Rotation 1 (90° CW, col 2):
     * [ . . # . ] -> row 0, col 2
     * [ . . # . ] -> row 1, col 2
     * [ . . # . ] -> row 2, col 2
     * [ . . # . ] -> row 3, col 2
     *
     * Rotation 2 (180° CW, row 2):
     * [ . . . . ]
     * [ . . . . ]
     * [ # # # # ] -> row 2, cols 0, 1, 2, 3
     * [ . . . . ]
     *
     * Rotation 3 (270° CW, col 1):
     * [ . # . . ] -> row 0, col 1
     * [ . # . . ] -> row 1, col 1
     * [ . # . . ] -> row 2, col 1
     * [ . # . . ] -> row 3, col 1
     */

    @Test
    @DisplayName("Rotation 0: Tetromino.I horizontal bar at row y+1")
    void shouldReturnOccupiedCellsForRotation0() {
      ActivePiece piece = new ActivePiece(Tetromino.I, 3, 0, 0);

      List<Point> cells = piece.getOccupiedCells();

      assertThat(cells)
          .containsExactlyInAnyOrder(
              new Point(3, 1),
              new Point(4, 1),
              new Point(5, 1),
              new Point(6, 1));
    }

    @Test
    @DisplayName("Rotation 1 (90° CW): Tetromino.I vertical bar at col x+2")
    void shouldReturnOccupiedCellsForRotation1() {
      ActivePiece piece = new ActivePiece(Tetromino.I, 3, 0, 1);

      List<Point> cells = piece.getOccupiedCells();

      assertThat(cells)
          .containsExactlyInAnyOrder(
              new Point(5, 0),
              new Point(5, 1),
              new Point(5, 2),
              new Point(5, 3));
    }

    @Test
    @DisplayName("Rotation 2 (180° CW): Tetromino.I horizontal bar at row y+2")
    void shouldReturnOccupiedCellsForRotation2() {
      ActivePiece piece = new ActivePiece(Tetromino.I, 3, 0, 2);

      List<Point> cells = piece.getOccupiedCells();

      assertThat(cells)
          .containsExactlyInAnyOrder(
              new Point(3, 2),
              new Point(4, 2),
              new Point(5, 2),
              new Point(6, 2));
    }

    @Test
    @DisplayName("Rotation 3 (270° CW): Tetromino.I vertical bar at col x+1")
    void shouldReturnOccupiedCellsForRotation3() {
      ActivePiece piece = new ActivePiece(Tetromino.I, 3, 0, 3);

      List<Point> cells = piece.getOccupiedCells();

      assertThat(cells)
          .containsExactlyInAnyOrder(
              new Point(4, 0),
              new Point(4, 1),
              new Point(4, 2),
              new Point(4, 3));
    }
  }

  @Nested
  @DisplayName("Tetromino.O Occupied Cells Across All 4 Orientations (2x2 Bounding Box)")
  class TetrominoOOrientationTests {

    /*
     * Tetromino.O 2x2 matrix representation:
     * All 4 blocks solid across all rotations:
     * [ # # ]
     * [ # # ]
     *
     * Occupies (col 0, row 0), (col 1, row 0), (col 0, row 1), (col 1, row 1) invariant under rotation.
     */

    @Test
    @DisplayName("Rotations 0 through 3: Tetromino.O occupied cells remain invariant under rotation")
    void shouldReturnInvariantOccupiedCellsAcrossAllOrientations() {
      int originX = 4;
      int originY = 5;

      Point[] expectedCells = {
        new Point(originX, originY),
        new Point(originX + 1, originY),
        new Point(originX, originY + 1),
        new Point(originX + 1, originY + 1)
      };

      for (int rotation = 0; rotation < 4; rotation++) {
        ActivePiece piece = new ActivePiece(Tetromino.O, originX, originY, rotation);
        assertThat(piece.getOccupiedCells())
            .as("Tetromino.O cells at rotation %d", rotation)
            .containsExactlyInAnyOrder(expectedCells);
      }
    }
  }

  @Nested
  @DisplayName("Tetromino.T Occupied Cells Across All 4 Orientations (3x3 Bounding Box)")
  class TetrominoTOrientationTests {

    /*
     * Tetromino.T 3x3 matrix representation:
     *
     * Rotation 0 (Pointing Up):
     * [ . # . ] -> (col 1, row 0)
     * [ # # # ] -> (col 0, row 1), (col 1, row 1), (col 2, row 1)
     * [ . . . ]
     *
     * Rotation 1 (90° CW - Pointing Right):
     * [ . # . ] -> (col 1, row 0)
     * [ . # # ] -> (col 1, row 1), (col 2, row 1)
     * [ . # . ] -> (col 1, row 2)
     *
     * Rotation 2 (180° CW - Pointing Down):
     * [ . . . ]
     * [ # # # ] -> (col 0, row 1), (col 1, row 1), (col 2, row 1)
     * [ . # . ] -> (col 1, row 2)
     *
     * Rotation 3 (270° CW - Pointing Left):
     * [ . # . ] -> (col 1, row 0)
     * [ # # . ] -> (col 0, row 1), (col 1, row 1)
     * [ . # . ] -> (col 1, row 2)
     */

    @Test
    @DisplayName("Rotation 0 (Pointing Up): top center block and horizontal bottom row")
    void shouldReturnOccupiedCellsForRotation0() {
      ActivePiece piece = new ActivePiece(Tetromino.T, 2, 4, 0);

      List<Point> cells = piece.getOccupiedCells();

      assertThat(cells)
          .containsExactlyInAnyOrder(
              new Point(3, 4), // (col 1, row 0) -> x=3, y=4
              new Point(2, 5), // (col 0, row 1) -> x=2, y=5
              new Point(3, 5), // (col 1, row 1) -> x=3, y=5
              new Point(4, 5) // (col 2, row 1) -> x=4, y=5
              );
    }

    @Test
    @DisplayName("Rotation 1 (90° CW - Pointing Right): vertical stem and right-pointing block")
    void shouldReturnOccupiedCellsForRotation1() {
      ActivePiece piece = new ActivePiece(Tetromino.T, 2, 4, 1);

      List<Point> cells = piece.getOccupiedCells();

      assertThat(cells)
          .containsExactlyInAnyOrder(
              new Point(3, 4), // (col 1, row 0) -> x=3, y=4
              new Point(3, 5), // (col 1, row 1) -> x=3, y=5
              new Point(4, 5), // (col 2, row 1) -> x=4, y=5
              new Point(3, 6) // (col 1, row 2) -> x=3, y=6
              );
    }

    @Test
    @DisplayName("Rotation 2 (180° CW - Pointing Down): horizontal row and bottom center block")
    void shouldReturnOccupiedCellsForRotation2() {
      ActivePiece piece = new ActivePiece(Tetromino.T, 2, 4, 2);

      List<Point> cells = piece.getOccupiedCells();

      assertThat(cells)
          .containsExactlyInAnyOrder(
              new Point(2, 5), // (col 0, row 1) -> x=2, y=5
              new Point(3, 5), // (col 1, row 1) -> x=3, y=5
              new Point(4, 5), // (col 2, row 1) -> x=4, y=5
              new Point(3, 6) // (col 1, row 2) -> x=3, y=6
              );
    }

    @Test
    @DisplayName("Rotation 3 (270° CW - Pointing Left): vertical stem and left-pointing block")
    void shouldReturnOccupiedCellsForRotation3() {
      ActivePiece piece = new ActivePiece(Tetromino.T, 2, 4, 3);

      List<Point> cells = piece.getOccupiedCells();

      assertThat(cells)
          .containsExactlyInAnyOrder(
              new Point(3, 4), // (col 1, row 0) -> x=3, y=4
              new Point(2, 5), // (col 0, row 1) -> x=2, y=5
              new Point(3, 5), // (col 1, row 1) -> x=3, y=5
              new Point(3, 6) // (col 1, row 2) -> x=3, y=6
              );
    }
  }

  @Nested
  @DisplayName("Integration: Moving and Rotating ActivePiece")
  class MovementAndRotationIntegrationTests {

    @Test
    @DisplayName("Occupied cells should correctly reflect both movement offset and rotation")
    void shouldUpdateOccupiedCellsAfterMoveAndRotate() {
      // Start at spawn position (3, 0)
      ActivePiece piece = new ActivePiece(Tetromino.T, 3, 0, 0);

      // Move right 1 and down 2 -> (4, 2), rotate clockwise once -> rotationIndex = 1
      ActivePiece shiftedAndRotated = piece.moveBy(1, 2).rotateClockwise();

      assertThat(shiftedAndRotated.x()).isEqualTo(4);
      assertThat(shiftedAndRotated.y()).isEqualTo(2);
      assertThat(shiftedAndRotated.rotationIndex()).isEqualTo(1);

      // Rotation 1 of T at (4, 2):
      // (col 1, row 0) -> (5, 2)
      // (col 1, row 1) -> (5, 3)
      // (col 2, row 1) -> (6, 3)
      // (col 1, row 2) -> (5, 4)
      assertThat(shiftedAndRotated.getOccupiedCells())
          .containsExactlyInAnyOrder(
              new Point(5, 2),
              new Point(5, 3),
              new Point(6, 3),
              new Point(5, 4));
    }
  }
}
