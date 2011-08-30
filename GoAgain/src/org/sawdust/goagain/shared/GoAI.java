package org.sawdust.goagain.shared;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.TreeSet;

@SuppressWarnings("serial")
public class GoAI implements Serializable {

  public static boolean isServer = false;
  public boolean useServer = false;
  public int depth = 2;
  public int breadth = 500;
  
  public void move(GoGame board) {
    move(board, depth, breadth);
  }

  protected GameCommand<GoGame> move(final GoGame game, final int depth, final int bredth)
  {
      TreeSet<GameCommand<GoGame>> moves = new TreeSet<GameCommand<GoGame>>(new Comparator<GameCommand<GoGame>>()
      {
          public int compare(GameCommand<GoGame> o1, GameCommand<GoGame> o2)
          {
              double v1 = 0.0;
              double v2 = 0.0;
              v1 = moveFitness(o1, game, v1);
              v2 = moveFitness(o2, game, v2);
              int compare1 = Double.compare(v2, v1);
              return compare1;
          }
      });
      ArrayList<GameCommand<GoGame>> m = game.getMoves();
      //Collections.shuffle(m);
      moves.addAll(m);
      GameCommand<GoGame> bestMove = null;
      double bestFitness = Integer.MIN_VALUE;
      int currentBreadth = 0;
      for (GameCommand<GoGame> thisMove : moves)
      {
          if (currentBreadth++ > bredth) break;
          try {
            int player = game.currentPlayer;
            GoGame hypotheticalGame = new GoGame(game);
            thisMove.move(hypotheticalGame);
            if (depth > 1)
            {
              move(hypotheticalGame, depth - 1, bredth);
            }
            double fitness1 = gameFitness(hypotheticalGame, player);
            boolean isBetter = fitness1 > bestFitness;
            if (null == bestMove || isBetter)
            {
              bestMove = thisMove;
              bestFitness = fitness1;
            }
          } catch (Exception e) {
          }
      }
      if (null != bestMove)
      {
          bestMove.move(game);
      }
      return bestMove;
  }

  protected double gameFitness(GoGame game, int playerIdx) {
    if (null == game) return Integer.MIN_VALUE;
     int otherIdx = (playerIdx == 1) ? 2 : 1;

     // Score-level fitness
     int score1 = game.getScore(playerIdx);
     int score2 = game.getScore(otherIdx);
     int scoreDiff = score1 - score2;
     double fitness = scoreDiff * 1000;
     
     // Freedom-level fitness
     for (Island island : game.islands)
     {
        double bias = -1.0;
        double freedom = 0;
        for(Tile t : island.getPerimiter())
        {
          if(game.getState(t) == 0)
          {
            freedom += 1;
          }
        }
        if (playerIdx == island.getPlayer()) bias = 1.0;
        fitness += bias * freedom * island.getPositions().size();
     }
     
     return fitness;
  }
  
  private double moveFitness(GameCommand<GoGame> o1, GoGame game, double v1)
  {
    int playerIdx = game.currentPlayer;
    if(o1 instanceof GoGame.Move)
    {
      GoGame.Move move = ((GoGame.Move)o1);
      int freindlyCount = 0;
      int enemyCount = 0;
      for(Tile t : move.tile.neighbors())
      {
        Integer state = game.tileState.get(t.idx);
        if(null != state)
        {
          if(state.equals(playerIdx))
          {
            freindlyCount++;
          }
          else
          {
            enemyCount++;
          }
        }
      }
      double x = 0.0;
      if(freindlyCount > 2)
      {
        x -= freindlyCount * 0.5;
      }
      else
      {
        x += freindlyCount;
      }
      if(enemyCount > 1)
      {
        x -= enemyCount;
      }
      else
      {
        x += enemyCount;
      }
      return x;
    }
    else
    {
      return 10;
    }
  }

}
