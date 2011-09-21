package org.sawdust.goagain.shared.go.ai;

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import org.sawdust.goagain.shared.go.GoGame;
import org.sawdust.goagain.shared.go.IslandNode;
import org.sawdust.goagain.shared.go.Tile;

public class IslandContext
{
  public final Set<IslandNode<Integer>> liberties = new HashSet<IslandNode<Integer>>();
  public final Set<IslandNode<Integer>> territory = new HashSet<IslandNode<Integer>>();
  public final Set<IslandNode<Integer>> contested = new HashSet<IslandNode<Integer>>();
  public final Set<IslandNode<Integer>> opponent = new HashSet<IslandNode<Integer>>();

  public IslandContext(GoGame game, IslandNode<Integer> island) {
    for(Entry<IslandNode<Integer>, Set<Tile>> e : island.neighbors(game).entrySet())
    {
      IslandNode<Integer> i = e.getKey();
      if(null == i.getPlayer())
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