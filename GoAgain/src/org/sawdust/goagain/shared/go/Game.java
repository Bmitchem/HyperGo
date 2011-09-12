package org.sawdust.goagain.shared.go;

import java.util.Collection;

import org.sawdust.goagain.shared.GameCommand;

public interface Game<T extends Game<T>> {
  Collection<GameCommand<T>> getMoves();
  int player();
  Game<T> unwrap();
}
