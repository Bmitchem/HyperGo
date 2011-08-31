package org.sawdust.goagain.shared;

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
    }
    else
    {
      x += 10;
    }
    return x;
  }
}
