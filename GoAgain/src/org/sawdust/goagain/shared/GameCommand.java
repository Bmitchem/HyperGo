package org.sawdust.goagain.shared;

import org.sawdust.goagain.shared.go.Game;

public interface GameCommand<T extends Game<T>> {

  Game<T> move(Game<T> game);

  String getCommandText();

}
