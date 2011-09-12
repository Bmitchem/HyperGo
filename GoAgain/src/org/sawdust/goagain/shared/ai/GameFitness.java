package org.sawdust.goagain.shared.ai;

import java.io.Serializable;

import org.sawdust.goagain.shared.go.Game;

public interface GameFitness<T extends Game<T>> extends Serializable {

  public IterativeResult<FitnessValue> gameFitness(Game<T> game, int playerIdx);

}
