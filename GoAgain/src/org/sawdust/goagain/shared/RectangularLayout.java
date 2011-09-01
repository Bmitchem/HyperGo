package org.sawdust.goagain.shared;

import java.util.ArrayList;
import java.util.TreeMap;

@SuppressWarnings("serial")
public class RectangularLayout extends BoardLayout {
  public int tileCols;
  public int tileRows;
  public TreeMap<Integer, Tile> tiles;

  public RectangularLayout() {
    this(9,9);
  }

  public RectangularLayout(int tileCols, int tileRows) {
    super();
    this.tileCols = tileCols;
    this.tileRows = tileRows;
    calculateLayout();
  }

  public void calculateLayout() {
    TreeMap<Integer, Tile> _tiles = new TreeMap<Integer, Tile>();
    @SuppressWarnings("unchecked") final ArrayList<Tile>[][] lists = new ArrayList[tileRows][tileCols];
    for(int i=1; i<1+tileRows; i++)
    {
      for(int j=1; j<1+tileCols; j++)
      {
        final ArrayList<Tile> list = new ArrayList<Tile>();
        Tile tile = new GoTile(getTileId(i, j), (1. / (1+tileRows)) * i, (1. / (1+tileCols)) * j, list);
        lists[i-1][j-1] = list;
        _tiles.put(tile.idx, tile);
      }
    }
    for(int i=1; i<1+tileRows; i++)
    {
      for(int j=1; j<1+tileCols; j++)
      {
        final ArrayList<Tile> list = lists[i-1][j-1];
        if(1 < i) list.add(_tiles.get(getTileId((i-1), j)));
        if(tileRows > i) list.add(_tiles.get(getTileId((i+1), j)));
        if(1 < j) list.add(_tiles.get(getTileId(i, (j-1))));
        if(tileCols > j) list.add(_tiles.get(getTileId(i, (j+1))));
      }
    }
    tiles = _tiles;
  }

  protected int getTileId(final int i, final int j) {
    return (j-1) + ((i-1)*tileCols);
  }

  @Override
  public TreeMap<Integer, Tile> getTiles() {
    return tiles;
  }

  @Override
  public double getScale() {
    return 18. / (tileCols + tileCols);
  }

}
