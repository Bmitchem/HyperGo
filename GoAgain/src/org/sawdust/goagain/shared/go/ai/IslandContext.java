package org.sawdust.goagain.shared.go.ai;

import java.util.HashSet;
import java.util.Set;

import org.sawdust.goagain.shared.go.GoGame;
import org.sawdust.goagain.shared.go.IslandNode;

public class IslandContext
{
  Set<IslandNode> liberties = new HashSet<IslandNode>();
  Set<IslandNode> territory = new HashSet<IslandNode>();
  Set<IslandNode> contested = new HashSet<IslandNode>();
  Set<IslandNode> opponent = new HashSet<IslandNode>();

  public IslandContext(GoGame game, IslandNode island) {
    throw new RuntimeException("Not Implemented");
//  Set<Tile> perimiter = new HashSet<Tile>(getPerimiter());
//  while(perimiter.size() > 0)
//  {
//    Tile t = perimiter.iterator().next();
//    IslandNode i = game.getIsland(t);
//    perimiter.removeAll(i.geometry.getPositions());
//    if(i.getPlayer()==0)
//    {
//      if(i.geometry.thin().getSize() < 2 && surrounds(i.geometry))
//      {
//        surroundings.liberties.add(i);
//      }
//      else if(game.isTerritory(getPlayer(), i))
//      {
//        surroundings.territory.add(i);
//      }
//      else
//      {
//        surroundings.contested.add(i);
//      }
//    }
//    else
//    {
//      surroundings.opponent.add(i);
//    }
//  }
  }
}