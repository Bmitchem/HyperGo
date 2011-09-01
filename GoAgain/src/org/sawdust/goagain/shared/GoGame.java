package org.sawdust.goagain.shared;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.sawdust.goagain.client.GoBoard;

@SuppressWarnings("serial")
public class GoGame implements Serializable {

  public static final class PassMove extends GameCommand<GoGame> {
    @Override
    public void move(GoGame board) {
      board.pass();
    }

    @Override
    public String getCommandText() {
      return "Pass";
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

  public BoardLayout layout = randomValue(BoardLayout.layouts);
  public Set<String> previousStates = new TreeSet<String>();
  public int currentPlayer = 1;
  public boolean scorePrisoners = true;
  public List<Island> islands = new ArrayList<Island>();
  public int passesInARow = 0;
  public int[] prisoners = {0, 0};
  public Integer winner = null;
  
  public GoGame() {
    reset();
  }

  public static <T,K> T randomValue(Map<K, T> map) {
    TreeMap<Double, K> randomOrder = new TreeMap<Double, K>();
    for(K key : map.keySet())
    {
      randomOrder.put(Math.random(), key);
    }
    T t = map.get(randomOrder.entrySet().iterator().next().getValue());
    return t;
  }

  public GoGame(GoGame game) {
    layout = game.layout;
    islands.addAll(game.islands);
    previousStates.addAll(game.previousStates);
    for(int i=0;i<numberOfPlayers();i++) prisoners[i] = game.prisoners[i];
    currentPlayer = game.currentPlayer;
    winner = game.winner;
  }

  private void reset(GoGame backup) {
    islands = backup.islands;
    prisoners = backup.prisoners;
    winner = backup.winner;
    currentPlayer = backup.currentPlayer;
  }

  public void reset() {
    previousStates.clear();
    islands.clear();
    prisoners = new int[]{0,0};
    winner = null;
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
    for(final Tile tile : layout.getTiles().values())
    {
      if(0 == getState(tile))
      {
        list.add(new Move(tile));
      }
    }
    list.add(new PassMove());
    return list;
  }

  public int getState(Tile tile) {
    if(null == tile) return -1;
    for(Island i : islands)
    {
      if(i.contains(tile)) return i.getPlayer();
    }
    return 0;
  }

  public Tile nearestTile(double x, double y, int width, int height) {
    double size = (width<height)?width:height;
    return layout.nearestTile(x / size, y /= size);
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
      int idx = 0;
      StringBuffer sb = new StringBuffer();
      for(Entry<Integer, Tile> tile : layout.getTiles().entrySet())
      {
        assert(tile.getKey().equals(idx++));
        int obj = getState(tile.getValue());
        if(0 == obj)
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
    Island newIsland;
    if (adjacentIslands.size() > 0) {
      newIsland = new Island(this, tile, adjacentIslands.toArray(new Island[] {}));
    }
    else
    {
      newIsland = new Island(this, tile, currentPlayer);
    }
    islands.add(newIsland);
    possiblyDeadIslands.add(newIsland);
    for(Island island : possiblyDeadIslands)
    {
      if(island.isDead(this))
      {
        islands.remove(island);
        prisoners[island.getPlayer()-1] += island.getPositions().size();
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
    int score = getTerritory(player);
    if(scorePrisoners)
    {
      score += prisoners[player==1?1:0];
    }
    return score;
  }

  public int getTerritory(int player) {
    int territory = 0;
    for(Island island : islands)
    {
      if(island.getPlayer() == player)
      {
        territory += island.getSize();
      }
    }
    
    Set<Tile> remainingEmptySpace = new HashSet<Tile>(layout.getTiles().values());
    for(Island island : islands)
    {
      remainingEmptySpace.removeAll(island.getPositions());
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
          int state = getState(n);
          if(0 != state)
          {
            if(state == player)
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
    if (layout == null) {
      if (other.layout != null) return false;
    } else if (!layout.equals(other.layout)) return false;
    if (hash == null) {
      if (other.hash != null) return false;
    } else if (!hash.equals(other.hash)) return false;
    if (passesInARow != other.passesInARow) return false;
    if (previousStates == null) {
      if (other.previousStates != null) return false;
    } else if (!previousStates.equals(other.previousStates)) return false;
    if (!Arrays.equals(prisoners, other.prisoners)) return false;
    if (winner == null) {
      if (other.winner != null) return false;
    } else if (!winner.equals(other.winner)) return false;
    return true;
  }
  
}
