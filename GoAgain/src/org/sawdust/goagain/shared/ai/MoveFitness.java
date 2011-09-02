package org.sawdust.goagain.shared.ai;

import java.io.Serializable;

import org.sawdust.goagain.shared.GameCommand;

public interface MoveFitness<T> extends Serializable {

  double moveFitness(GameCommand<T> o1, T game);

}
