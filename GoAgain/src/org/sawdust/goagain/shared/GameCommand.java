package org.sawdust.goagain.shared;

public abstract class GameCommand<T> {

  public abstract T move(T board);

  public abstract String getCommandText();

}
