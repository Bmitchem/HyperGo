package org.sawdust.goagain.shared.go;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

@SuppressWarnings("serial")
public class IslandNode implements Serializable {

  public IslandGeometry geometry;
  public int player;
  public Map<Integer,Set<Tile>> border = new HashMap<Integer, Set<Tile>>();

  protected IslandNode() {
    super();
  }

  private IslandNode(int player, IslandGeometry geometry) {
    super();
    this.player = player;
    this.geometry = geometry;
  }

  public IslandNode(int player, IslandGeometry geometry, Collection<IslandGeometry> neighbors) {
    this(player, geometry);
    if(null != neighbors)
    {
      for(IslandGeometry n : neighbors)
      {
        Set<Tile> countConnections = geometry.countConnections(n);
        if(0 < countConnections.size())
        {
          border.put(n.getId(), countConnections);
        }
      }
    }
  }

  public IslandNode replace(IslandNode thisIsland, Collection<IslandNode> created) {
    IslandNode mutant = new IslandNode(player, geometry);
    mutant.border.putAll(border);
    mutant.border.remove(thisIsland.getId());
    for(IslandNode i : created)
    {
      Set<Tile> countConnections = geometry.countConnections(i.geometry);
      if(0 < countConnections.size())
      {
        mutant.border.put(i.getId(), countConnections);
      }
    }
    return mutant;
  }

  public IslandNode replace(Collection<IslandNode> toJoin, IslandNode created) {
    IslandNode mutant = new IslandNode(player, geometry);
    mutant.border.putAll(border);
    Set<Tile> countConnections = new HashSet<Tile>();
    for(IslandNode i : toJoin)
    {
      Collection<Tile> integer = mutant.border.get(i.getId());
      if(null != integer)
      {
        countConnections.addAll(integer);
        mutant.border.remove(i.getId());
      }
    }
    assert(0 < countConnections.size());
    assert(countConnections.equals(geometry.countConnections(created.geometry)));
    mutant.border.put(created.getId(), countConnections);
    return mutant;
  }

  public IslandNode capture() {
    IslandNode mutant = new IslandNode(0, geometry);
    mutant.border.putAll(border);
    return mutant;
  }

  public int getId() {
    return geometry.getId();
  }

  public int getPlayer() {
    return player;
  }

  public boolean isDead(GoGame game) {
    for(IslandNode n : neighbors(game).keySet())
    {
      if(0 == n.getPlayer())
      {
        return false;
      }
    }
    return true;
  }

  public boolean surrounds(IslandNode space) {
    if(space.border.size() > 1) return false;
    return space.border.containsKey(getId());
  }

  public Map<IslandNode, Set<Tile>> neighbors(GoGame game) {
    Map<IslandNode, Set<Tile>> arrayList = new HashMap<IslandNode, Set<Tile>>();
    for(Entry<Integer, Set<Tile>> i : border.entrySet())
    {
      IslandNode key = game.islands.get(i.getKey());
      if(null == key)
      {
        assert(null != key);
      }
      arrayList.put(key, i.getValue());
    }
    return arrayList;
  }

  @Override
  public int hashCode() {
    return getId();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    IslandNode other = (IslandNode) obj;
    if (border == null) {
      if (other.border != null) return false;
    } else if (!border.equals(other.border)) return false;
    if (geometry == null) {
      if (other.geometry != null) return false;
    } else if (!geometry.equals(other.geometry)) return false;
    if (player != other.player) return false;
    return true;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("IslandNode [player=");
    builder.append(player);
    builder.append(", geometry=");
    builder.append(geometry);
    builder.append(", border=");
    builder.append(border);
    builder.append("]");
    return builder.toString();
  }

  public double getFreedom(GoGame game) {
    int freedom = 0;
    // TODO: This gives too high a value to freedom, need to track tiles in each border
    for(Entry<IslandNode, Set<Tile>> n : neighbors(game).entrySet())
    {
      if(0 == n.getKey().getPlayer())
      {
        freedom += n.getValue().size();
      }
    }
    return freedom;
  }

  public Boolean isTerritory(int forPlayer, GoGame game, Set<Integer> consideredIslands) {
    consideredIslands.add(getId());
    boolean foundFreindly = false;
    for(Entry<Integer, Set<Tile>> e : border.entrySet())
    {
      IslandNode n = game.islands.get(e.getKey());
      int p = n.getPlayer();
      if(0 != p)
      {
        if(p == forPlayer) 
        {
          foundFreindly = true;
        }
        else
        {
          return null;
        }
      }
      else if(!consideredIslands.contains(n.getId()))
      {
        Boolean territory = n.isTerritory(forPlayer, game, consideredIslands);
        if(null == territory) return null;
        if(territory) foundFreindly = true;
      }
    }
    return foundFreindly;
  }

  public boolean isTerritory(int forPlayer, GoGame game) {
    Boolean foundFreindly = isTerritory(forPlayer, game, new HashSet<Integer>());
    if(null == foundFreindly) return false;
    return foundFreindly;
  }

  public Collection<IslandNode> getConnectedMatching(GoGame game) {
    HashSet<IslandNode> set = new HashSet<IslandNode>();
    getConnectedMatching(game, set);
    return set;
  }

  public void getConnectedMatching(GoGame game, HashSet<IslandNode> set) {
    set.add(this);
    for(IslandNode n : new HashSet<IslandNode>(neighbors(game).keySet()))
    {
      int p = n.getPlayer();
      if(0 == p && !set.contains(n))
      {
        n.getConnectedMatching(game, set);
      }
    }
  }
  
}
