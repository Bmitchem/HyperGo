package org.sawdust.goagain.shared.go;

import org.sawdust.goagain.shared.GameCommand;
import org.sawdust.goagain.shared.ai.MoveFitness;

@SuppressWarnings("serial")
public class GoMoveIntuition implements MoveFitness<GoGame> {
  
  public double moveFitness(GameCommand<GoGame> o1, GoGame game)
  {
    int playerIdx = game.currentPlayer;
    double x = 10.0;
    if(o1 instanceof GoGame.Move)
    {
      GoGame.Move move = ((GoGame.Move)o1);
      int freindlyCount = 0;
      int enemyCount = 0;
      for(Tile t : move.tile.neighbors())
      {
        int state = game.getState(t);
        if(0 != state)
        {
          if(state == playerIdx)
          {
            freindlyCount++;
          }
          else
          {
            enemyCount++;
          }
        }
      }
      int connectivity = game.layout.connectivity;
      if(freindlyCount > connectivity-1)
      {
        x -= 10;
      }
      else if(freindlyCount > 0)
      {
        x += freindlyCount;
      }
      if(enemyCount > connectivity-1)
      {
        x -= 10;
      }
      else if(enemyCount > 0)
      {
        x += enemyCount;
      }
    }
    else
    {
      x += 10;
    }
    return x;
  }
}
