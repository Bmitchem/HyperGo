package org.sawdust.goagain.shared.ai;

import java.io.Serializable;

import org.sawdust.goagain.shared.GameCommand;
import org.sawdust.goagain.shared.go.Game;

public interface Ai<T extends Game<T>> extends Serializable {

  IterativeResult<GameCommand<T>> newContemplation(Game<T> gameMemoryTree);

}