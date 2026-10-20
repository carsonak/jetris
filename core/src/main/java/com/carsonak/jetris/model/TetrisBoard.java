package com.carsonak.jetris.model;

public class TetrisBoard implements GameEngine {

  @Override
  public void tick() {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public boolean moveLeft() {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public boolean moveRight() {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public boolean rotateClockwise() {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public void softDrop() {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public void hardDrop() {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public void reset() {
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
