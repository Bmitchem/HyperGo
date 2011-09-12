package org.sawdust.goagain.shared;


public interface Move<T extends Game<T>> {

  Game<T> move(T game);

  String getCommandText();

}
