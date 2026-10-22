package com.carsonak.jetris.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

class TetrominoTest {

  @Test
  void containsExactlyTheSevenStandardPieces() {
    assertThat(Tetromino.values())
        .containsExactlyInAnyOrder(
            Tetromino.I,
            Tetromino.O,
            Tetromino.T,
            Tetromino.S,
            Tetromino.Z,
            Tetromino.J,
            Tetromino.L);
  }

  @ParameterizedTest
  @CsvSource({"I, 4, 1", "O, 2, 2", "T, 3, 2", "S, 3, 2", "Z, 3, 2", "J, 3, 2", "L, 3, 2"})
  void reportsBaseOrientationDimensions(Tetromino type, int width, int height) {
    assertThat(type.getWidth()).isEqualTo(width);
    assertThat(type.getHeight()).isEqualTo(height);
  }

  @ParameterizedTest
  @MethodSource("baseShapes")
  void containsTheExpectedBaseCells(Tetromino type, List<Point> expected) {
    assertThat(type.getCells(0)).containsExactlyInAnyOrderElementsOf(expected);
  }

  static Stream<Arguments> baseShapes() {
    return Stream.of(
        Arguments.of(
            Tetromino.I,
            List.of(new Point(0, 0), new Point(1, 0), new Point(2, 0), new Point(3, 0))),
        Arguments.of(
            Tetromino.O,
            List.of(new Point(0, 0), new Point(1, 0), new Point(0, 1), new Point(1, 1))),
        Arguments.of(
            Tetromino.T,
            List.of(new Point(1, 0), new Point(0, 1), new Point(1, 1), new Point(2, 1))),
        Arguments.of(
            Tetromino.S,
            List.of(new Point(1, 0), new Point(2, 0), new Point(0, 1), new Point(1, 1))),
        Arguments.of(
            Tetromino.Z,
            List.of(new Point(0, 0), new Point(1, 0), new Point(1, 1), new Point(2, 1))),
        Arguments.of(
            Tetromino.J,
            List.of(new Point(0, 0), new Point(0, 1), new Point(1, 1), new Point(2, 1))),
        Arguments.of(
            Tetromino.L,
            List.of(new Point(2, 0), new Point(0, 1), new Point(1, 1), new Point(2, 1))));
  }

  @ParameterizedTest
  @EnumSource(Tetromino.class)
  void baseCellsAreDistinctNormalizedAndTouchEveryBoundingEdge(Tetromino type) {
    List<Point> cells = type.getCells(0);

    assertThat(cells).hasSize(4).doesNotHaveDuplicates().doesNotContainNull();
    assertThat(cells)
        .allSatisfy(
            cell -> {
              assertThat(cell.x()).isBetween(0, type.getWidth() - 1);
              assertThat(cell.y()).isBetween(0, type.getHeight() - 1);
            });
    assertThat(cells).anySatisfy(cell -> assertThat(cell.x()).isZero());
    assertThat(cells).anySatisfy(cell -> assertThat(cell.y()).isZero());
    assertThat(cells).anySatisfy(cell -> assertThat(cell.x()).isEqualTo(type.getWidth() - 1));
    assertThat(cells).anySatisfy(cell -> assertThat(cell.y()).isEqualTo(type.getHeight() - 1));
  }

  @ParameterizedTest
  @EnumSource(Tetromino.class)
  void baseCellsCannotBeModified(Tetromino type) {
    List<Point> original = List.copyOf(type.getCells(0));

    assertThatThrownBy(() -> type.getCells(0).add(new Point(9, 9)))
        .isInstanceOf(UnsupportedOperationException.class);
    assertThatThrownBy(() -> type.getCells(0).set(0, new Point(9, 9)))
        .isInstanceOf(UnsupportedOperationException.class);
    assertThatThrownBy(() -> type.getCells(0).remove(0))
        .isInstanceOf(UnsupportedOperationException.class);
    assertThat(type.getCells(0)).containsExactlyElementsOf(original);
  }

  @ParameterizedTest
  @EnumSource(Tetromino.class)
  void allOrientationsMatchExplicitShapeFixtures(Tetromino type) {
    // Row strings are independent visual fixtures, not a copy of the rotation algorithm.
    String[] shapes =
        switch (type) {
          case I -> new String[] {"####", "#/#/#/#", "####", "#/#/#/#"};
          case O -> new String[] {"##/##", "##/##", "##/##", "##/##"};
          case T -> new String[] {".#./###", "#./##/#.", "###/.#.", ".#/##/.#"};
          case S -> new String[] {".##/##.", "#./##/.#", ".##/##.", "#./##/.#"};
          case Z -> new String[] {"##./.##", ".#/##/#.", "##./.##", ".#/##/#."};
          case J -> new String[] {"#../###", "##/#./#.", "###/..#", ".#/.#/##"};
          case L -> new String[] {"..#/###", "#./#./##", "###/#..", "##/.#/.#"};
        };
    for (int rotation = 0; rotation < 4; rotation++) {
      var expected = new java.util.ArrayList<Point>();
      String[] rows = shapes[rotation].split("/");
      for (int y = 0; y < rows.length; y++) {
        for (int x = 0; x < rows[y].length(); x++) {
          if (rows[y].charAt(x) == '#') expected.add(new Point(x, y));
        }
      }
      assertThat(type.getCells(rotation))
          .as("%s rotation %d", type, rotation)
          .containsExactlyInAnyOrderElementsOf(expected);
      final int index = rotation;
      assertThatThrownBy(() -> type.getCells(index).clear())
          .isInstanceOf(UnsupportedOperationException.class);
    }
  }

  @ParameterizedTest
  @EnumSource(Tetromino.class)
  void normalizesAnyIntegerRotationWithoutChangingCachedCells(Tetromino type) {
    for (int raw :
        new int[] {Integer.MIN_VALUE, -5, -4, -3, -2, -1, 0, 3, 4, 5, Integer.MAX_VALUE}) {
      assertThat(type.getCells(raw))
          .containsExactlyElementsOf(type.getCells(Math.floorMod(raw, 4)));
    }
  }

  @ParameterizedTest
  @CsvSource({
    "I, 00FFFFFF",
    "O, FFFF00FF",
    "T, 800080FF",
    "S, 00FF00FF",
    "Z, FF0000FF",
    "J, 0000FFFF",
    "L, FFA500FF"
  })
  void reportsPackedRgbaColor(Tetromino type, String hex) {
    assertThat(type.getRgbaColor()).isEqualTo((int) Long.parseLong(hex, 16));
  }
}
