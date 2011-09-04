package org.sawdust.goagain.shared.go;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class IslandNode {

  public final IslandGeometry geometry;
  public final int player;
  public final Map<Integer,Integer> border = new HashMap<Integer, Integer>();

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
        Integer countConnections = geometry.countConnections(n);
        if(0 < countConnections)
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
      Integer countConnections = geometry.countConnections(i.geometry);
      if(0 < countConnections)
      {
        mutant.border.put(i.getId(), countConnections);
      }
    }
    return mutant;
  }

  public IslandNode replace(Collection<IslandNode> toJoin, IslandNode created) {
    IslandNode mutant = new IslandNode(player, geometry);
    mutant.border.putAll(border);
    int countConnections = 0;
    for(IslandNode i : toJoin)
    {
      Integer integer = mutant.border.get(i.getId());
      if(null != integer)
      {
        countConnections += integer;
        mutant.border.remove(i.getId());
      }
    }
    assert(0 < countConnections);
    assert(countConnections == (int)geometry.countConnections(created.geometry));
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
    Collection<IslandNode> neighbors = neighbors(game);
    for(IslandNode n : neighbors)
    {
      if(0 == n.getPlayer())
      {
        return false;
      }
    }
    return true;
  }

  public boolean surrounds(IslandNode space) {
    return false;
  }

  public Collection<IslandNode> neighbors(GoGame game) {
    ArrayList<IslandNode> arrayList = new ArrayList<IslandNode>();
    for(int i : border.keySet())
    {
      arrayList.add(game.islands.get(i));
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
  
}
