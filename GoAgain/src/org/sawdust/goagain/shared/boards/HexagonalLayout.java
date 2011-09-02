package org.sawdust.goagain.shared.boards;

import java.util.ArrayList;
import java.util.TreeMap;

import org.sawdust.goagain.shared.go.GoTile;
import org.sawdust.goagain.shared.go.Tile;

@SuppressWarnings("serial")
public class HexagonalLayout extends BoardLayout {
  public int size;
  public TreeMap<Integer, Tile> tiles;

  public HexagonalLayout() {
    this(15);
  }

  public HexagonalLayout(int size) {
    super();
    this.connectivity = 3;
    this.size = size;
    calculateLayout();
  }

  public void calculateLayout() {
    TreeMap<Integer, Tile> _tiles = new TreeMap<Integer, Tile>();
    @SuppressWarnings("unchecked") final ArrayList<Tile>[][] lists = new ArrayList[size][size];
    for(int i=1; i<1+size; i++)
    {
      for(int j=1; j<1+size; j++)
      {
        final ArrayList<Tile> list = new ArrayList<Tile>();
        double unitLength = (1. / (1+size))/2;
        double x = (unitLength * i) + unitLength;
        double y = (unitLength * 2 * j);
        if(0==(j%2))
        {
          x -= unitLength;
          x += ((int)(i/2)) * 2 * unitLength;
        }
        else
        {
          x += ((int)((i-1)/2)) * 2 * unitLength;
        }
        Tile tile = new GoTile(getTileId(i, j), x, y, list);
        lists[i-1][j-1] = list;
        _tiles.put(tile.idx, tile);
      }
    }
    for(int i=1; i<1+size; i++)
    {
      for(int j=1; j<1+size; j++)
      {
        final ArrayList<Tile> list = lists[i-1][j-1];
        if((0==(j%2)) == (0==(i%2)))
        {
          //if(1 < i) list.add(_tiles.get(getTileId((i-1), j)));
          if(size > i) list.add(_tiles.get(getTileId((i+1), j)));
          if(1 < j) list.add(_tiles.get(getTileId(i, (j-1))));
          if(size > j) list.add(_tiles.get(getTileId(i, (j+1))));
        }
        else
        {
          if(1 < i) list.add(_tiles.get(getTileId((i-1), j)));
          //if(size > i) list.add(_tiles.get(getTileId((i+1), j)));
          if(1 < j) list.add(_tiles.get(getTileId(i, (j-1))));
          if(size > j) list.add(_tiles.get(getTileId(i, (j+1))));
        }
      }
    }
    tiles = _tiles;
  }

  protected int getTileId(final int i, final int j) {
    return (j-1) + ((i-1)*size);
  }

  @Override
  public TreeMap<Integer, Tile> getTiles() {
    return tiles;
  }

  @Override
  public double getScale() {
    return 9. / size;
  }

}
