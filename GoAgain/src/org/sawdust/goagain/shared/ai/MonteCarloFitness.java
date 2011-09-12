package org.sawdust.goagain.shared.ai;

import org.sawdust.goagain.shared.GameCommand;
import org.sawdust.goagain.shared.go.Game;
import org.sawdust.goagain.shared.go.GoGame;
import org.sawdust.goagain.shared.go.IslandNode;
import org.sawdust.goagain.shared.go.Util;

@SuppressWarnings("serial")
public class MonteCarloFitness<T extends Game<T>> implements GameFitness<T> {

  public IterativeResult<FitnessValue> gameFitness(final Game<T> g, final int playerIdx) {
    final GoGame game = (GoGame) g;
    return new IterativeResult<FitnessValue>() {
      int total = 0;
      int wins = 0;
      
      public double think() {
        GoGame prevGame = null;
        GoGame newGame = game;
        GameCommand<GoGame> move = null;
        Integer w = null;
        int moveCount = 0;
        do
        {
          prevGame = newGame;
          newGame = null;
          while(null == newGame)
          {
            move = Util.randomValue(prevGame.getMoves());
            newGame = (GoGame) move.move(prevGame).unwrap();
          }
          moveCount++;
          w = getWinner(prevGame, newGame, move);
        }while(null==w);
        wins += w.equals(playerIdx) ? 1 : 0;
        total++;
        return 1;
      }
      
      public FitnessValue best() {
        double mean = wins / total;
        double confidence = 5 * Math.pow(total, -1.5);
        return new FitnessValue(mean, confidence);
      }
    };
  }


  boolean firstCaptureWins = true;
  protected Integer getWinner(GoGame prevGame, GoGame game, GameCommand<GoGame> move)
  {
    Integer w;
    if(null != game.winner)
    {
      w = game.winner;
    }
    else
    {
      if(null != prevGame && firstCaptureWins)
      {
        if(count(game, prevGame.currentPlayer) < 1 + count(prevGame, prevGame.currentPlayer))
        {
          w = game.currentPlayer;
        }
        else if(count(game, game.currentPlayer) < count(prevGame, game.currentPlayer))
        {
          w = prevGame.currentPlayer;
        }
        else
        {
          w = null;
        }
      }
      else
      {
        w = null;
      }
    }
    return w;
  }

  protected static int count(GoGame prevGame, int player)
  {
    int stoneCount = 0;
    for(IslandNode i : prevGame.islands.values())
    {
      if(i.getPlayer() == player)
      {
        stoneCount += i.geometry.getSize();
      }
    }
    return stoneCount;
  }
}
