package org.sawdust.goagain.shared.ai;

import org.sawdust.goagain.shared.Game;

@SuppressWarnings("serial")
public class MemoryFitness<T extends Game<T>> implements GameFitness<T> {

  GameFitness<T> inner;
  
  public MemoryFitness(GameFitness<T> inner) {
    super();
    this.inner = inner;
  }

  public IterativeResult<FitnessValue> gameFitness(T game, int playerIdx) {
    Key<T> key = new Key<T>(playerIdx, this);
    @SuppressWarnings("unchecked") IterativeResult<FitnessValue> result = (IterativeResult<FitnessValue>) game.getCache(key);
    if (null == result) {
      result = inner.gameFitness(game, playerIdx);
      game.putCache(key, result);
    }
    return result;
  }

  public static class Key<T extends Game<T>> {
    
    public int playerIdx;
    private MemoryFitness<T> parent;

    public Key(int playerIdx, MemoryFitness<T> p) {
      this.parent = p;
      this.playerIdx = playerIdx;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((parent == null) ? 0 : parent.hashCode());
      result = prime * result + playerIdx;
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj == null) return false;
      if (getClass() != obj.getClass()) return false;
      @SuppressWarnings("unchecked") Key<T> other = (Key<T>) obj;
      if (parent == null) {
        if (other.parent != null) return false;
      } else if (!parent.equals(other.parent)) return false;
      if (playerIdx != other.playerIdx) return false;
      return true;
    }
  }


  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("MemoryFitness [inner=");
    builder.append(inner);
    builder.append("]");
    return builder.toString();
  }

  
  
}
