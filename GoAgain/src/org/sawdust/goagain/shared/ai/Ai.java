package org.sawdust.goagain.shared.ai;

import java.io.Serializable;

import org.sawdust.goagain.shared.GameCommand;
import org.sawdust.goagain.shared.go.GoGame;

public interface Ai<T> extends Serializable {

  IterativeResult<GameCommand<T>> newContemplation(T game);

}