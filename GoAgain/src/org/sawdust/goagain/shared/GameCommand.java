package org.sawdust.goagain.shared;

public interface GameCommand<T> {

  T move(T board);

  String getCommandText();

}
