package org.sawdust.goagain.shared.go;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("serial")
public class IslandGeometry implements Serializable {
  boolean freeSpace = false;
  public HashSet<Tile> perimiter = new HashSet<Tile>();
  HashSet<Tile> positions = new HashSet<Tile>();

  protected IslandGeometry() {
    super();
  }

  public IslandGeometry(Tile tile) {
    this(new Tile[] { tile });
  }

  public IslandGeometry(IslandGeometry... array) {
    for (IslandGeometry i : array) {
      positions.addAll(i.positions);
      perimiter.addAll(i.perimiter);
    }
    perimiter.removeAll(positions);
  }

  public IslandGeometry(Collection<Tile> tiles) {
    this(tiles.toArray(new Tile[] {}));
  }

  public IslandGeometry(Tile... tiles) {
    for (Tile tile : tiles) {
      positions.add(tile);
      Collection<Tile> neighbors = tile.neighbors();
      if (null != neighbors) perimiter.addAll(neighbors);
    }
    perimiter.removeAll(positions);
  }

  public boolean contains(final Tile p) {
    return positions.contains(p);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    IslandGeometry other = (IslandGeometry) obj;
    if (positions == null) {
      if (other.positions != null) return false;
    } else if (!positions.equals(other.positions)) return false;
    return true;
  }

  public Collection<Tile> getPerimiter() {
    return perimiter;
  }

  public Collection<Tile> getPositions() {
    return positions;
  }

  public int getSize() {
    return getPositions().size();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((positions == null) ? 0 : positions.hashCode());
    return result;
  }

  private transient IslandGeometry thin = null;

  public IslandGeometry thin() {
    if (null == thin) {
      Set<Tile> tiles = new HashSet<Tile>(getPositions());
      for (Tile t : getPerimiter()) {
        tiles.removeAll(t.neighbors());
      }
      thin = new IslandGeometry(tiles.toArray(new Tile[] {}));
    }
    return thin;
  }

  Integer id = null;

  public int getId() {
    if (null == id) {
      for (Tile t : getPositions()) {
        if (null == id || t.idx < id) id = t.idx;
      }
    }
    return id;
  }

  public Set<Tile> countConnections(IslandGeometry geometry) {
    Set<Tile> tiles = new HashSet<Tile>(geometry.getPositions());
    tiles.retainAll(getPerimiter());
    return tiles;
//    int count = 0;
//    for (Tile p : tiles) {
//      for (Tile n : p.neighbors()) {
//        if (geometry.getPositions().contains(n)) count++;
//      }
//    }
//    return count;
  }

  //Map<Tile, Collection<IslandGeometry>> splitCache = new HashMap<Tile, Collection<IslandGeometry>>();

  public Collection<IslandGeometry> remove(Tile tile) {
    //if (splitCache.containsKey(tile)) return splitCache.get(tile);
    ArrayList<IslandGeometry> list = new ArrayList<IslandGeometry>();
    Set<Tile> tilesLeft = new HashSet<Tile>(getPositions());
    tilesLeft.remove(tile);
    while (tilesLeft.size() > 0) {
      Set<Tile> region = extractRegion(tilesLeft);
      list.add(new IslandGeometry(region));
      tilesLeft.removeAll(region);
    }
    //assert (checkSplit(tile, list));
    //splitCache.put(tile, list);
    return list;
  }

  private boolean checkSplit(Tile tile, ArrayList<IslandGeometry> list) {
    Set<Tile> allTiles = new HashSet<Tile>(getPositions());
    if(!allTiles.contains(tile))
    {
      return false;
    }
    allTiles.remove(tile);
    for (IslandGeometry i : list) {
      assert (!i.getPositions().contains(tile));
      for (Tile t : i.getPositions()) {
        if(!allTiles.contains(t))
        {
          return false;
        }
        allTiles.remove(t);
      }
    }
    if(0 < allTiles.size())
    {
      return false;
    }
    return true;
  }

  public static Set<Tile> extractRegion(Set<Tile> availible) {
    return extractRegion(availible, availible.iterator().next(), Integer.MAX_VALUE);
  }

  public static Set<Tile> extractRegion(Set<Tile> availible, Tile first, int generations) {
    Set<Tile> currentIsland = new HashSet<Tile>();
    Set<Tile> newIsland = new HashSet<Tile>();
    newIsland.add(first);
    while (newIsland.size() > 0) {
      if(generations-- == 0) break;
      currentIsland.addAll(newIsland);
      Set<Tile> newBorder = new HashSet<Tile>();
      for (Tile t : newIsland) {
        for (Tile n : t.neighbors()) {
          if (!currentIsland.contains(n) && availible.contains(n)) {
            newBorder.add(n);
          }
        }
      }
      newIsland = newBorder;
    }
    return currentIsland;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("IslandGeometry [");
    builder.append("getId()=");
    builder.append(getId());
    builder.append(", ");
    builder.append("getSize()=");
    builder.append(getSize());
    builder.append(", ");
    builder.append("positions=");
    builder.append(positions);
    builder.append("]");
    return builder.toString();
  }

  public Collection<IslandGeometry> partition(int size) {
    Set<Tile> allTiles = new HashSet<Tile>(getPositions());
    ArrayList<IslandGeometry> list = new ArrayList<IslandGeometry>();
    while(allTiles.size() > 0)
    {
      Set<Tile> region = extractRegion(allTiles, allTiles.iterator().next(), size);
      list.add(new IslandGeometry(region));
      allTiles.removeAll(region);
    }
    return list;
  }

}
