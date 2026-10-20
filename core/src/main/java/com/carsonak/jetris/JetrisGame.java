package com.carsonak.jetris;

import com.badlogic.gdx.Game;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class JetrisGame extends Game {
  @Override
  public void create() {
    setScreen(new FirstScreen());
  }
}
