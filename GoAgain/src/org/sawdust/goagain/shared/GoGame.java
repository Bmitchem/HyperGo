package org.sawdust.goagain.shared;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

@SuppressWarnings("serial")
public class GoGame implements Serializable {

  public static final class GoTile extends Tile {
    private ArrayList<Tile> list;

    protected GoTile() {
      super();
    }

    public GoTile(int idx, double x, double y, ArrayList<Tile> list) {
      super(idx, x, y);
      this.list = list;
    }

    @Override
    public Collection<Tile> neighbors() {
      return list;
    }
  }

  public static final class Move extends GameCommand<GoGame> {
    final Tile tile;

    public Move(Tile tile) {
      this.tile = tile;
    }

    @Override
    public String getCommandText() {
      return tile.toString();
    }

    @Override
    public void move(GoGame board) {
      board.occupy(tile, board.currentPlayer);
    }
  }

  public int currentPlayer = 1;
  public List<Island> islands = new ArrayList<Island>();
  public int[] points = {0, 0};
  public int tileCols = 10;
  public int tileRows = 10;
  public Map<Integer, Tile> tiles;
  Map<Integer, Integer> tileState = new HashMap<Integer, Integer>();
  public Integer winner = null;
  
  public GoGame() {
    reset();
  }

  public GoGame(GoGame game) {
    tiles = game.tiles;
    tileState.putAll(game.tileState);
    islands.addAll(game.islands);
    for(int i=0;i<points.length;i++) points[i] = game.points[i];
    currentPlayer = game.currentPlayer;
    winner = game.winner;
  }
  protected Map<Integer, Tile> calculateLayout() {
    Map<Integer, Tile> _tiles = new HashMap<Integer, Tile>();
    @SuppressWarnings("unchecked") final ArrayList<Tile>[][] lists = new ArrayList[tileRows][tileCols];
    for(int i=1; i<1+tileRows; i++)
    {
      for(int j=1; j<1+tileCols; j++)
      {
        final ArrayList<Tile> list = new ArrayList<Tile>();
        Tile tile = new GoTile(getTileId(i, j), (1. / (1+tileCols)) * i, (1. / (1+tileRows)) * j, list);
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
    return _tiles;
  }

  private Island getIsland(Tile t) {
    for(Island i : islands)
    {
      if(i.contains(t)) return i;
    }
    return null;
  }

  public ArrayList<GameCommand<GoGame>> getMoves() {
    ArrayList<GameCommand<GoGame>> list = new ArrayList<GameCommand<GoGame>>();
    for(final Tile tile : tiles.values())
    {
      if(!tileState.containsKey(tile.idx))
      {
        boolean isSurrounded = true;
        boolean isLiberty = true;
        for(Tile n : tile.neighbors())
        {
          Integer state = tileState.get(n.idx);
          if(state == null)
          {
            isSurrounded = false;
            isLiberty = false;
          }
          else if(state.equals(currentPlayer))
          {
            isSurrounded = false;
          }
          else
          {
            isLiberty = false;
          }
        }
        if(isSurrounded) continue;
        if(isLiberty) continue;
        list.add(new Move(tile));
      }
    }
    return list;
  }

  public int getState(Tile tile) {
    if(null == tile) return -1;
    if(null == tileState) return -1;
    if(!tileState.containsKey(tile.idx)) return 0;
    Integer integer = tileState.get(tile.idx);
    return (null==integer)?0:integer;
  }

  protected int getTileId(final int i, final int j) {
    return i + (j*tileCols);
  }

  public Tile nearestTile(double x, double y, int width, int height) {
    double size = (width<height)?width:height;
    x /= size; y /= size;
    Tile tile = null;
    double dist = Double.MAX_VALUE;
    for(Tile t : tiles.values())
    {
      double dx = t.x - x;
      double dy = t.y - y;
      double d = Math.sqrt(dx*dx + dy*dy);
      if(d < dist)
      {
        dist = d;
        tile = t;
      }
    }
    return tile;
  }

  public void occupy(Tile tile, int state) {
    if(0 != getState(tile)) return;
    tileState.put(tile.idx, state);
    ArrayList<Island> possiblyDeadIslands = new ArrayList<Island>();
    HashSet<Island> adjacentIslands = new HashSet<Island>();
    for(Tile t : tile.neighbors())
    {
      if(getState(t) == state)
      {
        adjacentIslands.add(getIsland(t));
      }
      else if(0 != getState(t))
      {
        possiblyDeadIslands.add(getIsland(t));
      }
    }
    for(Island island : adjacentIslands)
    {
      islands.remove(island);
    }
    Island e = new Island(this, tile, adjacentIslands.toArray(new Island[]{}));
    islands.add(e);
    possiblyDeadIslands.add(e);
    for(Island island : possiblyDeadIslands)
    {
      if(island.isDead(this))
      {
        islands.remove(island);
        for(Tile t : island.getPositions())
        {
          int capturedSide = getState(t);
          if(0 != capturedSide)
          {
            points[capturedSide-1]--;
            tileState.remove(t.idx);
          }
        }
      }
    }
    if(0 == getMoves().size())
    {
      int winningPoints = Integer.MIN_VALUE;
      for(int i=0;i<points.length;i++)
      {
        if(points[i] > winningPoints)
        {
          winner = i;
          winningPoints = points[i];
        }
      }
    }
    if(++currentPlayer == 3) currentPlayer = 1;
  }

  public void reset() {
    tiles = calculateLayout();
    tileState.clear();
    islands.clear();
    winner = null;
  }

}
