package org.sawdust.goagain.shared.ai;

import java.io.Serializable;

import org.sawdust.goagain.shared.Game;

public interface GameFitness<T extends Game<T>> extends Serializable {

  public IterativeResult<FitnessValue> gameFitness(T game, int playerIdx);

}
