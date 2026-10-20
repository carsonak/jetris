package com.carsonak.jetris.model;

@FunctionalInterface
public interface PieceGenerator {
  Tetromino nextPiece();
}
