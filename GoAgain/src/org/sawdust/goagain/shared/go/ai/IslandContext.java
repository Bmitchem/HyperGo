package org.sawdust.goagain.shared.go.ai;

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import org.sawdust.goagain.shared.go.GoGame;
import org.sawdust.goagain.shared.go.IslandNode;
import org.sawdust.goagain.shared.go.Tile;

public class IslandContext
{
  public final Set<IslandNode> liberties = new HashSet<IslandNode>();
  public final Set<IslandNode> territory = new HashSet<IslandNode>();
  public final Set<IslandNode> contested = new HashSet<IslandNode>();
  public final Set<IslandNode> opponent = new HashSet<IslandNode>();

  public IslandContext(GoGame game, IslandNode island) {
    for(Entry<IslandNode, Set<Tile>> e : island.neighbors(game).entrySet())
    {
      IslandNode i = e.getKey();
      if(i.getPlayer()==0)
      {
        if(i.geometry.thin().getSize() < 2 && island.surrounds(i))
        {
          liberties.addAll(i.getConnectedMatching(game));
        }
        else if(i.isTerritory(island.getPlayer(), game))
        {
          territory.addAll(i.getConnectedMatching(game));
        }
        else
        {
          contested.addAll(i.getConnectedMatching(game));
        }
      }
      else
      {
        opponent.add(i);
      }
      
    }
  }
}