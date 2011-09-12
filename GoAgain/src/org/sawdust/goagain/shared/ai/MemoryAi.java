package org.sawdust.goagain.shared.ai;

import org.sawdust.goagain.shared.Game;
import org.sawdust.goagain.shared.Move;

@SuppressWarnings("serial")
public class MemoryAi<T extends Game<T>> implements Ai<T> {

  Ai<T> inner;
  
  public MemoryAi(Ai<T> inner) {
    super();
    this.inner = inner;
  }

  public IterativeResult<Move<T>> newContemplation(final Game<T> game) {
    Key<T> key = new Key<T>(game, this);
    @SuppressWarnings("unchecked") IterativeResult<Move<T>> result = (IterativeResult<Move<T>>) game.getCache(key);
    if (null == result) {
      result = new IterativeResult<Move<T>>() {

        IterativeResult<Move<T>> innerMove = inner.newContemplation(game);
        GameMemoryTree.CachedMove<T> cachedMove = null;
        double progress = 0;
        public double think() {
          if(1. > progress)
          {
            cachedMove = null;
            progress = innerMove.think();
          }
          return progress;
        }

        public Move<T> best() {
          if(null == cachedMove)
          {
            cachedMove = new GameMemoryTree.CachedMove<T>(innerMove.best());
          }
          return cachedMove;
        }

        public void hint(Move<T> hint) {
          if(1. > progress)
          {
            innerMove.hint(hint);
            cachedMove = null;
          }
        }
      };
      game.putCache(key, result);
    }
    return result;
  }

  public static class Key<T extends Game<T>> {
    
    public Game<T> game;
    private MemoryAi<T> parent;

    public Key(Game<T> a, MemoryAi<T> p) {
      this.parent = p;
      this.game = a;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((parent == null) ? 0 : parent.hashCode());
      result = prime * result + ((game == null) ? 0 : game.hashCode());
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
      if (game == null) {
        if (other.game != null) return false;
      } else if (!game.equals(other.game)) return false;
      return true;
    }
  }

}
