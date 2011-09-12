package org.sawdust.goagain.shared.go;

import org.sawdust.goagain.shared.Move;
import org.sawdust.goagain.shared.Util;
import org.sawdust.goagain.shared.ai.FitnessValue;
import org.sawdust.goagain.shared.ai.GameFitness;
import org.sawdust.goagain.shared.ai.IterativeResult;

@SuppressWarnings("serial")
public class MonteCarloFitness implements GameFitness<GoGame> {

  public IterativeResult<FitnessValue> gameFitness(final GoGame game, final int playerIdx) {
    return new IterativeResult<FitnessValue>() {
      int total = 0;
      int wins = 0;
      
      public double think() {
        GoGame prevGame = null;
        GoGame newGame = game;
        Move<GoGame> move = null;
        Integer w = null;
        int moveCount = 0;
        do
        {
          prevGame = newGame;
          newGame = null;
          while(null == newGame)
          {
            move = (Move<GoGame>) Util.randomValue(prevGame.getMoves());
            newGame = (GoGame) move.move().unwrap();
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
  protected Integer getWinner(GoGame prevGame, GoGame newGame, Move<GoGame> move)
  {
    Integer w;
    if(null != newGame.winner)
    {
      w = newGame.winner;
    }
    else
    {
      if(null != prevGame && firstCaptureWins)
      {
        if(count(newGame, prevGame.currentPlayer) < 1 + count(prevGame, prevGame.currentPlayer))
        {
          w = newGame.currentPlayer;
        }
        else if(count(newGame, newGame.currentPlayer) < count(prevGame, newGame.currentPlayer))
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
