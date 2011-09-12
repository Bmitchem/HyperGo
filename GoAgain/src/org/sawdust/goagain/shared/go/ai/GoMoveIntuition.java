package org.sawdust.goagain.shared.go.ai;

import java.util.Map;
import java.util.Set;

import org.sawdust.goagain.shared.Move;
import org.sawdust.goagain.shared.ai.MoveFitness;
import org.sawdust.goagain.shared.go.GoGame;
import org.sawdust.goagain.shared.go.IslandNode;
import org.sawdust.goagain.shared.go.Tile;

@SuppressWarnings("serial")
public class GoMoveIntuition implements MoveFitness<GoGame> {
  
  public double moveFitness(Move<GoGame> o1, GoGame game)
  {
    double x = 1;
    if(o1 instanceof GoGame.PlaceMove)
    {
      GoGame.PlaceMove move = ((GoGame.PlaceMove)o1);
      
      IslandNode space = game.getIsland(move.tile);
      if(space.geometry.thin().getSize() < 2)
      {
        Map<IslandNode, Set<Tile>> neighbors = space.neighbors(game);
        for(IslandNode island : neighbors.keySet())
        {
          if(island.getPlayer() != 0 && island.surrounds(space)) 
          {
            IslandContext surround = new IslandContext(game, island);
            if(surround.liberties.size() >= 2) 
            {
              // Space is surrounded by an immortal island, and too small to make 2 liberties. This move is doomed.
              return -1;
            }
            else
            {
              break;
            }
          }
          else
          {
            break;
          }
        }
      }
      
//      int playerIdx = game.currentPlayer;
//      int freindlyCount = 0;
//      int enemyCount = 0;
//      Collection<Tile> neighbors = move.tile.neighbors();
//      int connectivity = neighbors.size();
//      for(Tile t : neighbors)
//      {
//        IslandNode island = game.getIsland(t);
//        int state = island.getPlayer();
//        if(0 != state)
//        {
//          if(state == playerIdx)
//          {
//            freindlyCount++;
//          }
//          else
//          {
//            enemyCount++;
//          }
//        }
//      }
//      if(freindlyCount == connectivity)
//      {
//        x -= 1;
//      }
//      else
//      {
//        x += freindlyCount;
//      }
//      x += enemyCount;
    }
    else
    {
      x += 10;
    }
    return x<0?0:x;
  }
}
