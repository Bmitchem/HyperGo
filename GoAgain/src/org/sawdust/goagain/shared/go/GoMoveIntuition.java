package org.sawdust.goagain.shared.go;

import java.util.Collection;

import org.sawdust.goagain.shared.GameCommand;
import org.sawdust.goagain.shared.ai.MoveFitness;

@SuppressWarnings("serial")
public class GoMoveIntuition implements MoveFitness<GoGame> {
  
  public double moveFitness(GameCommand<GoGame> o1, GoGame game)
  {
    int playerIdx = game.currentPlayer;
    double x = 1;
    if(o1 instanceof GoGame.Move)
    {
      GoGame.Move move = ((GoGame.Move)o1);
      
      Island space = game.getIsland(move.tile);
      if(space.thin().getSize() < 2)
      {
        Island island = game.getIsland(space.getPerimiter().iterator().next());
        if(island.surrounds(space) && island.getLiberties(game).size() >= 2) 
        {
          // Space is surrounded by an immortal island, and too small to make 2 liberties. This move is doomed.
          return -1;
        }
      }
      
      int freindlyCount = 0;
      int enemyCount = 0;
      Collection<Tile> neighbors = move.tile.neighbors();
      int connectivity = neighbors.size();
      for(Tile t : neighbors)
      {
        Island island = game.getIsland(t);
        int state = island.getPlayer();
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
      if(freindlyCount == connectivity)
      {
        x -= 1;
      }
      else
      {
        x += freindlyCount;
      }
      x += enemyCount;
    }
    else
    {
      x += 10;
    }
    return x<0?0:x;
  }
}
