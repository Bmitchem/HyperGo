package org.sawdust.goagain.shared.boards;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.sawdust.goagain.shared.go.Tile;

@SuppressWarnings("serial")
public abstract class BoardLayout implements Serializable {

  public static final Map<String, BoardLayout> layouts = new HashMap<String, BoardLayout>();
  static {
    layouts.put("Square", new RectangularLayout());
    layouts.put("Triangle", new TriangularLayout());
    layouts.put("Hexagonal", new HexagonalLayout());
  }

  public int connectivity;
  
  public abstract TreeMap<Integer, Tile> getTiles();

  public void assertValidIds() {
    int idx = 0;
    for(Entry<Integer, Tile> tile : getTiles().entrySet())
    {
      assert(tile.getKey().equals(idx++));
    }
  }

  public Tile nearestTile(double x, double y) {
    Tile tile = null;
    double dist = 1.;
    for(Tile t : getTiles().values())
    {
      double dx = (t.x - x);
      double dy = (t.y - y);
      double d = Math.sqrt(dx*dx + dy*dy);
      if(d < dist)
      {
        dist = d;
        tile = t;
      }
    }
    return tile;
  }

  public abstract double getScale();
}
