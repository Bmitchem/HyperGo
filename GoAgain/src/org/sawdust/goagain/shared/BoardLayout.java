package org.sawdust.goagain.shared;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

@SuppressWarnings("serial")
public abstract class BoardLayout implements Serializable {

  public static final Map<String, BoardLayout> layouts = new HashMap<String, BoardLayout>();
  static {
    layouts.put("Square", new RectangularLayout());
    layouts.put("Triangle", new TriangularLayout());
    layouts.put("Hexagonal", new HexagonalLayout());
  }

  public abstract TreeMap<Integer, Tile> getTiles();

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
