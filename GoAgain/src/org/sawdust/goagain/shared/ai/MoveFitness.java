package org.sawdust.goagain.shared.ai;

import java.io.Serializable;

import org.sawdust.goagain.shared.Game;
import org.sawdust.goagain.shared.Move;

public interface MoveFitness<T extends Game<T>> extends Serializable {

  double moveFitness(Move<T> o1, T game);

}
