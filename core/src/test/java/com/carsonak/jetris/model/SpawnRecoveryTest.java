package com.carsonak.jetris.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class SpawnRecoveryTest {

  /** Five vertical I pieces fill one column; the next O overlaps that column at spawn. */
  private TetrisBoard spawnOAgainstFullColumn(int column) {
    AtomicInteger generated = new AtomicInteger();
    TetrisBoard board =
        new TetrisBoard(() -> generated.getAndIncrement() < 5 ? Tetromino.I : Tetromino.O);
    for (int i = 0; i < 5; i++) {
      assertThat(board.rotateClockwise()).isTrue();
      for (int x = 3; x < column; x++) {
        assertThat(board.moveRight()).isTrue();
      }
      board.hardDrop();
    }
    assertThat(board.getActivePiece()).isEqualTo(new ActivePiece(Tetromino.O, 4, 0, 0));
    assertThat(board.isGameOver()).isFalse();
    return board;
  }

  @ParameterizedTest
  @CsvSource({"4, false, 5", "5, true, 3"})
  void canRecoverHorizontallyInEitherDirectionBeforeFirstTick(
      int column, boolean left, int recoveredX) {
    TetrisBoard board = spawnOAgainstFullColumn(column);

    assertThat(left ? board.moveLeft() : board.moveRight()).isTrue();
    assertThat(board.getActivePiece()).isEqualTo(new ActivePiece(Tetromino.O, recoveredX, 0, 0));
    board.tick();

    assertThat(board.isGameOver()).isFalse();
    assertThat(board.getActivePiece()).isEqualTo(new ActivePiece(Tetromino.O, recoveredX, 1, 0));
  }

  @ParameterizedTest
  @ValueSource(booleans = {false, true})
  void partialHorizontalRecoveryIsRejectedWithoutChangingPiece(boolean left) {
    TetrisBoard board = spawnOAgainstFullColumn(left ? 4 : 5);
    ActivePiece overlapping = board.getActivePiece();

    assertThat(left ? board.moveLeft() : board.moveRight()).isFalse();
    assertThat(board.getActivePiece()).isEqualTo(overlapping);
    assertThat(board.isGameOver()).isFalse();
    board.tick();
    assertThat(board.isGameOver()).isTrue();
    assertThat(board.getActivePiece()).isEqualTo(overlapping);
  }

  @ParameterizedTest
  @ValueSource(booleans = {false, true})
  void normalDropsResumeAfterHorizontalRecovery(boolean hard) {
    TetrisBoard board = spawnOAgainstFullColumn(4);
    assertThat(board.moveRight()).isTrue();

    if (hard) {
      board.hardDrop();
      assertThat(board.getLockedBlockAt(5, 19)).isEqualTo(Tetromino.O);
      assertThat(board.getLockedBlockAt(6, 18)).isEqualTo(Tetromino.O);
      assertThat(board.getActivePiece()).isEqualTo(new ActivePiece(Tetromino.O, 4, 0, 0));
    } else {
      board.softDrop();
      assertThat(board.getActivePiece()).isEqualTo(new ActivePiece(Tetromino.O, 5, 1, 0));
      assertThat(board.getLockedBlockAt(5, 1)).isNull();
    }
    assertThat(board.isGameOver()).isFalse();
  }

  @Nested
  class WhiteboxOverlapSafety {

    // Seed isolated states to distinguish current-position validation from destination validation.
    // The current storage is boolean[height][width]. If storage changes, adapt only this fixture.
    private final AtomicInteger generated = new AtomicInteger();

    private TetrisBoard fixture(Tetromino type, Point... locked) throws Exception {
      TetrisBoard board =
          new TetrisBoard(
              () -> {
                generated.incrementAndGet();
                return Tetromino.I;
              });
      field("activePiece").set(board, new ActivePiece(type, type == Tetromino.O ? 4 : 3, 0, 0));
      boolean[][] grid = (boolean[][]) field("board").get(board);
      for (Point cell : locked) {
        grid[cell.y()][cell.x()] = true;
      }
      return board;
    }

    private Field field(String name) throws Exception {
      Field field = TetrisBoard.class.getDeclaredField(name);
      field.setAccessible(true);
      return field;
    }

    private Snapshot snapshot(TetrisBoard board) throws Exception {
      boolean[][] grid = (boolean[][]) field("board").get(board);
      List<List<Boolean>> rows = new ArrayList<>();
      for (boolean[] row : grid) {
        List<Boolean> cells = new ArrayList<>();
        for (boolean cell : row) {
          cells.add(cell);
        }
        rows.add(List.copyOf(cells));
      }
      return new Snapshot(
          board.getActivePiece(),
          List.copyOf(rows),
          board.getScore(),
          board.getLinesCleared(),
          board.getNextPiecePreview(),
          generated.get());
    }

    @Test
    void tickChecksCurrentOverlapEvenWhenDownwardDestinationIsClear() throws Exception {
      TetrisBoard board = fixture(Tetromino.O, new Point(4, 0));
      Snapshot before = snapshot(board);
      // O at (4,1) would occupy rows 1 and 2, which contain no locked cells.
      assertThat(before.grid().get(0).get(4)).isTrue();
      assertThat(before.grid().get(1)).doesNotContain(true);
      assertThat(before.grid().get(2)).doesNotContain(true);

      board.tick();

      assertThat(board.isGameOver()).isTrue();
      assertThat(snapshot(board)).isEqualTo(before);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void overlappingDropsPreserveAllStateAndStillAllowRecovery(boolean hard) throws Exception {
      TetrisBoard board = fixture(Tetromino.O, new Point(4, 0));
      field("score").setInt(board, 300);
      field("clearedLines").setInt(board, 2);
      Snapshot before = snapshot(board);

      for (int i = 0; i < 3; i++) {
        if (hard) {
          board.hardDrop();
        } else {
          board.softDrop();
        }
        assertThat(snapshot(board)).isEqualTo(before);
        assertThat(board.isGameOver()).isFalse();
      }

      assertThat(board.moveRight()).isTrue();
      board.tick();
      assertThat(board.isGameOver()).isFalse();
      assertThat(board.getActivePiece()).isEqualTo(new ActivePiece(Tetromino.O, 5, 1, 0));
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void ignoredDropsDoNotResolveOverlapBeforeTheFirstTick(boolean hard) throws Exception {
      TetrisBoard board = fixture(Tetromino.O, new Point(4, 0));
      Snapshot before = snapshot(board);
      if (hard) {
        board.hardDrop();
      } else {
        board.softDrop();
      }
      board.tick();

      assertThat(board.isGameOver()).isTrue();
      assertThat(snapshot(board)).isEqualTo(before);
    }

    @ParameterizedTest
    @CsvSource({
      "true, 0, 3", "false, 0, 3", "true, 1, 2", "false, 1, 2",
      "true, 2, 4", "false, 2, 4", "true, 3, 1", "false, 3, 1",
      "true, 4, 5", "false, 4, 5"
    })
    void rotationRecoveryUsesFirstClearKickDestination(
        boolean clockwise, int blockedCandidates, int expectedX) throws Exception {
      // Each vertical I candidate occupies a different column. Block earlier candidates only.
      // For the zero-offset case, block (4,0), which overlaps the horizontal spawn, not column 3.
      Point[] blockers = {new Point(3, 0), new Point(2, 0), new Point(4, 0), new Point(1, 0)};
      Point[] locked =
          blockedCandidates == 0
              ? new Point[] {new Point(4, 0)}
              : java.util.Arrays.copyOf(blockers, blockedCandidates);
      TetrisBoard board = fixture(Tetromino.I, locked);
      Snapshot before = snapshot(board);

      assertThat(clockwise ? board.rotateClockwise() : board.rotateAnticlockwise()).isTrue();
      assertThat(board.getActivePiece())
          .isEqualTo(new ActivePiece(Tetromino.I, expectedX, 0, clockwise ? 1 : 3));
      Snapshot after = snapshot(board);
      assertThat(after.grid()).isEqualTo(before.grid());
      assertThat(after.generated()).isEqualTo(before.generated());
      assertThat(board.isGameOver()).isFalse();
      board.tick();
      assertThat(board.isGameOver()).isFalse();
      assertThat(board.getActivePiece().y()).isEqualTo(1);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void rejectsAllBlockedKicksIncludingUpwardKickOutsideTopBoundary(boolean clockwise)
        throws Exception {
      TetrisBoard board =
          fixture(
              Tetromino.I,
              new Point(3, 0),
              new Point(2, 0),
              new Point(4, 0),
              new Point(1, 0),
              new Point(5, 0));
      Snapshot before = snapshot(board);

      assertThat(clockwise ? board.rotateClockwise() : board.rotateAnticlockwise()).isFalse();
      assertThat(snapshot(board)).isEqualTo(before);
      assertThat(board.isGameOver()).isFalse();
      board.tick();
      assertThat(board.isGameOver()).isTrue();
      assertThat(snapshot(board)).isEqualTo(before);
    }

    @Test
    void resetClearsGameOverAndOverlapAndAllowsGravityAgain() throws Exception {
      TetrisBoard board = fixture(Tetromino.O, new Point(4, 0));
      board.tick();
      assertThat(board.isGameOver()).isTrue();

      board.reset();
      assertThat(board.isGameOver()).isFalse();
      assertThat(board.getScore()).isZero();
      assertThat(board.getLinesCleared()).isZero();
      assertThat(snapshot(board).grid()).allSatisfy(row -> assertThat(row).doesNotContain(true));
      assertThat(board.getActivePiece()).isEqualTo(new ActivePiece(Tetromino.I, 3, 0, 0));
      board.tick();
      assertThat(board.getActivePiece()).isEqualTo(new ActivePiece(Tetromino.I, 3, 1, 0));
    }

    @Test
    void allActionsPreserveStateAfterOverlapGameOver() throws Exception {
      TetrisBoard board = fixture(Tetromino.O, new Point(4, 0));
      board.tick();
      assertThat(board.isGameOver()).isTrue();
      Snapshot before = snapshot(board);

      assertThat(board.moveLeft()).isFalse();
      assertThat(board.moveRight()).isFalse();
      assertThat(board.rotateClockwise()).isFalse();
      assertThat(board.rotateAnticlockwise()).isFalse();
      board.softDrop();
      board.hardDrop();
      board.tick();

      assertThat(snapshot(board)).isEqualTo(before);
      assertThat(board.isGameOver()).isTrue();
    }
  }

  private record Snapshot(
      ActivePiece piece,
      List<List<Boolean>> grid,
      int score,
      int lines,
      Tetromino preview,
      int generated) {}
}
