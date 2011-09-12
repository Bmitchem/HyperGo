package org.sawdust.goagain.shared.go.ai;

import java.util.HashMap;
import java.util.Map;

import org.sawdust.goagain.shared.ai.FitnessValue;
import org.sawdust.goagain.shared.ai.GameFitness;
import org.sawdust.goagain.shared.ai.IterativeResult;
import org.sawdust.goagain.shared.go.Game;

@SuppressWarnings("serial")
public class MemoryFitness<T extends Game<T>> implements GameFitness<T> {

  GameFitness<T> inner;
  
  public MemoryFitness(GameFitness<T> inner) {
    super();
    this.inner = inner;
  }

  public IterativeResult<FitnessValue> gameFitness(Game<T> game, int playerIdx) {
    return eval(new EvalParameter<T>(game, playerIdx));
  }

  public static class EvalParameter<T extends Game<T>> {
    public Game<T> game;
    public int playerIdx;

    public EvalParameter(Game<T> game, int playerIdx) {
      this.game = game;
      this.playerIdx = playerIdx;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((game == null) ? 0 : game.hashCode());
      result = prime * result + playerIdx;
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj == null) return false;
      if (getClass() != obj.getClass()) return false;
      @SuppressWarnings("unchecked") EvalParameter<T> other = (EvalParameter<T>) obj;
      if (game == null) {
        if (other.game != null) return false;
      } else if (!game.equals(other.game)) return false;
      if (playerIdx != other.playerIdx) return false;
      return true;
    }
  }

  Map<EvalParameter<T>, IterativeResult<FitnessValue>> cache = new HashMap<EvalParameter<T>, IterativeResult<FitnessValue>>();
  protected IterativeResult<FitnessValue> eval(EvalParameter<T> parameterObject) {
    IterativeResult<FitnessValue> result;
    if (cache.containsKey(parameterObject)) {
      result = cache.get(parameterObject);
    }
    else
    {
      result = inner.gameFitness(parameterObject.game, parameterObject.playerIdx);
      cache.put(parameterObject, result);
    }
    return result;
  }

}
