package org.sawdust.goagain.shared;

import java.io.Serializable;


@SuppressWarnings("serial")
public class GameData implements Serializable {
  public GoAI[] ai;
  public GoGame game;
}
