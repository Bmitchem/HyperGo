package org.sawdust.goagain.shared;


public interface Move<T extends Game<T>> {

  Game<T> move();

  String getCommandText();

}
