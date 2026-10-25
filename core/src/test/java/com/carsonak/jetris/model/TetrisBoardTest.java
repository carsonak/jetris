package com.carsonak.jetris.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Queue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("TetrisBoard GameEngine & BoardState Tests")
class TetrisBoardTest {

  /**
   * Creates a deterministic PieceGenerator that yields pieces from the given sequence, and repeats
   * the last piece if the sequence is exhausted.
   */
  private static PieceGenerator sequenceOf(Tetromino... pieces) {
    Queue<Tetromino> queue = new ArrayDeque<>(Arrays.asList(pieces));
    Tetromino fallback = pieces.length > 0 ? pieces[pieces.length - 1] : Tetromino.O;
    return () -> queue.isEmpty() ? fallback : queue.poll();
  }

  @Nested
  @DisplayName("1. Board Initialization & Piece Spawning")
  class InitializationAndSpawningTests {

    @Test
    @DisplayName(
        "Default constructor should initialize 10x20 empty board, score 0, lines 0, and not game over")
    void shouldInitializeDefaultBoardState() {
      TetrisBoard board = new TetrisBoard(sequenceOf(Tetromino.T, Tetromino.I));

      assertThat(board.getWidth()).isEqualTo(10);
      assertThat(board.getHeight()).isEqualTo(20);
      assertThat(board.getScore()).isZero();
      assertThat(board.getLinesCleared()).isZero();
      assertThat(board.isGameOver()).isFalse();

      for (int y = 0; y < board.getHeight(); y++) {
        for (int x = 0; x < board.getWidth(); x++) {
          assertThat(board.getLockedBlockAt(x, y))
              .as("Cell (%d, %d) should initially be empty", x, y)
              .isNull();
        }
      }
    }

    @Test
    @DisplayName(
        "Custom dimensions constructor should initialize board with specified width and height")
    void shouldSupportCustomBoardDimensions() {
      TetrisBoard board = new TetrisBoard(8, 16, sequenceOf(Tetromino.O, Tetromino.T));

      assertThat(board.getWidth()).isEqualTo(8);
      assertThat(board.getHeight()).isEqualTo(16);
      // 2-wide O piece on width 8 spawns at x = (8 - 2) / 2 = 3, y = 0
      assertThat(board.getActivePiece()).isEqualTo(new ActivePiece(Tetromino.O, 3, 0, 0));
      assertThat(board.getNextPiecePreview()).isEqualTo(Tetromino.T);
    }

    @Test
    @DisplayName(
        "3-wide Tetromino (e.g. T) should spawn at top-center x = (10 - 3) / 2 = 3, y = 0, rotation 0")
    void shouldSpawn3WidePieceAtTopCenterAndPopulatePreview() {
      TetrisBoard board = new TetrisBoard(sequenceOf(Tetromino.T, Tetromino.L));

      assertThat(board.getActivePiece()).isEqualTo(new ActivePiece(Tetromino.T, 3, 0, 0));
      assertThat(board.getNextPiecePreview()).isEqualTo(Tetromino.L);
    }

    @Test
    @DisplayName(
        "4-wide Tetromino (I) should spawn at top-center x = (10 - 4) / 2 = 3, y = 0, rotation 0")
    void shouldSpawn4WidePieceAtTopCenter() {
      TetrisBoard board = new TetrisBoard(sequenceOf(Tetromino.I, Tetromino.S));

      assertThat(board.getActivePiece()).isEqualTo(new ActivePiece(Tetromino.I, 3, 0, 0));
      assertThat(board.getNextPiecePreview()).isEqualTo(Tetromino.S);
    }

    @Test
    @DisplayName(
        "2-wide Tetromino (O) should spawn at top-center x = (10 - 2) / 2 = 4, y = 0, rotation 0")
    void shouldSpawn2WidePieceAtTopCenter() {
      TetrisBoard board = new TetrisBoard(sequenceOf(Tetromino.O, Tetromino.Z));

      assertThat(board.getActivePiece()).isEqualTo(new ActivePiece(Tetromino.O, 4, 0, 0));
      assertThat(board.getNextPiecePreview()).isEqualTo(Tetromino.Z);
    }
  }

  @Nested
  @DisplayName("2. Horizontal Movement & Wall/Block Collisions")
  class HorizontalMovementTests {

    @Test
    @DisplayName("moveLeft() and moveRight() should shift active piece by 1 column when unblocked")
    void shouldMoveLeftAndRightWhenUnblocked() {
      TetrisBoard board = new TetrisBoard(sequenceOf(Tetromino.T, Tetromino.O));

      assertThat(board.moveLeft()).isTrue();
      assertThat(board.getActivePiece()).isEqualTo(new ActivePiece(Tetromino.T, 2, 0, 0));

      assertThat(board.moveRight()).isTrue();
      assertThat(board.getActivePiece()).isEqualTo(new ActivePiece(Tetromino.T, 3, 0, 0));
    }

    @Test
    @DisplayName("moveLeft() should stop at left wall (x = 0) and return false when blocked")
    void shouldRejectMoveLeftAgainstLeftWall() {
      // O spawns at x = 4, occupies cols 4 and 5
      TetrisBoard board = new TetrisBoard(sequenceOf(Tetromino.O));

      assertThat(board.moveLeft()).isTrue(); // x = 3
      assertThat(board.moveLeft()).isTrue(); // x = 2
      assertThat(board.moveLeft()).isTrue(); // x = 1
      assertThat(board.moveLeft()).isTrue(); // x = 0 (occupies cols 0, 1)

      // Next move left hits the left wall
      assertThat(board.moveLeft()).isFalse();
      assertThat(board.getActivePiece().x()).isZero();
    }

    @Test
    @DisplayName("moveRight() should stop at right wall (x = width - pieceWidth) and return false")
    void shouldRejectMoveRightAgainstRightWall() {
      // O spawns at x = 4, occupies cols 4 and 5
      TetrisBoard board = new TetrisBoard(sequenceOf(Tetromino.O));

      assertThat(board.moveRight()).isTrue(); // x = 5
      assertThat(board.moveRight()).isTrue(); // x = 6
      assertThat(board.moveRight()).isTrue(); // x = 7
      assertThat(board.moveRight()).isTrue(); // x = 8 (occupies cols 8, 9)

      // Next move right hits the right wall
      assertThat(board.moveRight()).isFalse();
      assertThat(board.getActivePiece().x()).isEqualTo(8);
    }

    @Test
    @DisplayName(
        "Tetromino.I in vertical rotation 1 should align cleanly with left wall at x = 0 and right wall at x = 9")
    void verticalIPieceShouldAlignCleanlyWithBothWallsWithoutNegativeOrigins() {
      // I spawns at x = 3, y = 0
      TetrisBoard board = new TetrisBoard(sequenceOf(Tetromino.I));
      assertThat(board.rotateClockwise()).isTrue(); // rotation 1: 1x4 vertical bar at col x

      assertThat(board.moveLeft()).isTrue(); // x = 2
      assertThat(board.moveLeft()).isTrue(); // x = 1
      assertThat(board.moveLeft()).isTrue(); // x = 0 (left wall!)
      assertThat(board.moveLeft()).isFalse();
      assertThat(board.getActivePiece().x()).isZero();

      for (int expectedX = 1; expectedX <= 9; expectedX++) {
        assertThat(board.moveRight()).isTrue();
        assertThat(board.getActivePiece().x()).isEqualTo(expectedX);
      }
      // At right wall x = 9
      assertThat(board.moveRight()).isFalse();
      assertThat(board.getActivePiece().x()).isEqualTo(9);
    }

    @Test
    @DisplayName("moveLeft() and moveRight() should be blocked by existing locked blocks")
    void shouldRejectHorizontalMoveIntoLockedBlocks() {
      // First piece O drops at center (cols 4, 5 -> rows 18, 19)
      TetrisBoard board = new TetrisBoard(sequenceOf(Tetromino.O, Tetromino.O, Tetromino.O));
      board.hardDrop();

      // Second piece O moves left to cols 2, 3, then drops almost to the bottom (rows 18, 19)
      board.moveLeft();
      board.moveLeft();
      for (int i = 0; i < 18; i++) {
        board.tick();
      }
      assertThat(board.getActivePiece()).isEqualTo(new ActivePiece(Tetromino.O, 2, 18, 0));

      // Moving right into cols 4, 5 should be rejected
      assertThat(board.moveRight()).isFalse();
      assertThat(board.getActivePiece()).isEqualTo(new ActivePiece(Tetromino.O, 2, 18, 0));
    }
  }

  @Nested
  @DisplayName("3. Clockwise & Anticlockwise Rotation, Wall/Floor Kicks & Collision Rejection")
  class RotationAndWallKickTests {

    @Test
    @DisplayName(
        "rotateClockwise() and rotateAnticlockwise() should rotate piece in-place (offset 0, 0) when unobstructed")
    void shouldRotateClockwiseAndAnticlockwiseInPlaceWhenUnobstructed() {
      TetrisBoard board = new TetrisBoard(sequenceOf(Tetromino.T));

      assertThat(board.rotateClockwise()).isTrue();
      assertThat(board.getActivePiece()).isEqualTo(new ActivePiece(Tetromino.T, 3, 0, 1));

      assertThat(board.rotateAnticlockwise()).isTrue();
      assertThat(board.getActivePiece()).isEqualTo(new ActivePiece(Tetromino.T, 3, 0, 0));

      assertThat(board.rotateAnticlockwise()).isTrue();
      assertThat(board.getActivePiece()).isEqualTo(new ActivePiece(Tetromino.T, 3, 0, 3));
    }

    @Test
    @DisplayName(
        "Wall kick (-1, 0): rotating 2-wide T-piece (rotation 3) at right wall (x = 8) into 3-wide rotation 0 should kick left by 1 to x = 7")
    void shouldKickLeftBy1WhenRotatingTAgainstRightWall() {
      // T spawns at (3, 0), rotate anticlockwise once to rotation 3 (2 cells wide: cols x and x+1)
      TetrisBoard board = new TetrisBoard(sequenceOf(Tetromino.T));
      assertThat(board.rotateAnticlockwise()).isTrue();

      // Move all the way to right wall: x = 8 (occupies cols 8 and 9)
      while (board.moveRight()) {
        // shift to right wall
      }
      assertThat(board.getActivePiece()).isEqualTo(new ActivePiece(Tetromino.T, 8, 0, 3));

      // Rotating CW to rotation 0 (3 cells wide: cols x, x+1, x+2):
      // Offset (0, 0) at x = 8 needs cols 8, 9, 10 (col 10 out of bounds).
      // Offset (-1, 0) shifts x to 7 (cols 7, 8, 9 -> valid!)
      assertThat(board.rotateClockwise()).isTrue();
      assertThat(board.getActivePiece()).isEqualTo(new ActivePiece(Tetromino.T, 7, 0, 0));
    }

    @Test
    @DisplayName(
        "Wall kick (-2, 0): rotating vertical Tetromino.I at right wall (x = 8) into 4-wide horizontal should kick left by 2 to x = 6")
    void shouldKickLeftBy2WhenRotatingIPieceNearRightWall() {
      // I spawns at (3, 0), rotate CW once to rotation 1 (1x4 vertical bar)
      TetrisBoard board = new TetrisBoard(sequenceOf(Tetromino.I));
      assertThat(board.rotateClockwise()).isTrue();

      // Move right 5 times to x = 8 (1 column away from right wall)
      for (int i = 0; i < 5; i++) {
        assertThat(board.moveRight()).isTrue();
      }
      assertThat(board.getActivePiece()).isEqualTo(new ActivePiece(Tetromino.I, 8, 0, 1));

      // Rotating CW (or CCW) to horizontal (4 cells wide: cols x..x+3):
      // (0, 0) -> x = 8 (cols 8..11, invalid)
      // (-1, 0) -> x = 7 (cols 7..10, invalid)
      // (+1, 0) -> x = 9 (cols 9..12, invalid)
      // (-2, 0) -> x = 6 (cols 6..9, valid!)
      assertThat(board.rotateClockwise()).isTrue();
      assertThat(board.getActivePiece()).isEqualTo(new ActivePiece(Tetromino.I, 6, 0, 2));
    }

    @Test
    @DisplayName(
        "Wall kick (+1, 0): rotating when blocked on the left by a locked block should kick right by 1 column")
    void shouldKickRightBy1WhenBlockedOnLeftByLockedBlock() {
      // Piece 1: Drop vertical I-piece at col 0 (locks at x = 0, rows 16..19)
      TetrisBoard board = new TetrisBoard(sequenceOf(Tetromino.I, Tetromino.T, Tetromino.O));
      board.rotateClockwise();
      while (board.moveLeft()) {
        // move to col 0
      }
      board.hardDrop();
      assertThat(board.getLockedBlockAt(0, 18)).isEqualTo(Tetromino.I);

      // Use J at (0, 15), rotation 2, above a locked I in column 0, rows 16..19.
      // Rotating to 3 hits the I at (0, 17); the left kick hits the wall, so (+1, 0) wins.
      TetrisBoard boardWithJ = new TetrisBoard(sequenceOf(Tetromino.I, Tetromino.J, Tetromino.O));
      boardWithJ.rotateClockwise();
      while (boardWithJ.moveLeft()) {
        // move vertical I to col 0
      }
      boardWithJ.hardDrop(); // locks I at col 0, rows 16..19

      // J spawns at (3, 0) in rotation 0. Rotate twice to rotation 2, move left to x = 0, tick 15
      // times to y = 15
      boardWithJ.rotateClockwise();
      boardWithJ.rotateClockwise();
      while (boardWithJ.moveLeft()) {
        // move to x = 0
      }
      for (int i = 0; i < 15; i++) {
        boardWithJ.tick();
      }
      assertThat(boardWithJ.getActivePiece()).isEqualTo(new ActivePiece(Tetromino.J, 0, 15, 2));

      // Rotate CW to rotation 3 -> (0,0) hits locked (0, 17), (-1,0) hits left wall, (+1,0) kicks
      // to (1, 15)!
      assertThat(boardWithJ.rotateClockwise()).isTrue();
      assertThat(boardWithJ.getActivePiece()).isEqualTo(new ActivePiece(Tetromino.J, 1, 15, 3));
    }

    @Test
    @DisplayName(
        "Floor kick (0, -1): rotating 3x2 T-piece at floor (y = 18) into 2x3 orientation should kick upward by 1 row to y = 17")
    void shouldKickUpBy1WhenRotatingAtFloor() {
      // T spawns at (3, 0) in rotation 0 (3x2: occupies rows y and y+1)
      TetrisBoard board = new TetrisBoard(sequenceOf(Tetromino.T));

      // Move down 18 times -> y = 18 (occupies rows 18 and 19, touching the floor)
      for (int i = 0; i < 18; i++) {
        board.tick();
      }
      assertThat(board.getActivePiece()).isEqualTo(new ActivePiece(Tetromino.T, 3, 18, 0));

      // Rotating CW (or CCW) to 2x3 orientation occupies rows y, y+1, y+2.
      // At y = 18, y+2 = 20 (exceeds floor 19) for all horizontal offsets (0,0)..(+2,0).
      // Floor kick (0, -1) shifts y to 17 (rows 17, 18, 19 -> valid!)
      assertThat(board.rotateAnticlockwise()).isTrue();
      assertThat(board.getActivePiece()).isEqualTo(new ActivePiece(Tetromino.T, 3, 17, 3));
    }

    @Test
    @DisplayName(
        "rotateClockwise() and rotateAnticlockwise() should return false and leave piece unchanged when all 6 kick offsets collide")
    void shouldRejectRotationWhenAllKickOffsetsCollide() {
      // Horizontal I-piece (4x1) at the floor y = 19 occupies row 19.
      // Rotating to vertical (1x4) occupies rows y, y+1, y+2, y+3:
      // - At y = 19 (horizontal kicks), y+3 = 22 (exceeds floor 19).
      // - At y = 18 (floor kick 0, -1), y+3 = 21 (still exceeds floor 19!).
      // Therefore all 6 kick offsets collide with the floor.
      TetrisBoard board = new TetrisBoard(sequenceOf(Tetromino.I));

      for (int i = 0; i < 19; i++) {
        board.tick();
      }
      assertThat(board.getActivePiece()).isEqualTo(new ActivePiece(Tetromino.I, 3, 19, 0));

      assertThat(board.rotateClockwise()).isFalse();
      assertThat(board.getActivePiece()).isEqualTo(new ActivePiece(Tetromino.I, 3, 19, 0));

      assertThat(board.rotateAnticlockwise()).isFalse();
      assertThat(board.getActivePiece()).isEqualTo(new ActivePiece(Tetromino.I, 3, 19, 0));
    }
  }

  @Nested
  @DisplayName("4. Gravity (tick), Soft Drop, Hard Drop & Piece Locking")
  class GravityAndDropTests {

    @Test
    @DisplayName("tick() should advance active piece down by 1 row when space below is empty")
    void shouldAdvancePieceDownOnTick() {
      TetrisBoard board = new TetrisBoard(sequenceOf(Tetromino.T, Tetromino.O));

      board.tick();

      assertThat(board.getActivePiece()).isEqualTo(new ActivePiece(Tetromino.T, 3, 1, 0));
    }

    @Test
    @DisplayName(
        "tick() at floor should lock active piece into board matrix and spawn next preview piece")
    void shouldLockPieceAndSpawnNextWhenTickBlockedAtFloor() {
      // O spawns at (4, 0), occupies rows 0 and 1
      TetrisBoard board = new TetrisBoard(sequenceOf(Tetromino.O, Tetromino.T, Tetromino.L));

      // 18 ticks move O from y = 0 down to y = 18 (occupying rows 18 and 19)
      for (int i = 0; i < 18; i++) {
        board.tick();
      }
      assertThat(board.getActivePiece()).isEqualTo(new ActivePiece(Tetromino.O, 4, 18, 0));
      assertThat(board.getLockedBlockAt(4, 19)).isNull();

      // 19th tick cannot move down -> locks O at (4..5, 18..19) and spawns T at (3, 0)
      board.tick();

      assertThat(board.getLockedBlockAt(4, 18)).isEqualTo(Tetromino.O);
      assertThat(board.getLockedBlockAt(5, 18)).isEqualTo(Tetromino.O);
      assertThat(board.getLockedBlockAt(4, 19)).isEqualTo(Tetromino.O);
      assertThat(board.getLockedBlockAt(5, 19)).isEqualTo(Tetromino.O);

      assertThat(board.getActivePiece()).isEqualTo(new ActivePiece(Tetromino.T, 3, 0, 0));
      assertThat(board.getNextPiecePreview()).isEqualTo(Tetromino.L);
    }

    @Test
    @DisplayName("softDrop() should move down 1 row when free, and lock immediately when blocked")
    void shouldSoftDropAndLockWhenBlocked() {
      TetrisBoard board = new TetrisBoard(sequenceOf(Tetromino.O, Tetromino.I, Tetromino.Z));

      board.softDrop();
      assertThat(board.getActivePiece()).isEqualTo(new ActivePiece(Tetromino.O, 4, 1, 0));

      for (int i = 0; i < 17; i++) {
        board.softDrop();
      }
      assertThat(board.getActivePiece()).isEqualTo(new ActivePiece(Tetromino.O, 4, 18, 0));

      // Next softDrop locks O and spawns I
      board.softDrop();
      assertThat(board.getLockedBlockAt(4, 19)).isEqualTo(Tetromino.O);
      assertThat(board.getActivePiece()).isEqualTo(new ActivePiece(Tetromino.I, 3, 0, 0));
      assertThat(board.getNextPiecePreview()).isEqualTo(Tetromino.Z);
    }

    @Test
    @DisplayName(
        "hardDrop() should drop piece instantly to lowest valid position, lock it, and spawn next piece")
    void shouldHardDropToFloorAndStackOnLockedPieces() {
      TetrisBoard board =
          new TetrisBoard(sequenceOf(Tetromino.O, Tetromino.O, Tetromino.T, Tetromino.L));

      // First O hard-drops to rows 18, 19 (cols 4, 5)
      board.hardDrop();
      assertThat(board.getLockedBlockAt(4, 18)).isEqualTo(Tetromino.O);
      assertThat(board.getLockedBlockAt(4, 19)).isEqualTo(Tetromino.O);
      assertThat(board.getActivePiece()).isEqualTo(new ActivePiece(Tetromino.O, 4, 0, 0));

      // Second O hard-drops onto first O -> locks at rows 16, 17 (cols 4, 5)
      board.hardDrop();
      assertThat(board.getLockedBlockAt(4, 16)).isEqualTo(Tetromino.O);
      assertThat(board.getLockedBlockAt(5, 16)).isEqualTo(Tetromino.O);
      assertThat(board.getLockedBlockAt(4, 17)).isEqualTo(Tetromino.O);
      assertThat(board.getLockedBlockAt(5, 17)).isEqualTo(Tetromino.O);

      assertThat(board.getActivePiece()).isEqualTo(new ActivePiece(Tetromino.T, 3, 0, 0));
      assertThat(board.getNextPiecePreview()).isEqualTo(Tetromino.L);
    }
  }

  @Nested
  @DisplayName("5. Line Clearing, Row Shifting & Fixed Scoring (100 / 300 / 500 / 800)")
  class LineClearingAndScoringTests {

    /**
     * Helper that drops 5 O-pieces side by side across a 10-column board (cols 0..1, 2..3, 4..5,
     * 6..7, 8..9), completing 2 full horizontal lines simultaneously.
     */
    private void dropFiveOPiecesAcrossRow(TetrisBoard board) {
      // Piece 1: cols 0, 1 (move left 4 times from x=4)
      board.moveLeft();
      board.moveLeft();
      board.moveLeft();
      board.moveLeft();
      board.hardDrop();

      // Piece 2: cols 2, 3 (move left 2 times from x=4)
      board.moveLeft();
      board.moveLeft();
      board.hardDrop();

      // Piece 3: cols 4, 5 (center)
      board.hardDrop();

      // Piece 4: cols 6, 7 (move right 2 times from x=4)
      board.moveRight();
      board.moveRight();
      board.hardDrop();

      // Piece 5: cols 8, 9 (move right 4 times from x=4)
      board.moveRight();
      board.moveRight();
      board.moveRight();
      board.moveRight();
      board.hardDrop();
    }

    @Test
    @DisplayName(
        "Clearing 1 line should award 100 points, increment linesCleared by 1, and shift upper blocks down")
    void shouldClearSingleLineAward100PointsAndShiftRowsDown() {
      TetrisBoard board = new TetrisBoard(4, 20, sequenceOf(Tetromino.O, Tetromino.I, Tetromino.T));

      // Piece 1: O spawns at x = (4 - 2) / 2 = 1. Move left to x = 0 (cols 0, 1; rows 18, 19)
      board.moveLeft();
      board.hardDrop();
      assertThat(board.getLockedBlockAt(0, 18)).isEqualTo(Tetromino.O);
      assertThat(board.getLockedBlockAt(0, 19)).isEqualTo(Tetromino.O);

      // Piece 2: I spawns at x = (4 - 4) / 2 = 0. Hard-drop lands on top of O at row 17 (cols
      // 0..3)
      board.hardDrop();

      // Row 17 was completely filled by I and cleared; rows 18 and 19 still have O at cols 0, 1
      assertThat(board.getLinesCleared()).isEqualTo(1);
      assertThat(board.getScore()).isEqualTo(100);
      assertThat(board.getLockedBlockAt(0, 17)).isNull();
      assertThat(board.getLockedBlockAt(0, 18)).isEqualTo(Tetromino.O);
      assertThat(board.getLockedBlockAt(0, 19)).isEqualTo(Tetromino.O);
    }

    @Test
    @DisplayName("Clearing a bottom row should shift blocks above it down by 1 row")
    void shouldShiftUpperRowsDownWhenBottomRowCleared() {
      TetrisBoard board = new TetrisBoard(4, 20, sequenceOf(Tetromino.L, Tetromino.I, Tetromino.O));

      // Piece 1: L spawns at x = (4 - 3) / 2 = 0. Hard-drop places (2, 18) and (0..2, 19)
      board.hardDrop();
      assertThat(board.getLockedBlockAt(2, 18)).isEqualTo(Tetromino.L);
      assertThat(board.getLockedBlockAt(0, 19)).isEqualTo(Tetromino.L);

      // Piece 2: I spawns at x = 0. Rotate CW once (vertical 1x4 at col x = 0), move right 3 times
      // to col 3
      board.rotateClockwise();
      board.moveRight();
      board.moveRight();
      board.moveRight();
      board.hardDrop();

      // Row 19 cleared -> 1 line, 100 points
      assertThat(board.getLinesCleared()).isEqualTo(1);
      assertThat(board.getScore()).isEqualTo(100);

      // (2, 18) from L shifted down to (2, 19); (3, 16..18) from I shifted down to (3, 17..19)
      assertThat(board.getLockedBlockAt(2, 19)).isEqualTo(Tetromino.L);
      assertThat(board.getLockedBlockAt(0, 19)).isNull();
      assertThat(board.getLockedBlockAt(1, 19)).isNull();
      assertThat(board.getLockedBlockAt(3, 19)).isEqualTo(Tetromino.I);
      assertThat(board.getLockedBlockAt(3, 18)).isEqualTo(Tetromino.I);
      assertThat(board.getLockedBlockAt(3, 17)).isEqualTo(Tetromino.I);
      assertThat(board.getLockedBlockAt(3, 16)).isNull();
    }

    @Test
    @DisplayName("Clearing 2 lines simultaneously should award 300 points and increment lines by 2")
    void shouldClearDoubleLinesAndAward300Points() {
      TetrisBoard board = new TetrisBoard(sequenceOf(Tetromino.O));

      dropFiveOPiecesAcrossRow(board);

      assertThat(board.getLinesCleared()).isEqualTo(2);
      assertThat(board.getScore()).isEqualTo(300);
      for (int x = 0; x < 10; x++) {
        assertThat(board.getLockedBlockAt(x, 19)).isNull();
        assertThat(board.getLockedBlockAt(x, 18)).isNull();
      }
    }

    @Test
    @DisplayName("Clearing 3 lines simultaneously should award 500 points and increment lines by 3")
    void shouldClearTripleLinesAndAward500Points() {
      TetrisBoard board =
          new TetrisBoard(
              4, 20, sequenceOf(Tetromino.O, Tetromino.L, Tetromino.I, Tetromino.I, Tetromino.O));

      // 1. O spawns at x = 1 -> move left to x = 0 (cols 0, 1; rows 18, 19)
      board.moveLeft();
      board.hardDrop();

      // 2. L spawns at x = 0 -> rotate anticlockwise once (rot 3: (0,0), (1,0), (1,1), (1,2)),
      // move right to x = 1, hardDrop -> locks at (1, 17), (2, 17), (2, 18), (2, 19)
      board.rotateAnticlockwise();
      board.moveRight();
      board.hardDrop();

      // 3. I spawns at x = 0 -> rotate CW once (vertical 1x4 at col x = 0), hardDrop -> locks at
      // col 0, rows 14..17
      board.rotateClockwise();
      board.hardDrop();

      assertThat(board.getLinesCleared()).isZero();
      assertThat(board.getScore()).isZero();

      // 4. I spawns at x = 0 -> rotate CW once (vertical 1x4 at col 0), move right 3 times to col
      // 3, hardDrop -> fills (3, 16..19), completing rows 17, 18, 19!
      board.rotateClockwise();
      board.moveRight();
      board.moveRight();
      board.moveRight();
      board.hardDrop();

      assertThat(board.getLinesCleared()).isEqualTo(3);
      assertThat(board.getScore()).isEqualTo(500);
    }

    @Test
    @DisplayName(
        "Clearing 4 lines (Tetris) simultaneously should award 800 points and increment lines by 4")
    void shouldClearFourLinesTetrisAndAward800Points() {
      // On a 4-column board, drop 4 vertical I-pieces into columns 0, 1, 2, and 3!
      TetrisBoard board =
          new TetrisBoard(
              4, 20, sequenceOf(Tetromino.I, Tetromino.I, Tetromino.I, Tetromino.I, Tetromino.O));

      // Piece 1: col 0 (rot 1 at x = 0)
      board.rotateClockwise();
      board.hardDrop();

      // Piece 2: col 1 (rot 1, move right once to x = 1)
      board.rotateClockwise();
      board.moveRight();
      board.hardDrop();

      // Piece 3: col 2 (rot 1, move right twice to x = 2)
      board.rotateClockwise();
      board.moveRight();
      board.moveRight();
      board.hardDrop();

      assertThat(board.getLinesCleared()).isZero();
      assertThat(board.getScore()).isZero();

      // Piece 4: col 3 (rot 1, move right 3 times to x = 3)
      board.rotateClockwise();
      board.moveRight();
      board.moveRight();
      board.moveRight();
      board.hardDrop();

      assertThat(board.getLinesCleared()).isEqualTo(4);
      assertThat(board.getScore()).isEqualTo(800);

      for (int y = 16; y <= 19; y++) {
        for (int x = 0; x < 4; x++) {
          assertThat(board.getLockedBlockAt(x, y)).isNull();
        }
      }
    }
  }

  @Nested
  @DisplayName("6. Game Over Detection, Input Lockout & Board Reset")
  class GameOverAndResetTests {

    @Test
    @DisplayName(
        "Spawning a piece that immediately overlaps a locked block should trigger isGameOver() = true")
    void shouldTriggerGameOverWhenSpawnedPieceOverlapsLockedBlocks() {
      // Stack 10 O-pieces at the center (cols 4, 5): each O-piece is 2 rows tall, filling rows
      // 19..0
      TetrisBoard board = new TetrisBoard(sequenceOf(Tetromino.O));

      for (int i = 0; i < 9; i++) {
        board.hardDrop();
        assertThat(board.isGameOver())
            .as("Should not be game over after %d dropped O-pieces", i + 1)
            .isFalse();
      }

      // 10th O-piece locks at rows 0 and 1; the 11th O-piece spawns at (4, 0) overlapping (4, 0)
      board.hardDrop();

      assertThat(board.isGameOver()).isTrue();
    }

    @Test
    @DisplayName(
        "When isGameOver() is true, all movement, rotation, drop, and tick calls should be ignored")
    void shouldIgnoreAllPlayerActionsAndTicksWhenGameOver() {
      TetrisBoard board = new TetrisBoard(sequenceOf(Tetromino.O));
      for (int i = 0; i < 10; i++) {
        board.hardDrop();
      }
      assertThat(board.isGameOver()).isTrue();

      ActivePiece pieceAtGameOver = board.getActivePiece();

      assertThat(board.moveLeft()).isFalse();
      assertThat(board.moveRight()).isFalse();
      assertThat(board.rotateClockwise()).isFalse();
      assertThat(board.rotateAnticlockwise()).isFalse();
      board.tick();
      board.softDrop();
      board.hardDrop();

      assertThat(board.isGameOver()).isTrue();
      assertThat(board.getActivePiece()).isEqualTo(pieceAtGameOver);
    }

    @Test
    @DisplayName(
        "reset() should clear all locked blocks, reset score/linesCleared/gameOver, and spawn a fresh piece")
    void shouldResetBoardToCleanInitialState() {
      TetrisBoard board = new TetrisBoard(4, 20, sequenceOf(Tetromino.I, Tetromino.O));
      // Clear 1 line with horizontal I-piece to accumulate score and lines
      board.hardDrop();
      // Drop O-piece to leave locked blocks on the board
      board.hardDrop();

      assertThat(board.getScore()).isEqualTo(100);
      assertThat(board.getLinesCleared()).isEqualTo(1);
      assertThat(board.getLockedBlockAt(1, 19)).isEqualTo(Tetromino.O);

      board.reset();

      assertThat(board.getScore()).isZero();
      assertThat(board.getLinesCleared()).isZero();
      assertThat(board.isGameOver()).isFalse();
      assertThat(board.getActivePiece()).isEqualTo(new ActivePiece(Tetromino.O, 1, 0, 0));

      for (int y = 0; y < board.getHeight(); y++) {
        for (int x = 0; x < board.getWidth(); x++) {
          assertThat(board.getLockedBlockAt(x, y)).isNull();
        }
      }
    }
  }
}
