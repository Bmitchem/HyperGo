package org.sawdust.goagain.shared.go;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.sawdust.goagain.shared.GameCommand;
import org.sawdust.goagain.shared.boards.BoardLayout;
import org.sawdust.goagain.shared.go.ai.IslandContext;

@SuppressWarnings("serial")
public class GoGame implements Serializable {

  public static final class PassMove implements GameCommand<GoGame> {

    public GoGame move(GoGame board) {
      return board.pass();
    }

    public String getCommandText() {
      return "Pass";
    }

    @Override
    public int hashCode()
    {
      return 298304;
    }

    @Override
    public boolean equals(Object obj)
    {
      if (this == obj) return true;
      if (obj == null) return false;
      if (getClass() != obj.getClass()) return false;
      return true;
    }

    @Override
    public String toString()
    {
      StringBuilder builder = new StringBuilder();
      builder.append("PassMove [getCommandText()=");
      builder.append(getCommandText());
      builder.append("]");
      return builder.toString();
    }
    
  }

  public static final class Move implements GameCommand<GoGame> {
    public final Tile tile;

    public Move(Tile tile) {
      this.tile = tile;
    }

    public String getCommandText() {
      return tile.toString();
    }

    public GoGame move(GoGame board) {
      return board.play(tile);
    }

    @Override
    public int hashCode()
    {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((tile == null) ? 0 : tile.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj)
    {
      if (this == obj) return true;
      if (obj == null) return false;
      if (getClass() != obj.getClass()) return false;
      Move other = (Move) obj;
      if (tile == null)
      {
        if (other.tile != null) return false;
      }
      else if (!tile.equals(other.tile)) return false;
      return true;
    }

    @Override
    public String toString()
    {
      StringBuilder builder = new StringBuilder();
      builder.append("Move [tile=");
      builder.append(tile);
      builder.append("]");
      return builder.toString();
    }
    
  }

  private BoardLayout layout = Util.randomValue(BoardLayout.layouts);
  public Set<String> previousStates = new TreeSet<String>();
  public Map<Integer, IslandNode> islands = new HashMap<Integer, IslandNode>();
  
  public int currentPlayer = 1;
  public boolean scorePrisoners = true;
  public int passesInARow = 0;
  public int[] prisoners = {0, 0};
  public Integer winner = null;
  
  public GoGame() {
    Collection<IslandGeometry> partitions = new IslandGeometry(getLayout().getTiles().values()).partition(Integer.MAX_VALUE);
    for(IslandGeometry geometry : partitions)
    {
      add(new IslandNode(0, geometry, partitions));
    }
    assert(isValid());
  }

  protected GoGame(GoGame game) {
    layout = game.getLayout();
    getLayout().assertValidIds();
    islands.putAll(game.islands);
    previousStates.addAll(game.previousStates);
    for(int i=0;i<numberOfPlayers();i++) prisoners[i] = game.prisoners[i];
    currentPlayer = game.currentPlayer;
    winner = game.winner;
    passesInARow = game.passesInARow;
  }

  public GoGame(BoardLayout layout) {
    this.layout = layout;
    Collection<IslandGeometry> partitions = new IslandGeometry(getLayout().getTiles().values()).partition(Integer.MAX_VALUE);
    for(IslandGeometry geometry : partitions)
    {
      add(new IslandNode(0, geometry, partitions));
    }
  }

  public GoGame reset() {
    return new GoGame(this.layout);
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
          if(i.geometry.positions.size() == 1 && i.border.size() == 1)
          {
            Integer surroundingIslandId = i.border.keySet().iterator().next();
            IslandNode surroundingIsland = islands.get(surroundingIslandId);
            if(surroundingIsland.getPlayer() == currentPlayer)
            {
              continue;
            }
          }
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

  public Tile nearestTile(double x, double y, int width, int height) {
    double size = (width<height)?width:height;
    return getLayout().nearestTile(x / size, y /= size);
  }

  public GoGame pass() {
    GoGame nextGame = this.cloneGame();
    if(++nextGame.passesInARow >= 2)
    {
      nextGame.decideWinner();
    }
    else
    {
      nextGame.nextPlayer();
    }
    return nextGame;
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

  public GoGame play(final Tile tile) {
    IslandNode thisIsland = getIsland(tile);
    if(0 != thisIsland.getPlayer()) return null;
    GoGame nextGame = this.cloneGame();
    nextGame.passesInARow = 0;
    // Split the whitespace island
    Collection<IslandNode> splitNeighbors = thisIsland.neighbors(nextGame).keySet();
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
    nextGame.remove(thisIsland);
    for(IslandNode i : splitIslands)
    {
      nextGame.add(i);
    }
    // Join new adjacent compatible islands
    Collection<IslandNode> toJoin = new ArrayList<IslandNode>();
    Collection<IslandNode> possiblyCaptured = new ArrayList<IslandNode>();
    toJoin.add(moveNode);
    for(IslandNode i : splitNeighbors)
    {
      nextGame.remove(i);
      IslandNode replace = i.replace(thisIsland, splitIslands);
      nextGame.add(replace);
      if(replace.border.containsKey(moveNode.getId()))
      {
        if(replace.getPlayer() == currentPlayer)
        {
          toJoin.add(replace);
        }
        else
        {
          possiblyCaptured.add(replace);
        }
      }
    }
    if(1 < toJoin.size())
    {
      Set<IslandGeometry> joinNeighbors = new HashSet<IslandGeometry>();
      Set<Tile> joinedTiles = new HashSet<Tile>();
      for(IslandNode i : toJoin)
      {
        for(IslandNode n : i.neighbors(nextGame).keySet())
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
        IslandNode node = nextGame.islands.get(i.getId());
        nextGame.remove(node);
        IslandNode replace = node.replace(toJoin, joinedIsland);
        nextGame.add(replace);
      }
      for(IslandNode i : toJoin) nextGame.remove(i);
      nextGame.add(joinedIsland);
      possiblyCaptured.add(joinedIsland);
    }
    else
    {
      possiblyCaptured.add(moveNode);
    }
    // Capture surrounded islands
    for(IslandNode i : possiblyCaptured)
    {
      IslandNode captured = nextGame.islands.get(i.getId());
      if(captured.isDead(nextGame))
      {
        nextGame.remove(captured);
        nextGame.add(captured.capture());
      }
    }
    // Check if move violates Ko rule
    nextGame.hash = null;
    String stateHash = nextGame.getStateHash();
    if(previousStates.contains(stateHash)) {
      return null;
    }
    else
    {
      nextGame.previousStates.add(stateHash);
    }
    nextGame.nextPlayer();
    //nextGame.printIslands();
    return nextGame;
  }

  public void printIslands() {
    System.out.println("\nGame Islands:");
    for(IslandNode i : islands.values())
    {
      System.out.println(i.toString());
      if(0 != i.getPlayer())
      {
        IslandContext islandContext = new IslandContext(this, i);
        System.out.println("  Liberties: " + idList(islandContext.liberties));
        System.out.println("  Territory: " + idList(islandContext.territory));
        System.out.println("  Contested: " + idList(islandContext.contested));
        System.out.println("  Opponent: " + idList(islandContext.opponent));
      }
    }
  }

  private String idList(Set<IslandNode> liberties) {
    ArrayList<Integer> list = new ArrayList<Integer>();
    for(IslandNode i : liberties) list.add(i.getId());
    return list.toString();
  }

  private void remove(IslandNode thisIsland) {
    islands.remove(thisIsland.getId());
  }

  protected void nextPlayer() {
    //assert(isValid());
    if(++currentPlayer == 3) currentPlayer = 1;
  }

  private boolean isValid() {
    Set<Tile> allTiles = new HashSet<Tile>(getLayout().getTiles().values());
    for(IslandNode i : islands.values())
    {
      if(0 == i.neighbors(this).size())
      {
        if(islands.size() > 1)
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
        if(island.isTerritory(player, this))
        {
          territory += island.geometry.getSize();
        }
      }
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
    if (getLayout() == null) {
      if (other.getLayout() != null) return false;
    } else if (!getLayout().equals(other.getLayout())) return false;
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

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("GoGame [currentPlayer=");
    builder.append(currentPlayer);
    builder.append(", islands=");
    for(IslandNode i : islands.values())
    {
      builder.append("\n  ");
      builder.append(i);
    }
    builder.append("]");
    return builder.toString();
  }

  public GoGame setLayout(BoardLayout layout) {
    return new GoGame(layout);
  }

  public BoardLayout getLayout() {
    return layout;
  }
  
}
