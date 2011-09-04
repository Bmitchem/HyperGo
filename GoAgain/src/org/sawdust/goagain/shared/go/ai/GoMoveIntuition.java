package org.sawdust.goagain.shared.go.ai;

import java.util.Collection;

import org.sawdust.goagain.shared.GameCommand;
import org.sawdust.goagain.shared.ai.MoveFitness;
import org.sawdust.goagain.shared.go.GoGame;
import org.sawdust.goagain.shared.go.IslandNode;
import org.sawdust.goagain.shared.go.Tile;

@SuppressWarnings("serial")
public class GoMoveIntuition implements MoveFitness<GoGame> {
  
  public double moveFitness(GameCommand<GoGame> o1, GoGame game)
  {
    // TODO: Update
    int playerIdx = game.currentPlayer;
    double x = 1;
    if(o1 instanceof GoGame.Move)
    {
      GoGame.Move move = ((GoGame.Move)o1);
      
      IslandNode space = game.getIsland(move.tile);
      if(space.geometry.thin().getSize() < 2)
      {
        IslandNode island = game.getIsland(space.geometry.getPerimiter().iterator().next());
        IslandContext surround = new IslandContext(game, island);
        if(island.surrounds(space) && surround.liberties.size() >= 2) 
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
        IslandNode island = game.getIsland(t);
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
