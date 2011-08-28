package org.sawdust.goagain.shared;

public abstract class GameCommand<T> {

  public abstract void move(T board);

  public abstract String getCommandText();

}
