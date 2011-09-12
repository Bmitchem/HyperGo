package org.sawdust.goagain.shared.ai;

import java.io.Serializable;

import org.sawdust.goagain.shared.Game;
import org.sawdust.goagain.shared.Move;

public interface Ai<T extends Game<T>> extends Serializable {

  IterativeResult<Move<T>> newContemplation(Game<T> gameMemoryTree);

}