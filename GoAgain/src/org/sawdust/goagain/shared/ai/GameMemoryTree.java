package org.sawdust.goagain.shared.ai;

import java.util.ArrayList;
import java.util.Collection;

import org.sawdust.goagain.shared.Game;
import org.sawdust.goagain.shared.Move;

public class GameMemoryTree<T extends Game<T>> extends Game<T> {

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
        wrapped.add(new Move<T>() {
          Game<T> result = null;
          public Game<T> move(T board) {
            if(null == result)
            {
              result = new GameMemoryTree<T>(move.move(game.unwrap()));
            }
            return result;
          }
          
          public String getCommandText() {
            return move.getCommandText();
          }
        });
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
