package org.sawdust.goagain.shared.ai;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.sawdust.goagain.shared.Game;
import org.sawdust.goagain.shared.Move;

public interface Ai<T extends Game<T>> extends Serializable {

  public static class GameProjection<T extends Game<T>>
  {
    List<Move<T>> moves = new ArrayList<Move<T>>();
    List<Game<T>> games = new ArrayList<Game<T>>();

    public GameProjection(Move<T> move) {
      add(move);
    }

    public GameProjection(Move<T> move, GameProjection<T> projection) {
      this(move);
      moves.addAll(projection.moves);
      games.addAll(projection.games);
    }

    protected GameProjection() {
    }

    public Move<T> firstMove() {
      return moves.get(0);
    }

    public Game<T> finalGame() {
      return games.get(games.size()-1);
    }

    public org.sawdust.goagain.shared.ai.Ai.GameProjection<T> pop() {
      GameProjection<T> gameProjection = new GameProjection<T>();
      for(int i=1;i<moves.size();i++)
      {
        gameProjection.add(moves.get(i));
      }
      return gameProjection;
    }

    protected void add(Move<T> move) {
      moves.add(move);
      games.add(move.move());
    }
    
  }
  
  IterativeResult<GameProjection<T>> newContemplation(Game<T> gameMemoryTree);

}