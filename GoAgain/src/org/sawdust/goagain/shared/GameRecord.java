package org.sawdust.goagain.shared;

import java.io.Serializable;


@SuppressWarnings("serial")
public class GameRecord implements Serializable
{
  public GameId activeId;
  public GameData data;
  
  public GameRecord(GameId activeId, GameData data) {
    super();
    this.activeId = activeId;
    this.data = data;
  }
  
  public GameRecord() {
    super();
  }
  
}