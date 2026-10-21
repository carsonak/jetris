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
      // 2x2 O piece on width 8 spawns at x = (8 - 2) / 2 = 3, y = 0
      assertThat(board.getActivePiece()).isEqualTo(new ActivePiece(Tetromino.O, 3, 0, 0));
      assertThat(board.getNextPiecePreview()).isEqualTo(Tetromino.T);
    }

    @Test
    @DisplayName(
        "3x3 Tetromino (e.g. T) should spawn at top-center x = (10 - 3) / 2 = 3, y = 0, rotation 0")
    void shouldSpawn3x3PieceAtTopCenterAndPopulatePreview() {
      TetrisBoard board = new TetrisBoard(sequenceOf(Tetromino.T, Tetromino.L));

      assertThat(board.getActivePiece()).isEqualTo(new ActivePiece(Tetromino.T, 3, 0, 0));
      assertThat(board.getNextPiecePreview()).isEqualTo(Tetromino.L);
    }

    @Test
    @DisplayName(
        "4x4 Tetromino (I) should spawn at top-center x = (10 - 4) / 2 = 3, y = 0, rotation 0")
    void shouldSpawn4x4PieceAtTopCenter() {
      TetrisBoard board = new TetrisBoard(sequenceOf(Tetromino.I, Tetromino.S));

      assertThat(board.getActivePiece()).isEqualTo(new ActivePiece(Tetromino.I, 3, 0, 0));
      assertThat(board.getNextPiecePreview()).isEqualTo(Tetromino.S);
    }

    @Test
    @DisplayName(
        "2x2 Tetromino (O) should spawn at top-center x = (10 - 2) / 2 = 4, y = 0, rotation 0")
    void shouldSpawn2x2PieceAtTopCenter() {
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
    @DisplayName("moveRight() should stop at right wall (x = width - 1) and return false")
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
        "Tetromino.I in vertical rotation 3 should allow negative origin x = -1 so solid column aligns with left wall x = 0")
    void shouldAllowNegativeBoundingBoxOriginWhenSolidCellsRemainInBounds() {
      // I spawns at x = 3, y = 0
      TetrisBoard board = new TetrisBoard(sequenceOf(Tetromino.I));
      // Rotate 3 times to rotationIndex = 3 (solid cells at col x + 1)
      assertThat(board.rotateClockwise()).isTrue();
      assertThat(board.rotateClockwise()).isTrue();
      assertThat(board.rotateClockwise()).isTrue();

      assertThat(board.moveLeft()).isTrue(); // x = 2 (solid col 3)
      assertThat(board.moveLeft()).isTrue(); // x = 1 (solid col 2)
      assertThat(board.moveLeft()).isTrue(); // x = 0 (solid col 1)
      assertThat(board.moveLeft()).isTrue(); // x = -1 (solid col 0)

      assertThat(board.getActivePiece().x()).isEqualTo(-1);
      // Moving left once more would place solid cells at col -1 (out of bounds)
      assertThat(board.moveLeft()).isFalse();
      assertThat(board.getActivePiece().x()).isEqualTo(-1);
    }

    @Test
    @DisplayName("moveLeft() and moveRight() should be blocked by existing locked blocks")
    void shouldRejectHorizontalMoveIntoLockedBlocks() {
      // First piece O drops at center (cols 4, 5 -> rows 18, 19)
      TetrisBoard board = new TetrisBoard(sequenceOf(Tetromino.O, Tetromino.O, Tetromino.O));
      board.hardDrop();

      // Second piece O moves left to cols 2, 3, then drops almost to the bottom (row 18, 19)
      board.moveLeft();
      board.moveLeft();
      for (int i = 0; i < 18; i++) {
        board.tick();
      }
      // Second O is now at x = 2, y = 18 (occupying cols 2, 3 at rows 18, 19), adjacent to locked O
      // at cols 4, 5
      assertThat(board.getActivePiece()).isEqualTo(new ActivePiece(Tetromino.O, 2, 18, 0));

      // Moving right into cols 4, 5 should be rejected
      assertThat(board.moveRight()).isFalse();
      assertThat(board.getActivePiece()).isEqualTo(new ActivePiece(Tetromino.O, 2, 18, 0));
    }
  }

  @Nested
  @DisplayName("3. Rotation, Wall/Floor Kicks & Collision Rejection")
  class RotationAndWallKickTests {

    @Test
    @DisplayName("rotateClockwise() should rotate piece in-place (offset 0, 0) when unobstructed")
    void shouldRotateClockwiseInPlaceWhenUnobstructed() {
      TetrisBoard board = new TetrisBoard(sequenceOf(Tetromino.T));

      boolean rotated = board.rotateClockwise();

      assertThat(rotated).isTrue();
      assertThat(board.getActivePiece()).isEqualTo(new ActivePiece(Tetromino.T, 3, 0, 1));
    }

    @Test
    @DisplayName(
        "Wall kick (+1, 0): rotating against left wall should shift piece right by 1 column")
    void shouldKickRightBy1WhenRotatingAgainstLeftWall() {
      // I spawns at (3, 0), rotate 3 times to vertical rotation 3 (solid col at x + 1)
      TetrisBoard board = new TetrisBoard(sequenceOf(Tetromino.I));
      board.rotateClockwise();
      board.rotateClockwise();
      board.rotateClockwise();

      // Move all the way to the left wall: x = -1 (solid cells at col 0)
      while (board.moveLeft()) {
        // shift to left wall
      }
      assertThat(board.getActivePiece()).isEqualTo(new ActivePiece(Tetromino.I, -1, 0, 3));

      // Rotating CW to rotation 0 at x = -1 would put horizontal bar at cols -1..2 (invalid).
      // Offset (-1, 0) -> x = -2 (invalid).
      // Offset (+1, 0) -> x = 0 (cols 0..3, valid!)
      assertThat(board.rotateClockwise()).isTrue();
      assertThat(board.getActivePiece()).isEqualTo(new ActivePiece(Tetromino.I, 0, 0, 0));
    }

    @Test
    @DisplayName(
        "Wall kick (-1, 0): rotating T-piece against right wall should shift piece left by 1 column")
    void shouldKickLeftBy1WhenRotatingAgainstRightWall() {
      // T spawns at (3, 0), rotate 3 times to rotation 3 (pointing left: solid cols x+0 and x+1)
      TetrisBoard board = new TetrisBoard(sequenceOf(Tetromino.T));
      board.rotateClockwise();
      board.rotateClockwise();
      board.rotateClockwise();

      // Move all the way to right wall: x = 8 (solid cols 8 and 9)
      while (board.moveRight()) {
        // shift to right wall
      }
      assertThat(board.getActivePiece()).isEqualTo(new ActivePiece(Tetromino.T, 8, 0, 3));

      // Rotating CW to rotation 0 at x = 8 would need cols 8, 9, 10 (col 10 out of bounds).
      // Offset (-1, 0) shifts x to 7 (cols 7, 8, 9 -> valid!)
      assertThat(board.rotateClockwise()).isTrue();
      assertThat(board.getActivePiece()).isEqualTo(new ActivePiece(Tetromino.T, 7, 0, 0));
    }

    @Test
    @DisplayName(
        "Wall kick (-2, 0): rotating vertical Tetromino.I (rotation 1) against right wall should kick left by 2 columns")
    void shouldKickLeftBy2WhenRotatingIPieceAgainstRightWall() {
      // I spawns at (3, 0), rotate once to rotation 1 (solid col at x + 2)
      TetrisBoard board = new TetrisBoard(sequenceOf(Tetromino.I));
      board.rotateClockwise();

      // Move all the way to right wall: x = 7 (solid col at 7 + 2 = 9)
      while (board.moveRight()) {
        // shift to right wall
      }
      assertThat(board.getActivePiece()).isEqualTo(new ActivePiece(Tetromino.I, 7, 0, 1));

      // Rotating CW to rotation 2 (horizontal bar at cols x..x+3):
      // (0, 0) -> x = 7 (cols 7..10, invalid)
      // (-1, 0) -> x = 6 (cols 6..9, valid!) -> Wait: for rotation 3 (solid col x + 1) at right
      // wall x = 8 (solid col 9), rotating CW to rotation 0 needs cols x..x+3:
      // At x = 8: (0,0)->x=8 (cols 8..11), (-1,0)->x=7 (cols 7..10), (+1,0)->x=9, (-2,0)->x=6
      // (cols 6..9, valid!)
      // Let's test from rotation 3 at right wall x = 8!
      board.rotateClockwise(); // rot 2 (kicks to x = 6)
      board.rotateClockwise(); // rot 3 at x = 6
      while (board.moveRight()) {
        // shift rot 3 to right wall: x = 8 (solid col at 8 + 1 = 9)
      }
      assertThat(board.getActivePiece()).isEqualTo(new ActivePiece(Tetromino.I, 8, 0, 3));

      // Rotating CW from rot 3 to rot 0 at x = 8 requires (-2, 0) kick to x = 6!
      assertThat(board.rotateClockwise()).isTrue();
      assertThat(board.getActivePiece()).isEqualTo(new ActivePiece(Tetromino.I, 6, 0, 0));
    }

    @Test
    @DisplayName(
        "Floor kick (0, -1): rotating horizontal T-piece at floor should kick upward by 1 row")
    void shouldKickUpBy1WhenRotatingAtFloor() {
      // T spawns at (3, 0) in rotation 0 (occupies rows y+0 and y+1)
      TetrisBoard board = new TetrisBoard(sequenceOf(Tetromino.T));

      // Move down 18 times -> y = 18 (occupies rows 18 and 19, touching the floor)
      for (int i = 0; i < 18; i++) {
        board.tick();
      }
      assertThat(board.getActivePiece()).isEqualTo(new ActivePiece(Tetromino.T, 3, 18, 0));

      // Rotating CW to rotation 1 occupies rows y, y+1, y+2.
      // At y = 18, y+2 = 20 (out of bounds below floor 19), and horizontal kicks (-1..+2, 0) also
      // have y+2 = 20.
      // Floor kick (0, -1) shifts y to 17 (rows 17, 18, 19 -> valid!)
      assertThat(board.rotateClockwise()).isTrue();
      assertThat(board.getActivePiece()).isEqualTo(new ActivePiece(Tetromino.T, 3, 17, 1));
    }

    @Test
    @DisplayName(
        "rotateClockwise() should return false and leave piece unchanged when all 6 kick offsets collide")
    void shouldRejectRotationWhenAllKickOffsetsCollide() {
      // On a 4-column board, horizontal I-piece is 4 cells wide (cols 0..3).
      // If we lock blocks such that vertical I-piece (4 cells tall) cannot fit anywhere within
      // offsets (0,0), (-1,0), (+1,0), (-2,0), (+2,0), (0,-1):
      // Even simpler: at the floor (y = 18) of a 10-column board, horizontal I-piece in rotation 0
      // has its bar at row 19 (y + 1).
      // Rotating CW to rotation 1 puts its vertical bar at rows y, y+1, y+2, y+3.
      // At y = 18:
      // - Horizontal offsets (0,0), (-1,0), (+1,0), (-2,0), (+2,0) all have y+3 = 21 (exceeds
      //   floor 19!)
      // - Floor kick (0, -1) has y = 17, so y+3 = 20 (STILL exceeds floor 19!)
      // Therefore, ALL 6 offsets collide with the floor!
      TetrisBoard board = new TetrisBoard(sequenceOf(Tetromino.I));

      for (int i = 0; i < 18; i++) {
        board.tick();
      }
      assertThat(board.getActivePiece()).isEqualTo(new ActivePiece(Tetromino.I, 3, 18, 0));

      assertThat(board.rotateClockwise()).isFalse();
      assertThat(board.getActivePiece()).isEqualTo(new ActivePiece(Tetromino.I, 3, 18, 0));
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
  @DisplayName("5. Line Clearing, Row Shifting & Dynamic Width-Aware Scoring")
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
        "Clearing 1 line on width=4 board should award round((1000/4) * 1^1.5) = 250 points and shift upper blocks down")
    void shouldClearSingleLineAwardWidthAdjustedPointsAndShiftRowsDown() {
      // On a 4-column board, base constant is 1000.0 / 4 = 250.0.
      // First drop an O-piece on the left (cols 0, 1 at rows 18, 19), then drop a horizontal
      // I-piece on top (row 17, cols 0..3) to clear row 17.
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
      assertThat(board.getScore()).isEqualTo(250);
      assertThat(board.getLockedBlockAt(0, 17)).isNull();
      assertThat(board.getLockedBlockAt(0, 18)).isEqualTo(Tetromino.O);
      assertThat(board.getLockedBlockAt(0, 19)).isEqualTo(Tetromino.O);
    }

    @Test
    @DisplayName(
        "Wider boards should have a lower base constant: clearing 1 line on width=20 awards round((1000/20) * 1^1.5) = 50 points")
    void widerBoardsShouldAwardLowerConstantPerLineClear() {
      // On a 20-column board, 5 horizontal I-pieces (each 4 wide) across the bottom clear 1 line.
      // Base constant = 1000.0 / 20 = 50 points.
      TetrisBoard board = new TetrisBoard(20, 20, sequenceOf(Tetromino.I));

      // I spawns at x = (20 - 4) / 2 = 8 on a 20-wide board:
      // 1) cols 0..3 (move left 8 times from x = 8)
      for (int i = 0; i < 8; i++) {
        board.moveLeft();
      }
      board.hardDrop();

      // 2) cols 4..7 (move left 4 times from x = 8)
      for (int i = 0; i < 4; i++) {
        board.moveLeft();
      }
      board.hardDrop();

      // 3) cols 8..11 (center x = 8)
      board.hardDrop();

      // 4) cols 12..15 (move right 4 times from x = 8)
      for (int i = 0; i < 4; i++) {
        board.moveRight();
      }
      board.hardDrop();

      // 5) cols 16..19 (move right 8 times from x = 8)
      for (int i = 0; i < 8; i++) {
        board.moveRight();
      }
      board.hardDrop();

      assertThat(board.getLinesCleared()).isEqualTo(1);
      assertThat(board.getScore()).isEqualTo(50);
    }

    @Test
    @DisplayName("Clearing a bottom row should shift blocks above it down by 1 row")
    void shouldShiftUpperRowsDownWhenBottomRowCleared() {
      TetrisBoard board = new TetrisBoard(4, 20, sequenceOf(Tetromino.L, Tetromino.I, Tetromino.O));

      // Piece 1: L spawns at x = (4 - 3) / 2 = 0. Hard-drop places (2, 18) and (0..2, 19)
      board.hardDrop();
      assertThat(board.getLockedBlockAt(2, 18)).isEqualTo(Tetromino.L);
      assertThat(board.getLockedBlockAt(0, 19)).isEqualTo(Tetromino.L);

      // Piece 2: I spawns at x = 0. Rotate CW once (solid at col x + 2 = 2), move right once (x =
      // 1 -> solid at col 3)
      board.rotateClockwise();
      board.moveRight();
      board.hardDrop();

      // Row 19 cleared on width=4 -> 1 line, round((1000.0 / 4) * 1^1.5) = 250 points
      assertThat(board.getLinesCleared()).isEqualTo(1);
      assertThat(board.getScore()).isEqualTo(250);

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
    @DisplayName(
        "Clearing 2 lines on standard width=10 board should award round((1000/10) * 2^1.5) = 283 points")
    void shouldClearDoubleLinesAndAwardFormulaPoints() {
      TetrisBoard board = new TetrisBoard(sequenceOf(Tetromino.O));

      dropFiveOPiecesAcrossRow(board);

      assertThat(board.getLinesCleared()).isEqualTo(2);
      assertThat(board.getScore()).isEqualTo(283);
      for (int x = 0; x < 10; x++) {
        assertThat(board.getLockedBlockAt(x, 19)).isNull();
        assertThat(board.getLockedBlockAt(x, 18)).isNull();
      }
    }

    @Test
    @DisplayName(
        "Clearing 3 lines on width=4 board should award round((1000/4) * 3^1.5) = 1299 points")
    void shouldClearTripleLinesAndAwardFormulaPoints() {
      TetrisBoard board =
          new TetrisBoard(
              4, 20, sequenceOf(Tetromino.O, Tetromino.L, Tetromino.I, Tetromino.I, Tetromino.O));

      // 1. O spawns at x = 1 -> move left to x = 0 (cols 0, 1; rows 18, 19)
      board.moveLeft();
      board.hardDrop();

      // 2. L spawns at x = 0 -> rotate 3 times (rot 3), move right to x = 1, hardDrop
      board.rotateClockwise();
      board.rotateClockwise();
      board.rotateClockwise();
      board.moveRight();
      board.hardDrop();

      // 3. I spawns at x = 0 -> rotate 3 times (rot 3: solid col at x + 1), move left to x = -1
      // (solid col 0), hardDrop
      board.rotateClockwise();
      board.rotateClockwise();
      board.rotateClockwise();
      board.moveLeft();
      board.hardDrop();

      assertThat(board.getLinesCleared()).isZero();
      assertThat(board.getScore()).isZero();

      // 4. I spawns at x = 0 -> rotate once (rot 1: solid col at x + 2), move right to x = 1
      // (solid col 3), hardDrop
      board.rotateClockwise();
      board.moveRight();
      board.hardDrop();

      assertThat(board.getLinesCleared()).isEqualTo(3);
      // (int) Math.round((1000.0 / 4) * Math.pow(3, 1.5)) == 1299
      assertThat(board.getScore()).isEqualTo(1299);
    }

    @Test
    @DisplayName(
        "Clearing 4 lines (Tetris) on width=4 board should award round((1000/4) * 4^1.5) = 2000 points")
    void shouldClearFourLinesTetrisAndAwardFormulaPoints() {
      // On a 4-column board, drop 4 vertical I-pieces into columns 0, 1, 2, and 3!
      TetrisBoard board =
          new TetrisBoard(
              4, 20, sequenceOf(Tetromino.I, Tetromino.I, Tetromino.I, Tetromino.I, Tetromino.O));

      // Piece 1: col 0 (rot 3 -> solid at x + 1, move left to x = -1 -> col 0)
      board.rotateClockwise();
      board.rotateClockwise();
      board.rotateClockwise();
      board.moveLeft();
      board.hardDrop();

      // Piece 2: col 1 (rot 3 -> solid at x + 1, at x = 0 -> col 1)
      board.rotateClockwise();
      board.rotateClockwise();
      board.rotateClockwise();
      board.hardDrop();

      // Piece 3: col 2 (rot 1 -> solid at x + 2, at x = 0 -> col 2)
      board.rotateClockwise();
      board.hardDrop();

      assertThat(board.getLinesCleared()).isZero();
      assertThat(board.getScore()).isZero();

      // Piece 4: col 3 (rot 1 -> solid at x + 2, move right to x = 1 -> col 3)
      board.rotateClockwise();
      board.moveRight();
      board.hardDrop();

      assertThat(board.getLinesCleared()).isEqualTo(4);
      // (int) Math.round((1000.0 / 4) * Math.pow(4, 1.5)) == 2000
      assertThat(board.getScore()).isEqualTo(2000);

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

      assertThat(board.getScore()).isEqualTo(250);
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
