package com.carsonak.jetris.model;

import java.util.List;

/**
 * Core headless implementation of {@link GameEngine} and {@link BoardState}.
 *
 * <p>NOTE (Design Pointers for Implementation):
 *
 * <ul>
 *   <li><b>Spawning:</b> Use {@code type.getWidth()} from {@link Tetromino} to center newly spawned
 *       pieces at {@code x = (width - type.getWidth()) / 2, y = 0, rotationIndex = 0}. Because
 *       {@link ActivePiece#getOccupiedCells()} uses normalized top-left coordinates, {@code x} and
 *       {@code y} are always {@code >= 0} for valid on-board positions (no negative origin hack
 *       needed!).
 *   <li><b>Wall & Floor Kicks:</b> For both {@link #rotateClockwise()} and {@link
 *       #rotateAnticlockwise()}, test candidate {@code (dx, dy)} offsets in this exact order:
 *       {@code (0, 0), (-1, 0), (+1, 0), (-2, 0), (+2, 0), (0, -1)}. Apply the first candidate
 *       whose {@code getOccupiedCells()} are all within {@code [0, width - 1] x [0, height - 1]}
 *       and do not overlap locked blocks.
 *   <li><b>Line Clearing & Scoring Invariant:</b> Since the tallest standard {@link Tetromino}
 *       ({@code I}) is 4 blocks high, a single piece lock can clear at most 4 lines ({@code 0 <=
 *       cleared <= 4}). Use standard fixed scores: {@code 1 -> 100, 2 -> 300, 3 -> 500, 4 -> 800},
 *       and guard the invariant that {@code cleared} is never {@code > 4}.
 * </ul>
 */
public class TetrisBoard implements GameEngine {
  public static final int DEFAULT_WIDTH = 10;
  public static final int DEFAULT_HEIGHT = 20;

  private final int width;
  private final int height;
  private final boolean[][] board;
  private final PieceGenerator pieceGenerator;
  private ActivePiece activePiece;
  private Tetromino nextPiece;
  private int score = 0;
  private int clearedLines = 0;
  private boolean gameIsOver = false;

  public TetrisBoard(PieceGenerator pieceGenerator) {
    this(DEFAULT_WIDTH, DEFAULT_HEIGHT, pieceGenerator);
  }

  public TetrisBoard(int width, int height, PieceGenerator pieceGenerator) throws IllegalArgumentException {
    if (width < 5 || height < 6) {
      throw new IllegalArgumentException(
          "Minimum board dimensions are width = 4 and height = 5, got width = " + width + " and height = " + height);
    }

    if (pieceGenerator == null) {
      throw new IllegalArgumentException("`pieceGenerator` cannot be null");
    }

    this.width = width;
    this.height = height;
    board = new boolean[height][width];
    this.pieceGenerator = pieceGenerator;
    nextPiece = pieceGenerator.nextPiece();
    spawnPiece();
  }

  private void spawnPiece() {
    activePiece = new ActivePiece(nextPiece, width / 2, 0, 0);
    nextPiece = pieceGenerator.nextPiece();
  }

  @Override
  public void tick() {
    // TODO: Advance active piece down 1 row if valid; otherwise lock piece, clear full lines,
    // update score/linesCleared, and spawn next piece.
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public boolean moveLeft() {
    return shiftActivePiece(-1, 0);
  }

  @Override
  public boolean moveRight() {
    return shiftActivePiece(1, 0);
  }

  private boolean shiftActivePiece(final int dx, final int dy) {
    final var projection = activePiece.moveBy(dx, dy);

    for (var block : projection.getOccupiedCells()) {
      final int x = block.x();
      final int y = block.y();

      if (x < 0 || x >= width || y >= height || board[x][y] == true) {
        return false;
      }

    }

    activePiece = projection;
    return true;
  }

  @Override
  public boolean rotateClockwise() {
    // TODO: Attempt to rotate active piece 90° CW using the 6 wall/floor kick offsets:
    // (0, 0), (-1, 0), (+1, 0), (-2, 0), (+2, 0), (0, -1).
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public boolean rotateAnticlockwise() {
    // TODO: Attempt to rotate active piece 90° CCW using the 6 wall/floor kick offsets:
    // (0, 0), (-1, 0), (+1, 0), (-2, 0), (+2, 0), (0, -1).
    throw new UnsupportedOperationException("Not implemented");
  }

  private int updateBoard(final List<Point> cells) {
    int clearedLines = 0;

    for (var c : cells) {
      board[c.x()][c.y()] = true;
    }

    // TODO: Clear complete lines and shift locked rows down

    return clearedLines;
  }

  @Override
  public void softDrop() {
    // TODO: Move piece down 1 row; if blocked, lock immediately and spawn next piece.
    if (shiftActivePiece(0, 1)) {
      return;
    }

    final int clearedLines = updateBoard(activePiece.getOccupiedCells());

    // TODO: Update score
    spawnPiece();
  }

  @Override
  public void hardDrop() {
    // TODO: Drop piece to lowest valid row, lock immediately, and spawn next piece.
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public void reset() {
    // TODO: Clear grid, reset score/linesCleared/gameOver, and spawn fresh piece.
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public int getWidth() {
    return width;
  }

  @Override
  public int getHeight() {
    return height;
  }

  @Override
  public Tetromino getLockedBlockAt(int x, int y) {
    throw new UnsupportedOperationException("Not Implemented");
  }

  @Override
  public ActivePiece getActivePiece() {
    return activePiece;
  }

  @Override
  public Tetromino getNextPiecePreview() {
    return nextPiece;
  }

  @Override
  public int getScore() {
    return score;
  }

  @Override
  public int getLinesCleared() {
    return clearedLines;
  }

  @Override
  public boolean isGameOver() {
    return gameIsOver;
  }
}
