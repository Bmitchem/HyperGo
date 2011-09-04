package org.sawdust.goagain.shared;

import java.io.Serializable;

import org.sawdust.goagain.shared.go.GoGame;
import org.sawdust.goagain.shared.go.ai.GoAI;


@SuppressWarnings("serial")
public class GameData implements Serializable {
  public GoAI[] ai;
  public GoGame game;
}
