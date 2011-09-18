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

  public IterativeResult<GameProjection<T>> newContemplation(final Game<T> game) {
    Key<T> key = new Key<T>(game, this);
    @SuppressWarnings("unchecked") IterativeResult<GameProjection<T>> result = (IterativeResult<GameProjection<T>>) game.getCache(key);
    if (null == result) {
      result = new IterativeResult<GameProjection<T>>() {

        IterativeResult<GameProjection<T>> innerMove = inner.newContemplation(game);
        GameProjection<T> cachedMove = null;
        double progress = 0;
        public double think() {
          if(1. > progress)
          {
            cachedMove = null;
            progress = innerMove.think();
          }
          return progress;
        }

        public GameProjection<T> best() {
          if(null == cachedMove)
          {
            cachedMove = innerMove.best();
          }
          return cachedMove;
        }

        public void hint(GameProjection<T> hint) {
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
