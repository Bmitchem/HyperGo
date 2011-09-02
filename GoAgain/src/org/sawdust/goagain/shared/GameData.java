package org.sawdust.goagain.shared;

import java.io.Serializable;

import org.sawdust.goagain.shared.go.GoAI;
import org.sawdust.goagain.shared.go.GoGame;


@SuppressWarnings("serial")
public class GameData implements Serializable {
  public GoAI[] ai;
  public GoGame game;
}
