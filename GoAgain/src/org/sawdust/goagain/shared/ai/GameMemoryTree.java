package org.sawdust.goagain.shared.ai;

import java.util.ArrayList;

import org.sawdust.goagain.shared.GameCommand;
import org.sawdust.goagain.shared.go.Game;

public class GameMemoryTree<T extends Game<T>> implements Game<T> {

  T game;
  ArrayList<GameCommand<T>> wrapped = null;
  
  public GameMemoryTree(T game) {
    super();
    this.game = game;
  }

  public ArrayList<GameCommand<T>> getMoves() {
    if(null == wrapped)
    {
      wrapped = new ArrayList<GameCommand<T>>();
      for(final GameCommand<T> move : game.getMoves())
      {
        wrapped.add(new GameCommand<T>() {
          T result = null;
          @SuppressWarnings("unchecked")
          public T move(Game<T> board) {
            if(null == result)
            {
              result = (T) new GameMemoryTree<T>((T) move.move(game));
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

  public Game<T> unwrap() {
    return game.unwrap();
  }

}
