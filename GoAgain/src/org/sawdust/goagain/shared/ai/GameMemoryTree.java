package org.sawdust.goagain.shared.ai;

import java.util.ArrayList;
import java.util.Collection;

import org.sawdust.goagain.shared.Game;
import org.sawdust.goagain.shared.Move;

public class GameMemoryTree<T extends Game<T>> extends Game<T> {

  public static final class CachedMove<T extends Game<T>> implements Move<T> {
    private final Move<T> move;
    Game<T> result = null;

    public CachedMove(Move<T> move) {
      this.move = move;
    }

    public Game<T> move() {
      if(null == result)
      {
        result = new GameMemoryTree<T>(move.move());
      }
      return result;
    }

    public String getCommandText() {
      return move.getCommandText();
    }
  }

  Game<T> game;
  ArrayList<Move<T>> wrapped = null;
  
  public GameMemoryTree(Game<T> game2) {
    super();
    this.game = game2;
  }

  public Collection<? extends Move<T>> getMoves() {
    if(null == wrapped)
    {
      wrapped = new ArrayList<Move<T>>();
      for(final Move<T> move : game.getMoves())
      {
        wrapped.add(new CachedMove<T>(move));
      }
    }
    return wrapped;
  }

  public int player() {
    return game.player();
  }

  public T unwrap() {
    return game.unwrap();
  }

  @Override
  public Object getCache(Object key) {
    return game.getCache(key);
  }

  @Override
  public void putCache(Object key, Object value) {
    game.putCache(key, value);
  }

}
