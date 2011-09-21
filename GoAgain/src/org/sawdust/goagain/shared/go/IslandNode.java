package org.sawdust.goagain.shared.go;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

@SuppressWarnings("serial")
public class IslandNode<T> implements Serializable {

  public IslandGeometry geometry;
  public T player;
  public Map<Integer,Set<Tile>> border = new HashMap<Integer, Set<Tile>>();

  protected IslandNode() {
    super();
  }

  private IslandNode(T player, IslandGeometry geometry) {
    super();
    this.player = player;
    this.geometry = geometry;
  }

  public IslandNode(T player, IslandGeometry geometry, Collection<IslandGeometry> neighbors) {
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

  public IslandNode<T> replace(IslandNode<T> thisIsland, Collection<IslandNode<T>> created) {
    IslandNode<T> mutant = new IslandNode<T>(player, geometry);
    mutant.border.putAll(border);
    mutant.border.remove(thisIsland.getId());
    for(IslandNode<T> i : created)
    {
      Set<Tile> countConnections = geometry.countConnections(i.geometry);
      if(0 < countConnections.size())
      {
        mutant.border.put(i.getId(), countConnections);
      }
    }
    return mutant;
  }

  public IslandNode<T> replace(Collection<IslandNode<T>> toJoin, IslandNode<T> created) {
    IslandNode<T> mutant = new IslandNode<T>(player, geometry);
    mutant.border.putAll(border);
    Set<Tile> countConnections = new HashSet<Tile>();
    for(IslandNode<T> i : toJoin)
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

  public IslandNode<T> capture() {
    IslandNode<T> mutant = new IslandNode<T>(null, geometry);
    mutant.border.putAll(border);
    return mutant;
  }

  public int getId() {
    return geometry.getId();
  }

  public T getPlayer() {
    return player;
  }

  public boolean isDead(GoGame game) {
    for(IslandNode<Integer> n : neighbors(game).keySet())
    {
      if(null == n.getPlayer())
      {
        return false;
      }
    }
    return true;
  }

  public boolean surrounds(IslandNode<T> space) {
    if(space.border.size() > 1) return false;
    return space.border.containsKey(getId());
  }

  public Map<IslandNode<T>, Set<Tile>> neighbors(Map<Integer, IslandNode<T>> map) {
    Map<IslandNode<T>, Set<Tile>> arrayList = new HashMap<IslandNode<T>, Set<Tile>>();
    for(Entry<Integer, Set<Tile>> i : border.entrySet())
    {
      IslandNode<T> key = map.get(i.getKey());
      if(null == key)
      {
        assert(null != key);
      }
      arrayList.put(key, i.getValue());
    }
    return arrayList;
  }

  @SuppressWarnings("unchecked")
  public Map<IslandNode<Integer>, Set<Tile>> neighbors(GoGame game) {
    return ((IslandNode<Integer>)this).neighbors(game.islands);
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
    IslandNode<?> other = (IslandNode<?>) obj;
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
    for(Entry<IslandNode<Integer>, Set<Tile>> n : neighbors(game).entrySet())
    {
      if(null == n.getKey().getPlayer())
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
      IslandNode<Integer> n = game.islands.get(e.getKey());
      Integer p = n.getPlayer();
      if(null != p)
      {
        if(p.equals(forPlayer)) 
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

  public Collection<IslandNode<Integer>> getConnectedMatching(GoGame game) {
    HashSet<IslandNode<Integer>> set = new HashSet<IslandNode<Integer>>();
    getConnectedMatching(game, set);
    return set;
  }

  @SuppressWarnings("unchecked")
  public void getConnectedMatching(GoGame game, HashSet<IslandNode<Integer>> set) {
    set.add((IslandNode<Integer>) this);
    for(IslandNode<Integer> n : new HashSet<IslandNode<Integer>>(neighbors(game).keySet()))
    {
      Integer p = n.getPlayer();
      if(null == p && !set.contains(n))
      {
        n.getConnectedMatching(game, set);
      }
    }
  }
  
}
