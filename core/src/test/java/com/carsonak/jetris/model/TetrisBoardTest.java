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
  @DisplayName("3. Rotation & Collision Rejection (No Wall Kicks)")
  class RotationCollisionTests {

    @Test
    @DisplayName("rotateClockwise() should rotate piece by 90° CW when unobstructed")
    void shouldRotateClockwiseWhenUnobstructed() {
      TetrisBoard board = new TetrisBoard(sequenceOf(Tetromino.T));

      boolean rotated = board.rotateClockwise();

      assertThat(rotated).isTrue();
      assertThat(board.getActivePiece()).isEqualTo(new ActivePiece(Tetromino.T, 3, 0, 1));
    }

    @Test
    @DisplayName(
        "rotateClockwise() should return false and leave piece unchanged when rotation would cross left wall")
    void shouldRejectRotationAgainstLeftWall() {
      // I spawns at (3, 0), rotate to vertical rotation 3 (solid col at x + 1)
      TetrisBoard board = new TetrisBoard(sequenceOf(Tetromino.I));
      board.rotateClockwise();
      board.rotateClockwise();
      board.rotateClockwise();

      // Move all the way to the left wall: x = -1 (solid cells at col 0)
      while (board.moveLeft()) {
        // shift to left wall
      }
      assertThat(board.getActivePiece()).isEqualTo(new ActivePiece(Tetromino.I, -1, 0, 3));

      // Rotating CW to rotation 0 would put horizontal bar at cols -1, 0, 1, 2 (col -1 is out of
      // bounds)
      assertThat(board.rotateClockwise()).isFalse();
      assertThat(board.getActivePiece()).isEqualTo(new ActivePiece(Tetromino.I, -1, 0, 3));
    }

    @Test
    @DisplayName(
        "rotateClockwise() should return false and leave piece unchanged when rotation would cross floor")
    void shouldRejectRotationThroughFloor() {
      // I spawns at (3, 0) horizontal (solid row y + 1)
      TetrisBoard board = new TetrisBoard(sequenceOf(Tetromino.I));

      // Move down 18 times -> y = 18, solid horizontal bar is on floor row 19
      for (int i = 0; i < 18; i++) {
        board.tick();
      }
      assertThat(board.getActivePiece()).isEqualTo(new ActivePiece(Tetromino.I, 3, 18, 0));

      // Rotating CW to rotation 1 would place vertical bar at rows 18, 19, 20, 21 (exceeds floor
      // 19)
      assertThat(board.rotateClockwise()).isFalse();
      assertThat(board.getActivePiece()).isEqualTo(new ActivePiece(Tetromino.I, 3, 18, 0));
    }

    @Test
    @DisplayName(
        "rotateClockwise() should return false when rotated cells would overlap locked blocks")
    void shouldRejectRotationIntoLockedBlock() {
      // Piece 1: O dropped at center (cols 4, 5; rows 18, 19)
      TetrisBoard board = new TetrisBoard(sequenceOf(Tetromino.O, Tetromino.I, Tetromino.T));
      board.hardDrop();

      // Piece 2: I rotated to vertical rotation 3 (solid col at x + 1 = col 3 when x = 2)
      board.rotateClockwise();
      board.rotateClockwise();
      board.rotateClockwise();
      board.moveLeft(); // x = 2 -> solid col is 3, rows 0..3

      // Move down 16 rows -> y = 16, solid cells at col 3, rows 16, 17, 18, 19
      for (int i = 0; i < 16; i++) {
        board.tick();
      }
      assertThat(board.getActivePiece()).isEqualTo(new ActivePiece(Tetromino.I, 2, 16, 3));

      // Rotating CW to rotation 0 at (2, 16) would place cells at row 17, cols 2..5 (free),
      // Wait: locked O is at rows 18, 19 (cols 4, 5). Move 1 more row down to y = 17?
      // At y = 16, row 18 is y + 2 (Rotation 2). Let's test rotating from rotation 1 at x = 1, y =
      // 16:
      // Actually at y = 17, rotation 3 has cells at y=17, 18, 19, 20 (invalid).
      // Instead, if piece is at x = 2, y = 16, rotation 1 (col 4, rows 16..19) overlaps (4, 18) and
      // (4, 19)!
      // From rotation 3 -> 0 (row 17, cols 2..5) is free, then 0 -> 1 (col 4, rows 16..19) hits
      // locked O at (4, 18)!
      assertThat(board.rotateClockwise()).isTrue(); // rotation 0 (row 17, cols 2..5)
      assertThat(board.rotateClockwise()).isFalse(); // rotation 1 blocked by (4, 18) and (4, 19)
      assertThat(board.getActivePiece()).isEqualTo(new ActivePiece(Tetromino.I, 2, 16, 0));
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
  @DisplayName("5. Line Clearing, Row Shifting & Scoring")
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
      // Use a 4-column board so a single horizontal I-piece (width 4) clears 1 line!
      // First drop an O-piece on the left (cols 0, 1 at rows 18, 19), then drop a horizontal
      // I-piece
      // on top (row 17, cols 0..3) -> Wait: horizontal I-piece fills all 4 columns of row 17!
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
      // On a 4-column board:
      // 1. Drop L (rotation 0: (col 2, row 0) and (cols 0, 1, 2, row 1)) at x = 0 ->
      //    occupies (2, 18) and (0, 19), (1, 19), (2, 19).
      // 2. Drop vertical I (rotation 1: col 2, rows 0..3) shifted right to x = 1 (col 3) ->
      //    fills (3, 16), (3, 17), (3, 18), (3, 19).
      //    Now row 19 has cols 0, 1, 2 (from L) and col 3 (from I) -> Row 19 clears!
      //    Upper blocks (2, 18) from L and (3, 16), (3, 17), (3, 18) from I must shift down by 1!
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
      // On a 4-column board:
      // Stack three J pieces rotated or three horizontal rows across cols 0..2, then drop vertical
      // I in col 3:
      // Even simpler: use three L/J/T or horizontal pieces in cols 0..2?
      // Look at Tetromino.T in rotation 0: row 0 has col 1, row 1 has cols 0, 1, 2.
      // What piece has 3 full cols in 1 row? T, L, J all have {1, 1, 1} in row 1!
      // What piece has 3 vertical blocks? I has 4 vertical blocks.
      // Let's use a 4-column board and fill rows 17, 18, 19 in cols 0, 1, 2:
      // If we drop 3 vertical I-pieces in cols 0, 1, 2 (filling rows 16, 17, 18, 19 in cols 0..2),
      // and then drop a vertical J-piece (3 blocks tall in col 3, rows 17, 18, 19)!
      // Let's check J in rotation 3 (270° CW):
      // J base: row 0={1,0,0}, row 1={1,1,1}, row 2={0,0,0}.
      // J rot 1 (90° CW): col 1 has rows 0, 1, 2; col 2 has row 0.
      // J rot 3 (270° CW): col 1 has rows 0, 1, 2; col 0 has row 2.
      // If we drop vertical I in col 0 (rows 16..19) and col 1 (rows 16..19), and O in cols 2,3
      // (rows 18..19) + O in cols 2,3 (rows 16..17), that clears 2 + 2.
      // Instead,with vertical I in col 0 (rows 16..19), vertical I in col 1 (rows 16..19),
      // and in cols 2..3:
      // Drop J in rot 1 at cols 2..3: col 2 has 3 blocks, col 3 has 1 block!
      // Even cleaner: on a 3-column board!
      // In a 3-column board, T/L/J in rotation 1 or 3 is 3 rows tall!
      // Wait: in a 6-column board, three O-pieces fill 2 rows; what fills 3 rows?
      // In a 3-column board:
      // Piece 1: L in rotation 1 (col 1: rows 0,1,2; col 2: row 2) at x = -1 -> col 0: rows
      // 17,18,19; col 1: row 19.
      // Actually, in a 3-column board, if we drop 3 pieces of (T/L/J) or I? No, I is 4x4.
      // Let's check 4-column board with:
      // 1. Vertical I at col 0 (rows 16, 17, 18, 19)
      // 2. Vertical I at col 1 (rows 16, 17, 18, 19)
      // 3. Vertical I at col 2 (rows 16, 17, 18, 19)
      // Now cols 0, 1, 2 are filled at rows 16, 17, 18, 19!
      // Now we just need a piece that puts 3 blocks in col 3 at rows 17, 18, 19 (and its 4th block
      // at row 15 or 16 in col 2, resting on top of col 2's row 16!):
      // Look at Tetromino.J in rotation 3:
      // Base J: (0,0), (0,1), (1,1), (2,1).
      // Rot 1: (2,0), (1,0), (1,1), (1,2) -> top-right hook!
      // Wait: in Rot 1, J has:
      //   (col 1, row 0), (col 2, row 0)
      //   (col 1, row 1)
      //   (col 1, row 2)
      // And what about L in Rot 3?
      // Base L: (2,0), (0,1), (1,1), (2,1).
      // Rot 1: (1,0), (1,1), (1,2), (2,2).
      // Rot 2: (0,1), (1,1), (2,1), (0,2).
      // Rot 3: (0,0), (1,0), (1,1), (1,2)!
      // Look at L in Rot 3:
      //   row 0: col 0, col 1
      //   row 1: col 1
      //   row 2: col 1
      // If L in Rot 3 is placed at x = 2 (so col 0 -> board col 2, col 1 -> board col 3):
      // When it drops onto cols 0, 1, 2 (which have height at row 16, so row 15 is empty):
      // At y = 15:
      //   (col 0, row 0) -> (board col 2, row 15) -- rests right on top of (2, 16)!
      //   (col 1, row 0) -> (board col 3, row 15)
      //   (col 1, row 1) -> (board col 3, row 16)
      //   (col 1, row 2) -> (board col 3, row 17)
      // Then rows 16 and 17 are full (2 lines).
      // What if cols 0, 1, 2 only have 3 rows filled (rows 17, 18, 19)?
      // How do we fill rows 17, 18, 19 in cols 0, 1, 2?
      // With three horizontal 3-wide bars!
      // Wait: L, J, T all have a 3-wide bar, plus 1 extra block above it (in rotation 0) or below
      // it (in rotation 2).
      // What if we use a 3-column board and drop three vertical 3-tall pieces?
      // Wait: on a 4-column board, if we drop:
      // 1) O at cols 0, 1 (rows 18, 19)
      // 2) O at cols 0, 1 (rows 16, 17) -> cols 0, 1 have rows 16, 17, 18, 19 filled.
      // 3) L (rot 1: (1,0), (1,1), (1,2), (2,2)) at x = 1 (so col 1 -> board col 2, col 2 -> board
      // col 3):
      //    Hard-drop lands on floor (y = 17):
      //    board col 2 has rows 17, 18, 19; board col 3 has row 19!
      //    Now row 19 is full (cols 0, 1, 2, 3) -> 1 line clears!
      //    Wait: we want 3 lines cleared AT ONCE in a single drop!
      // How to get 3 rows waiting with 1 empty column (col 3), and then fill all 3 rows at once
      // with a single piece in col 3?
      // If rows 17, 18, 19 are filled in cols 0..8, and col 9 is empty, then dropping a vertical
      // I-piece (4 tall) in col 9 fills rows 16, 17, 18, 19 in col 9 -> rows 17, 18, 19 are
      // completely full (3 lines!), while row 16 only has col 9 (not full), so EXACTLY 3 lines
      // clear at once!
      // Let's verify that!
      // On a 4-column board:
      // How to fill rows 17, 18, 19 in cols 0, 1, 2 while keeping row 16 not full and col 3 empty?
      // - Col 0: vertical I-piece -> fills (0, 16), (0, 17), (0, 18), (0, 19).
      // - Col 1: vertical I-piece -> fills (1, 16), (1, 17), (1, 18), (1, 19).
      // - Col 2: we want (2, 17), (2, 18), (2, 19) filled, and (2, 16) EMPTY!
      //   Wait: if we put a vertical I-piece in col 3 instead, it fills (3, 16..19). If (2, 16) is
      //   empty, then row 16 has cols 0, 1, 3 (missing col 2!), while rows 17, 18, 19 have cols 0,
      //   1, 2, 3 (ALL FULL -> 3 lines)!
      //   Wait: how do we get col 2 to have rows 17, 18, 19 filled and (2, 16) empty?
      //   Drop J in rotation 2 or L/T? No, any piece has 4 blocks! Where does the 4th block go?
      //   In row 15 or row 16! Wait: if the 4th block is in col 0 or col 1 at row 15 (on top of row
      //   16), then row 16 is STILL missing col 2!
      //   Let's check!
      //   Suppose:
      //   1. Vertical I in col 0 -> fills (0, 16), (0, 17), (0, 18), (0, 19).
      //   2. Vertical I in col 1 -> fills (1, 16), (1, 17), (1, 18), (1, 19).
      //   Now cols 0 and 1 are filled up to row 16. Cols 2 and 3 are completely empty (down to row
      //   19).
      //   Can a piece land in col 2 at rows 17, 18, 19?
      //   If a piece is in col 2 at rows 17, 18, 19, its 4th block is either at:
      //   - (3, 19) [L rot 1] -> puts a block in col 3!
      //   - (1, 19) [J rot 3] -> blocked by (1, 16..19) as it falls from the top! Because (1, 16)
      //     is in the way, a piece with a block in col 1 will stop when its col 1 block hits row
      //     15!
      //   Aha! What if we drop the J piece FIRST before col 1 is tall?
      //   Let's trace dropping FIRST on an empty 4-column board:
      //   1. Drop O at cols 0, 1 -> fills (0, 18), (1, 18), (0, 19), (1, 19).
      //   2. Drop L in rotation 3 at x = 1 (col 0->1, col 1->2):
      //      L rot 3 has cells: (col 0, row 0), (col 1, row 0), (col 1, row 1), (col 1, row 2).
      //      At x = 1, those are board columns: (1, y), (2, y), (2, y+1), (2, y+2).
      //      As it falls, (1, y) hits (1, 18) at y = 17!
      //      And at y = 17, (2, y+2) = (2, 19) hits the floor at the exact same time!
      //      So L rot 3 locks at: (1, 17), (2, 17), (2, 18), (2, 19)!
      //   3. Now what is filled?
      //      - Row 19: cols 0, 1, 2
      //      - Row 18: cols 0, 1, 2
      //      - Row 17: cols 1, 2 (missing col 0!)
      //   4. Now drop a vertical I in col 0!
      //      Col 0 currently has (0, 18) and (0, 19).
      //      So vertical I in col 0 lands on (0, 18), filling: (0, 14), (0, 15), (0, 16), (0, 17)!
      //      Now look at the board across cols 0, 1, 2:
      //      - Row 19: cols 0, 1, 2 (missing only col 3)
      //      - Row 18: cols 0, 1, 2 (missing only col 3)
      //      - Row 17: cols 0, 1, 2 (missing only col 3)
      //      - Row 16: col 0 only! (cols 1, 2 are empty)
      //   5. Now drop a vertical I in col 3!
      //      Col 3 is completely empty, so vertical I lands on the floor, filling (3, 16), (3, 17),
      //      (3, 18), (3, 19)!
      //      Result:
      //      - Rows 17, 18, 19 have ALL 4 columns (0, 1, 2, 3) filled -> 3 lines cleared at once!
      //      - Row 16 only has cols 0 and 3 -> NOT cleared!
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
