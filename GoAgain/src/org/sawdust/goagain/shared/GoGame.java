package org.sawdust.goagain.shared;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

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
      board.play(tile);
    }
  }

  public Set<String> previousStates = new TreeSet<String>();
  public int currentPlayer = 1;
  public List<Island> islands = new ArrayList<Island>();
  public int passesInARow = 0;
  public int[] prisoners = {0, 0};
  public int tileCols = 9;
  public int tileRows = 9;
  public TreeMap<Integer, Tile> tiles;
  Map<Integer, Integer> tileState = new HashMap<Integer, Integer>();
  public Integer winner = null;
  
  public GoGame() {
    reset();
  }

  public GoGame(GoGame game) {
    tiles = game.tiles;
    tileState.putAll(game.tileState);
    islands.addAll(game.islands);
    previousStates.addAll(game.previousStates);
    for(int i=0;i<numberOfPlayers();i++) prisoners[i] = game.prisoners[i];
    currentPlayer = game.currentPlayer;
    winner = game.winner;
  }

  private void reset(GoGame backup) {
    islands = backup.islands;
    tileState = backup.tileState;
    prisoners = backup.prisoners;
    winner = backup.winner;
    currentPlayer = backup.currentPlayer;
  }

  public void reset() {
    tiles = calculateLayout();
    previousStates.clear();
    tileState.clear();
    islands.clear();
    prisoners = new int[]{0,0};
    winner = null;
  }

  protected TreeMap<Integer, Tile> calculateLayout() {
    TreeMap<Integer, Tile> _tiles = new TreeMap<Integer, Tile>();
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
        list.add(new Move(tile));
      }
    }
    list.add(new GameCommand<GoGame>(){
      @Override
      public void move(GoGame board) {
        board.pass();
      }
      @Override
      public String getCommandText() {
        return "Pass";
      }});
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
    double dist = 1.;
    for(Tile t : tiles.values())
    {
      double dx = (t.x - x) * tileCols;
      double dy = (t.y - y) * tileRows;
      double d = Math.sqrt(dx*dx + dy*dy);
      if(d < dist)
      {
        dist = d;
        tile = t;
      }
    }
    return tile;
  }

  public void pass() {
    if(++passesInARow >= 2)
    {
      decideWinner();
    }
    else
    {
      nextPlayer();
    }
  }


  volatile transient String hash = null;
  protected String getStateHash() {
    if(null == hash)
    {
      StringBuffer sb = new StringBuffer();
      for(Entry<Integer, Tile> tile : tiles.entrySet())
      {
        Integer obj = tileState.get(tile.getKey());
        if(null == obj)
        {
          sb.append(" ");
        }
        else
        {
          sb.append(obj);
        }
      }
      hash = sb.toString();
    }
    return hash;
  }

  public void play(Tile tile) {
    if(0 != getState(tile)) throw new RuntimeException("Tile occupied!");
    passesInARow = 0;
    GoGame backup = new GoGame(this);
    tileState.put(tile.idx, currentPlayer);
    ArrayList<Island> possiblyDeadIslands = new ArrayList<Island>();
    HashSet<Island> adjacentIslands = new HashSet<Island>();
    for(Tile t : tile.neighbors())
    {
      if(getState(t) == currentPlayer)
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
            prisoners[capturedSide-1]++;
            tileState.remove(t.idx);
          }
        }
      }
    }
    hash = null;
    String stateHash = getStateHash();
    if(previousStates.contains(stateHash)) {
      reset(backup);
      throw new RuntimeException("Move Violates Ko");
    }
    else
    {
      previousStates.add(stateHash);
    }
    nextPlayer();
  }

  protected void nextPlayer() {
    if(++currentPlayer == 3) currentPlayer = 1;
  }

  protected void decideWinner() {
    int winningPoints = Integer.MIN_VALUE;
    for(int i=0;i<numberOfPlayers();i++)
    {
      int score = getScore(i+1);
      if(score > winningPoints)
      {
        winner = i;
        winningPoints = score;
      }
    }
  }

  public int numberOfPlayers() {
    return prisoners.length;
  }

  public int getScore(int player) {
    return getTerritory(player) + prisoners[player==1?1:0];
  }

  public int getTerritory(int player) {
    int territory = 0;
    for(Entry<Integer, Integer> t : tileState.entrySet())
    {
      Integer value = t.getValue();
      if(value.equals(player))
      {
        territory++;
      }
    }
    
    Set<Tile> remainingEmptySpace = new HashSet<Tile>(tiles.values());
    for(int i : tileState.keySet())
    {
      remainingEmptySpace.remove(tiles.get(i));
    }
    while(remainingEmptySpace.size() > 0)
    {
      Set<Tile> currentIsland = new HashSet<Tile>();
      Set<Tile> newIsland = new HashSet<Tile>();
      newIsland.add(remainingEmptySpace.iterator().next());
      boolean touchesSelf = false;
      boolean touchesOther = false;
      while(newIsland.size() > 0)
      {
        Set<Tile> newBorder = new HashSet<Tile>();
        for(Tile t : newIsland)
        {
          newBorder.addAll(t.neighbors());
        }
        currentIsland.addAll(newIsland);
        newBorder.removeAll(currentIsland);
        newIsland.clear();
        for(Tile n : newBorder)
        {
          Integer state = tileState.get(n.idx);
          if(null != state)
          {
            if(state.equals(player))
            {
              touchesSelf = true;
            }
            else
            {
              touchesOther = true;
            }
          }
          else
          {
            newIsland.add(n);
          }
        }
      }
      if(touchesSelf && !touchesOther)
      {
        territory += currentIsland.size();
      }
      remainingEmptySpace.removeAll(currentIsland);
    }
    return territory;
  }

  @Override
  public int hashCode() {
    return getStateHash().hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    GoGame other = (GoGame) obj;
    if (currentPlayer != other.currentPlayer) return false;
    if (hash == null) {
      if (other.hash != null) return false;
    } else if (!hash.equals(other.hash)) return false;
    if (passesInARow != other.passesInARow) return false;
    if (previousStates == null) {
      if (other.previousStates != null) return false;
    } else if (!previousStates.equals(other.previousStates)) return false;
    if (!Arrays.equals(prisoners, other.prisoners)) return false;
    if (tileCols != other.tileCols) return false;
    if (tileRows != other.tileRows) return false;
    if (winner == null) {
      if (other.winner != null) return false;
    } else if (!winner.equals(other.winner)) return false;
    return true;
  }
  
}
