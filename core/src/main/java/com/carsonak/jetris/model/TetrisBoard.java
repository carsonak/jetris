package com.carsonak.jetris.model;

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

  public TetrisBoard(PieceGenerator pieceGenerator) {
    this(DEFAULT_WIDTH, DEFAULT_HEIGHT, pieceGenerator);
  }

  public TetrisBoard(int width, int height, PieceGenerator pieceGenerator) {
    // TODO: Initialize board dimensions, grid, pieceGenerator, and spawn initial active + preview
    // pieces.
  }

  @Override
  public void tick() {
    // TODO: Advance active piece down 1 row if valid; otherwise lock piece, clear full lines,
    // update score/linesCleared, and spawn next piece.
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public boolean moveLeft() {
    // TODO: Attempt to move active piece by (-1, 0).
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public boolean moveRight() {
    // TODO: Attempt to move active piece by (+1, 0).
    throw new UnsupportedOperationException("Not implemented");
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

  @Override
  public void softDrop() {
    // TODO: Move piece down 1 row; if blocked, lock immediately and spawn next piece.
    throw new UnsupportedOperationException("Not implemented");
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
    throw new UnsupportedOperationException("Not Implemented");
  }

  @Override
  public int getHeight() {
    throw new UnsupportedOperationException("Not Implemented");
  }

  @Override
  public Tetromino getLockedBlockAt(int x, int y) {
    throw new UnsupportedOperationException("Not Implemented");
  }

  @Override
  public ActivePiece getActivePiece() {
    throw new UnsupportedOperationException("Not Implemented");
  }

  @Override
  public Tetromino getNextPiecePreview() {
    throw new UnsupportedOperationException("Not Implemented");
  }

  @Override
  public int getScore() {
    throw new UnsupportedOperationException("Not Implemented");
  }

  @Override
  public int getLinesCleared() {
    throw new UnsupportedOperationException("Not Implemented");
  }

  @Override
  public boolean isGameOver() {
    throw new UnsupportedOperationException("Not Implemented");
  }
}
