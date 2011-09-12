package org.sawdust.goagain.shared.ai;

import java.io.Serializable;

import org.sawdust.goagain.shared.GameCommand;
import org.sawdust.goagain.shared.go.Game;

public interface MoveFitness<T extends Game<T>> extends Serializable {

  double moveFitness(GameCommand<T> o1, T game);

}
