package com.carsonak.jetris.model;

public enum Tetromino {
  I(
      new int[][] {
        {0, 0, 0, 0},
        {1, 1, 1, 1},
        {0, 0, 0, 0},
        {0, 0, 0, 0}
      },
      0x00FFFFff),
  O(
      new int[][] {
        {1, 1},
        {1, 1}
      },
      0xFFFF00ff),
  T(
      new int[][] {
        {0, 1, 0},
        {1, 1, 1},
        {0, 0, 0}
      },
      0x800080ff),
  S(
      new int[][] {
        {0, 1, 1},
        {1, 1, 0},
        {0, 0, 0}
      },
      0x00FF00ff),
  Z(
      new int[][] {
        {1, 1, 0},
        {0, 1, 1},
        {0, 0, 0}
      },
      0xFF0000ff),
  J(
      new int[][] {
        {1, 0, 0},
        {1, 1, 1},
        {0, 0, 0}
      },
      0x0000FFff),
  L(
      new int[][] {
        {0, 0, 1},
        {1, 1, 1},
        {0, 0, 0}
      },
      0xFFA500ff);

  private final int[][] baseShape;
  private final int rgbaColor;

  Tetromino(int[][] baseShape, int rgbaColor) {
    this.baseShape = baseShape;
    this.rgbaColor = rgbaColor;
  }

  public int[][] getBaseShape() {
    return baseShape;
  }

  public int getRgbaColor() {
    return rgbaColor;
  }
}
