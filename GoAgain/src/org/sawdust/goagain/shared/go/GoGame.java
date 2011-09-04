package org.sawdust.goagain.shared.go;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.sawdust.goagain.shared.GameCommand;
import org.sawdust.goagain.shared.boards.BoardLayout;

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
    public final Tile tile;

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
  public Map<Integer, IslandNode> islands = new HashMap<Integer, IslandNode>();
  
  public int currentPlayer = 1;
  public boolean scorePrisoners = true;
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

  protected GoGame(GoGame game) {
    layout = game.layout;
    layout.assertValidIds();
    islands.putAll(game.islands);
    previousStates.addAll(game.previousStates);
    for(int i=0;i<numberOfPlayers();i++) prisoners[i] = game.prisoners[i];
    currentPlayer = game.currentPlayer;
    winner = game.winner;
    passesInARow = game.passesInARow;
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
    add(new IslandNode(0, new IslandGeometry(layout.getTiles().values()), null));
    assert(isValid());
  }

  private void add(IslandNode islandNode) {
    islands.put(islandNode.getId(), islandNode);
  }

  public IslandNode getIsland(Tile t) {
    for(IslandNode i : islands.values())
    {
      if(i.geometry.contains(t)) return i;
    }
    return null;
  }

  public ArrayList<GameCommand<GoGame>> getMoves() {
    ArrayList<GameCommand<GoGame>> list = new ArrayList<GameCommand<GoGame>>();
    if(null == winner)
    {
      for(final IslandNode i : islands.values())
      {
        if(0 == i.getPlayer())
        {
          for(final Tile tile : i.geometry.getPositions())
          {
            list.add(new Move(tile));
          }
        }
      }
    }
    list.add(new PassMove());
    return list;
  }

  public int getState(Tile tile) {
    if(null == tile) return -1;
    IslandNode island = getIsland(tile);
    if(null == island) return 0;
    return island.getPlayer();
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
      int highestInt = 0;
      char data[] = new char[1000];
      for(IslandNode i : islands.values())
      {
        char c;
        int player = i.getPlayer();
        if(0 == player)
        {
          c = ' ';
        }
        else
        {
          c = Integer.toString(player).toCharArray()[0];
        }
        for(Tile t : i.geometry.getPositions())
        {
          int x = t.idx;
          if(x > highestInt) highestInt = x;
          data[x] = c;
        }
      }
      hash = new String(data, 0, highestInt+1);
      //System.out.println("Game Hash: " + hash);
    }
    return hash;
  }

  public void play(final Tile tile) {
    IslandNode thisIsland = getIsland(tile);
    if(0 != thisIsland.getPlayer()) throw new RuntimeException("Tile occupied!");
    passesInARow = 0;
    GoGame backup = this.cloneGame();
    // Split the whitespace island
    Collection<IslandNode> splitNeighbors = thisIsland.neighbors(this);
    Collection<IslandNode> splitIslands = new ArrayList<IslandNode>();
    Collection<IslandGeometry> split = thisIsland.geometry.remove(tile);
    Set<IslandGeometry> splitNeighborhood = new HashSet<IslandGeometry>(split);
    for(IslandNode n : splitNeighbors)
    {
      splitNeighborhood.add(n.geometry);
    }
    IslandGeometry moveGeometry = new IslandGeometry(tile);
    splitNeighborhood.add(moveGeometry);
    for(IslandGeometry i : split)
    {
      splitIslands.add(new IslandNode(0, i, splitNeighborhood));
    }
    IslandNode moveNode = new IslandNode(currentPlayer, moveGeometry, splitNeighborhood);
    splitIslands.add(moveNode);
    islands.remove(thisIsland.getId());
    for(IslandNode i : splitIslands)
    {
      islands.put(i.getId(), i);
    }
    // Join new adjacent compatible islands
    Collection<IslandNode> toJoin = new ArrayList<IslandNode>();
    Collection<IslandNode> possiblyCaptured = new ArrayList<IslandNode>();
    toJoin.add(moveNode);
    for(IslandNode i : splitNeighbors)
    {
      islands.remove(i.getId());
      IslandNode replace = i.replace(thisIsland, splitIslands);
      islands.put(i.getId(), replace);
      if(replace.border.containsKey(moveNode.getId()))
      {
        if(i.getPlayer() == currentPlayer)
        {
          toJoin.add(i);
        }
        else
        {
          possiblyCaptured.add(i);
        }
      }
    }
    if(1 < toJoin.size())
    {
      Set<IslandGeometry> joinNeighbors = new HashSet<IslandGeometry>();
      Set<Tile> joinedTiles = new HashSet<Tile>();
      for(IslandNode i : toJoin)
      {
        for(IslandNode n : i.neighbors(this))
        {
          joinNeighbors.add(n.geometry);
        }
        joinedTiles.addAll(i.geometry.getPositions());
      }
      for(IslandNode i : toJoin)
      {
        joinNeighbors.remove(i.geometry);
      }
      IslandNode joinedIsland = new IslandNode(currentPlayer, new IslandGeometry(joinedTiles), joinNeighbors);
      for(IslandGeometry i : joinNeighbors)
      {
        int id = i.getId();
        IslandNode node = islands.get(id);
        islands.remove(id);
        islands.put(id, node.replace(toJoin, joinedIsland));
      }
      for(IslandNode i : toJoin) islands.remove(i.getId());
      add(joinedIsland);
      possiblyCaptured.add(joinedIsland);
    }
    else
    {
      possiblyCaptured.add(moveNode);
    }
    // Capture surrounded islands
    for(IslandNode i : possiblyCaptured)
    {
      int id = i.getId();
      if(islands.get(id).isDead(this))
      {
        islands.remove(id);
        islands.put(id, i.capture());
      }
    }
    // Check if move violates Ko rule
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
    assert(isValid());
    if(++currentPlayer == 3) currentPlayer = 1;
  }

  private boolean isValid() {
    System.out.println("\nNew Island config:");
    for(IslandNode i : islands.values())
    {
      System.out.println(i.toString());
    }
    Set<Tile> allTiles = new HashSet<Tile>(layout.getTiles().values());
    for(IslandNode i : islands.values())
    {
      for(IslandNode n : i.neighbors(this))
      {
        if(null == n)
        {
          return false;
        }
      }
      for(Tile t : i.geometry.positions)
      {
        if(!allTiles.contains(t)) 
        {
          return false;
        }
        allTiles.remove(t);
      }
    }
    if(0 != allTiles.size())
    {
      return false;
    }
    return true;
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
    for(IslandNode island : islands.values())
    {
      if(island.getPlayer() == player)
      {
        territory += island.geometry.getSize();
      }
      else if(island.getPlayer() == 0) 
      {
        if(isTerritory(player, island))
        {
          territory += island.geometry.getSize();
        }
      }
    }
    return territory;
  }

  protected boolean isTerritory(int player, IslandNode island) {
    boolean touchesSelf = false;
    boolean touchesOther = false;
    // TODO: Update
    for(Tile n : island.geometry.getPerimiter())
    {
      int state = getState(n);
      if(state == player)
      {
        touchesSelf = true;
      }
      else if(0 != state)
      {
        touchesOther = true;
      }
    }
    boolean isTerritory = touchesSelf && !touchesOther;
    return isTerritory;
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

  public GoGame cloneGame() {
    return new GoGame(this);
  }
  
}
