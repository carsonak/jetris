package com.carsonak.jetris.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

@DisplayName("ActivePiece Model Tests (Normalized Top-Left Coordinates)")
class ActivePieceTest {

  @ParameterizedTest
  @EnumSource(Tetromino.class)
  void translatesEveryCachedOrientationWithoutMutatingIt(Tetromino type) {
    for (int rotation = 0; rotation < 4; rotation++) {
      List<Point> local = List.copyOf(type.getCells(rotation));
      ActivePiece piece = new ActivePiece(type, -3, -5, rotation);
      assertThat(piece.getOccupiedCells())
          .containsExactlyInAnyOrderElementsOf(
              local.stream().map(cell -> new Point(cell.x() - 3, cell.y() - 5)).toList());
      assertThatThrownBy(() -> piece.getOccupiedCells().clear())
          .isInstanceOf(UnsupportedOperationException.class);
      assertThat(type.getCells(rotation)).containsExactlyElementsOf(local);
      assertThat(piece.rotateClockwise().rotateClockwise().rotateClockwise().rotateClockwise())
          .isEqualTo(piece);
      assertThat(
              piece
                  .rotateAnticlockwise()
                  .rotateAnticlockwise()
                  .rotateAnticlockwise()
                  .rotateAnticlockwise())
          .isEqualTo(piece);
    }
  }

  @Nested
  @DisplayName("moveBy() Movement Tests")
  class MovementTests {

    @Test
    @DisplayName(
        "moveBy() should shift coordinates by dx and dy without mutating original instance")
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
  @DisplayName("rotateClockwise() & rotateAnticlockwise() Rotation Index Cycle Tests")
  class RotationCycleTests {

    @Test
    @DisplayName(
        "rotateClockwise() should cycle rotationIndex 0 -> 1 -> 2 -> 3 -> 0 preserving position")
    void shouldCycleRotationIndexClockwiseMod4() {
      ActivePiece r0 = new ActivePiece(Tetromino.I, 3, 0, 0);

      ActivePiece r1 = r0.rotateClockwise();
      ActivePiece r2 = r1.rotateClockwise();
      ActivePiece r3 = r2.rotateClockwise();
      ActivePiece r4 = r3.rotateClockwise();

      assertThat(r0.rotationIndex()).isZero();
      assertThat(r1).isEqualTo(new ActivePiece(Tetromino.I, 3, 0, 1));
      assertThat(r2).isEqualTo(new ActivePiece(Tetromino.I, 3, 0, 2));
      assertThat(r3).isEqualTo(new ActivePiece(Tetromino.I, 3, 0, 3));
      assertThat(r4).isEqualTo(new ActivePiece(Tetromino.I, 3, 0, 0));
    }

    @Test
    @DisplayName(
        "rotateAnticlockwise() should cycle rotationIndex 0 -> 3 -> 2 -> 1 -> 0 preserving"
            + " position")
    void shouldCycleRotationIndexAnticlockwiseMod4() {
      ActivePiece r0 = new ActivePiece(Tetromino.T, 4, 2, 0);

      ActivePiece r3 = r0.rotateAnticlockwise();
      ActivePiece r2 = r3.rotateAnticlockwise();
      ActivePiece r1 = r2.rotateAnticlockwise();
      ActivePiece backTo0 = r1.rotateAnticlockwise();

      assertThat(r0.rotationIndex()).isZero();
      assertThat(r3).isEqualTo(new ActivePiece(Tetromino.T, 4, 2, 3));
      assertThat(r2).isEqualTo(new ActivePiece(Tetromino.T, 4, 2, 2));
      assertThat(r1).isEqualTo(new ActivePiece(Tetromino.T, 4, 2, 1));
      assertThat(backTo0).isEqualTo(new ActivePiece(Tetromino.T, 4, 2, 0));
    }

    @Test
    @DisplayName("rotateClockwise() followed by rotateAnticlockwise() should be an identity")
    void clockwiseAndAnticlockwiseShouldBeInverses() {
      ActivePiece piece = new ActivePiece(Tetromino.L, 2, 7, 2);

      assertThat(piece.rotateClockwise().rotateAnticlockwise()).isEqualTo(piece);
      assertThat(piece.rotateAnticlockwise().rotateClockwise()).isEqualTo(piece);
    }
  }

  @Nested
  @DisplayName("Tetromino.I Normalized Occupied Cells Across All 4 Orientations")
  class TetrominoIOrientationTests {

    /*
     * NOTE: With Normalized Top-Left Coordinates, Tetromino.I has NO blank leading rows or columns:
     *
     * Rotation 0 & 2 (Horizontal 4x1 bar flushed to row 0):
     * [ # # # # ] -> (x+0, y+0), (x+1, y+0), (x+2, y+0), (x+3, y+0)
     *
     * Rotation 1 & 3 (Vertical 1x4 bar flushed to col 0):
     * [ # ] -> (x+0, y+0)
     * [ # ] -> (x+0, y+1)
     * [ # ] -> (x+0, y+2)
     * [ # ] -> (x+0, y+3)
     */

    @Test
    @DisplayName("Rotation 0: Tetromino.I horizontal bar flushed to top row y")
    void shouldReturnOccupiedCellsForRotation0() {
      ActivePiece piece = new ActivePiece(Tetromino.I, 3, 0, 0);

      assertThat(piece.getOccupiedCells())
          .containsExactlyInAnyOrder(
              new Point(3, 0), new Point(4, 0), new Point(5, 0), new Point(6, 0));
    }

    @Test
    @DisplayName("Rotation 1 (90° CW): Tetromino.I vertical bar flushed to left col x")
    void shouldReturnOccupiedCellsForRotation1() {
      ActivePiece piece = new ActivePiece(Tetromino.I, 3, 0, 1);

      assertThat(piece.getOccupiedCells())
          .containsExactlyInAnyOrder(
              new Point(3, 0), new Point(3, 1), new Point(3, 2), new Point(3, 3));
    }

    @Test
    @DisplayName("Rotation 2 (180° CW): Tetromino.I horizontal bar flushed to top row y")
    void shouldReturnOccupiedCellsForRotation2() {
      ActivePiece piece = new ActivePiece(Tetromino.I, 3, 0, 2);

      assertThat(piece.getOccupiedCells())
          .containsExactlyInAnyOrder(
              new Point(3, 0), new Point(4, 0), new Point(5, 0), new Point(6, 0));
    }

    @Test
    @DisplayName("Rotation 3 (270° CW): Tetromino.I vertical bar flushed to left col x")
    void shouldReturnOccupiedCellsForRotation3() {
      ActivePiece piece = new ActivePiece(Tetromino.I, 3, 0, 3);

      assertThat(piece.getOccupiedCells())
          .containsExactlyInAnyOrder(
              new Point(3, 0), new Point(3, 1), new Point(3, 2), new Point(3, 3));
    }
  }

  @Nested
  @DisplayName("Tetromino.O Normalized Occupied Cells Across All 4 Orientations")
  class TetrominoOOrientationTests {

    @Test
    @DisplayName(
        "Rotations 0 through 3: Tetromino.O occupied cells remain invariant under rotation")
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
  @DisplayName("Tetromino.T Normalized Occupied Cells Across All 4 Orientations")
  class TetrominoTOrientationTests {

    /*
     * Normalized Top-Left representations for Tetromino.T:
     *
     * Rotation 0 (Pointing Up, 3x2):
     * [ . # . ] -> (1, 0)
     * [ # # # ] -> (0, 1), (1, 1), (2, 1)
     *
     * Rotation 1 (90° CW - Pointing Right, 2x3, flushed to col 0):
     * [ # . ] -> (0, 0)
     * [ # # ] -> (0, 1), (1, 1)
     * [ # . ] -> (0, 2)
     *
     * Rotation 2 (180° CW - Pointing Down, 3x2, flushed to row 0):
     * [ # # # ] -> (0, 0), (1, 0), (2, 0)
     * [ . # . ] -> (1, 1)
     *
     * Rotation 3 (270° CW - Pointing Left, 2x3, flushed to col 0):
     * [ . # ] -> (1, 0)
     * [ # # ] -> (0, 1), (1, 1)
     * [ . # ] -> (1, 2)
     */

    @Test
    @DisplayName("Rotation 0 (Pointing Up, 3x2): top center block and horizontal bottom row")
    void shouldReturnOccupiedCellsForRotation0() {
      ActivePiece piece = new ActivePiece(Tetromino.T, 2, 4, 0);

      assertThat(piece.getOccupiedCells())
          .containsExactlyInAnyOrder(
              new Point(3, 4), new Point(2, 5), new Point(3, 5), new Point(4, 5));
    }

    @Test
    @DisplayName("Rotation 1 (90° CW - Pointing Right, 2x3): vertical stem flushed to col x")
    void shouldReturnOccupiedCellsForRotation1() {
      ActivePiece piece = new ActivePiece(Tetromino.T, 2, 4, 1);

      assertThat(piece.getOccupiedCells())
          .containsExactlyInAnyOrder(
              new Point(2, 4), new Point(2, 5), new Point(3, 5), new Point(2, 6));
    }

    @Test
    @DisplayName("Rotation 2 (180° CW - Pointing Down, 3x2): horizontal bar flushed to row y")
    void shouldReturnOccupiedCellsForRotation2() {
      ActivePiece piece = new ActivePiece(Tetromino.T, 2, 4, 2);

      assertThat(piece.getOccupiedCells())
          .containsExactlyInAnyOrder(
              new Point(2, 4), new Point(3, 4), new Point(4, 4), new Point(3, 5));
    }

    @Test
    @DisplayName("Rotation 3 (270° CW - Pointing Left, 2x3): left-pointing tip at col x")
    void shouldReturnOccupiedCellsForRotation3() {
      ActivePiece piece = new ActivePiece(Tetromino.T, 2, 4, 3);

      assertThat(piece.getOccupiedCells())
          .containsExactlyInAnyOrder(
              new Point(3, 4), new Point(2, 5), new Point(3, 5), new Point(3, 6));
    }
  }

  @Nested
  @DisplayName("Integration: Moving and Rotating ActivePiece")
  class MovementAndRotationIntegrationTests {

    @Test
    @DisplayName("Occupied cells should correctly reflect both movement offset and rotation")
    void shouldUpdateOccupiedCellsAfterMoveAndRotate() {
      ActivePiece piece = new ActivePiece(Tetromino.T, 3, 0, 0);

      // Move right 1 and down 2 -> (4, 2), rotate clockwise once -> rotationIndex = 1
      ActivePiece shiftedAndRotated = piece.moveBy(1, 2).rotateClockwise();

      assertThat(shiftedAndRotated).isEqualTo(new ActivePiece(Tetromino.T, 4, 2, 1));
      assertThat(shiftedAndRotated.getOccupiedCells())
          .containsExactlyInAnyOrder(
              new Point(4, 2), new Point(4, 3), new Point(5, 3), new Point(4, 4));
    }
  }

  @Nested
  @DisplayName("Whitebox: Invariants, Edge Cases & Invalid States")
  class WhiteboxInvariantsAndEdgeCaseTests {

    @Test
    @DisplayName("Compact constructor should reject null Tetromino type with NullPointerException")
    void shouldThrowNullPointerExceptionWhenTypeIsNull() {
      assertThatThrownBy(() -> new ActivePiece(null, 0, 0, 0))
          .isInstanceOf(NullPointerException.class)
          .hasMessage("Tetromino type cannot be null");
    }

    @ParameterizedTest(name = "raw rotationIndex {0} should normalize to {1}")
    @CsvSource({
      "0, 0",
      "1, 1",
      "2, 2",
      "3, 3",
      "4, 0",
      "5, 1",
      "6, 2",
      "7, 3",
      "100, 0",
      "-1, 3",
      "-2, 2",
      "-3, 1",
      "-4, 0",
      "-5, 3",
      "2147483647, 3",
      "-2147483648, 0"
    })
    @DisplayName(
        "Compact constructor should normalize positive and negative rotationIndex values into [0,"
            + " 3]")
    void shouldNormalizeRotationIndexIntoValidRange(int rawRotation, int expectedNormalized) {
      ActivePiece piece = new ActivePiece(Tetromino.T, 3, 0, rawRotation);

      assertThat(piece.rotationIndex()).isEqualTo(expectedNormalized);
    }

    @Test
    @DisplayName(
        "Pieces with equivalent mod-4 rotationIndex should be equal and occupy identical cells")
    void shouldTreatEquivalentMod4RotationsAsEqual() {
      ActivePiece canonical = new ActivePiece(Tetromino.L, 2, 3, 3);
      ActivePiece fromNegative = new ActivePiece(Tetromino.L, 2, 3, -1);
      ActivePiece fromLargePositive = new ActivePiece(Tetromino.L, 2, 3, 7);

      assertThat(fromNegative).isEqualTo(canonical);
      assertThat(fromLargePositive).isEqualTo(canonical);
      assertThat(fromNegative.getOccupiedCells()).isEqualTo(canonical.getOccupiedCells());
      assertThat(fromLargePositive.getOccupiedCells()).isEqualTo(canonical.getOccupiedCells());
    }

    @ParameterizedTest
    @EnumSource(Tetromino.class)
    @DisplayName(
        "Invariant: every Tetromino in every rotation [0..3] has 4 distinct cells flushed to"
            + " (originX, originY) with span <= 4")
    void everyTetrominoInAllRotationsShouldBeNormalizedToTopLeftAndHaveSpanAtMost4(Tetromino type) {
      int originX = 5;
      int originY = 7;

      for (int rot = 0; rot < 4; rot++) {
        ActivePiece piece = new ActivePiece(type, originX, originY, rot);
        List<Point> cells = piece.getOccupiedCells();

        assertThat(cells)
            .as("%s at rotation %d must have 4 distinct cells", type, rot)
            .hasSize(4)
            .doesNotHaveDuplicates();

        int minX = cells.stream().mapToInt(Point::x).min().orElseThrow();
        int maxX = cells.stream().mapToInt(Point::x).max().orElseThrow();
        int minY = cells.stream().mapToInt(Point::y).min().orElseThrow();
        int maxY = cells.stream().mapToInt(Point::y).max().orElseThrow();

        // Top-left normalization invariant: no blank leading rows or columns!
        assertThat(minX)
            .as("%s rot %d must be flushed to originX (no blank left column)", type, rot)
            .isEqualTo(originX);
        assertThat(minY)
            .as("%s rot %d must be flushed to originY (no blank top row)", type, rot)
            .isEqualTo(originY);

        // 4-block maximum span invariant (guarantees a single piece lock can never clear > 4 lines)
        assertThat(maxX - minX + 1).as("%s rot %d width <= 4", type, rot).isBetween(1, 4);
        assertThat(maxY - minY + 1).as("%s rot %d height <= 4", type, rot).isBetween(1, 4);
      }
    }

    @Test
    @DisplayName(
        "Edge case: moveBy(0, 0) should produce an equal piece with identical occupied cells")
    void shouldHandleZeroDisplacementMove() {
      ActivePiece piece = new ActivePiece(Tetromino.S, 4, 8, 2);
      ActivePiece unmoved = piece.moveBy(0, 0);

      assertThat(unmoved).isEqualTo(piece);
      assertThat(unmoved.getOccupiedCells()).isEqualTo(piece.getOccupiedCells());
    }
  }
}
