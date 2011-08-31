package org.sawdust.goagain.shared;

import java.io.Serializable;

public interface MoveFitness<T> extends Serializable {

  double moveFitness(GameCommand<T> o1, T game);

}
