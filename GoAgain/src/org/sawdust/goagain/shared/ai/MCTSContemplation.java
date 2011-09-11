package org.sawdust.goagain.shared.ai;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.sawdust.goagain.shared.GameCommand;
import org.sawdust.goagain.shared.go.GoGame;
import org.sawdust.goagain.shared.go.IslandNode;
import org.sawdust.goagain.shared.go.Util;

/**
 * See Also: 
 * * http://www.mcts.ai/?q=mcts
 * * http://senseis.xmp.net/?MonteCarlo
 * * http://senseis.xmp.net/?UCT
 * 
 * @author acharneski
 *
 */
public class MCTSContemplation implements IterativeResult<GoGame> {

  final int maxGeneration = 4;
  final boolean firstCaptureWins = true;

  public class Node {
    final Map<GameCommand<GoGame>, Node> moves = new HashMap<GameCommand<GoGame>, MCTSContemplation.Node>();
    final Node parent;
    final GoGame game;
    final int generation;
    final Integer winner;
    int totalEvals = 0;
    int totalWins = 0;

    public Node(Node parent, GameCommand<GoGame> move, int generation) {
      this.parent = parent;
      this.generation = generation;
      this.game = move.move(parent.game);
      if(null != game)
      {
        this.winner = getWinner(this.parent.game, game, move);
        if(null != this.winner)
        {
          this.totalWins = this.winner.equals(parent.game.currentPlayer)?1:0;
          this.totalEvals = 1;
        }
        
        if(maxGeneration > generation)
        {
          for(GameCommand<GoGame> m : getMoves(game))
          {
            moves.put(m, null);
          }
        }
      }
      else
      {
        this.winner = null;
      }
    }

    public Node(GoGame game)
    {
      this.game = game;
      this.parent = null;
      this.generation = 0;
      this.winner = null;

      for(GameCommand<GoGame> m : getMoves(game))
      {
        moves.put(m, null);
      }
    }

    private Integer play()
    {
      if(this.winner != null)
      {
        System.out.println("Static winner " + this.winner + " in generation " + this.generation);
        return this.winner;
      }
      else
      {
        return playRandomGame();
      }
    }

    private Integer playRandomGame()
    {
      GoGame prevGame = null;
      GoGame newGame = game;
      GameCommand<GoGame> move = null;
      Integer w = null;
      int moveCount = 0;
      do
      {
        prevGame = newGame;
        newGame = null;
        while(null == newGame)
        {
          move = Util.randomValue(getMoves(prevGame));
          newGame = move.move(prevGame);
        }
        moveCount++;
        w = getWinner(prevGame, newGame, move);
      } while(null == w);
      System.out.println("Random winner " + w + " in generation " + (this.generation + moveCount));
      return w;
    }

    private Node select()
    {
      if(generation >= maxGeneration) return this;
      TreeMap<Double, Entry<GameCommand<GoGame>, Node>> sorted = sortedChildren();
      for(Entry<GameCommand<GoGame>, Node> e : sorted.values())
      {
        GameCommand<GoGame> move = e.getKey();
        if(null != e.getValue())
        {
          Node node = e.getValue();
          if(null == node.winner)
          {
            return node.select();
          }
        }
        else
        {
          Node expand = expand(move);
          if(null != expand) return expand;
        }
      }
      return null;
    }

    private Node expand(GameCommand<GoGame> move)
    {
      Node node = new Node(this, move, generation+1);
      if(null != node.game)
      {
        moves.put(move, node);
        return node;
      }
      else
      {
        moves.remove(move);
        return null;
      }
    }

    private void reportWinner(int winner)
    {
      totalEvals++;
      if(null != parent) 
      {
        if(winner == player())
        {
          totalWins++;
        }
        parent.reportWinner(winner);
      }
    }

    private TreeMap<Double, Entry<GameCommand<GoGame>, Node>> sortedChildren()
    {
      TreeMap<Double,Entry<GameCommand<GoGame>, Node>> sortedMoves = new TreeMap<Double,Map.Entry<GameCommand<GoGame>,Node>>();
      for(Entry<GameCommand<GoGame>, Node> e : moves.entrySet())
      {
        Node node = e.getValue();
        double winRatio;
        if (null == node) {
          winRatio = 2;
        }
        else
        {
          winRatio = node.winRatio();
        }
        winRatio += Math.random() * 0.1;
        sortedMoves.put(-winRatio, e);
      }
      return sortedMoves;
    }

    private double winRatio()
    {
      if (null != winner) {
        return (winner.equals(player()))?1:0;
      }
      double upperConfidence = (double)totalWins;
      upperConfidence += 5 * Math.pow(Math.log(parent.totalEvals) / totalEvals, 0.5);
      double value = upperConfidence / ((double)totalEvals);
      return value;
    }

    protected int player() {
      if(null == parent) return -1;
      return parent.game.currentPlayer;
    }

    @Override
    public String toString()
    {
      StringBuilder builder = new StringBuilder();
      builder.append("Node [totalEvals=");
      builder.append(totalEvals);
      builder.append(", totalWins=");
      builder.append(totalWins);
      builder.append(", generation=");
      builder.append(generation);
      builder.append(", winner=");
      builder.append(winner);
      builder.append("]");
      return builder.toString();
    }
    
  }

  final Node root;
  private double scenarios = 10000;

  public MCTSContemplation(GoGame game) {
    this.root = new Node(game);
  }

  public double think() {
    Node node = root.select();
    Integer winner = node.play();
    if(null != winner)
    {
      node.reportWinner(winner);
    }
    return ((double)root.totalEvals) / (scenarios);
  }

  public GameCommand<GoGame> best() {
    TreeMap<Double, Entry<GameCommand<GoGame>, Node>> sortedChildren = root.sortedChildren();
    for(Entry<Double, Entry<GameCommand<GoGame>, Node>> e : sortedChildren.entrySet())
    {
      System.out.println(e.getKey() + " - " + e.getValue().getKey().toString() + " - " + e.getValue().getValue().toString());
    }
    return sortedChildren.entrySet().iterator().next().getValue().getKey();
  }

  protected Collection<GameCommand<GoGame>> getMoves(GoGame game)
  {
    return game.getMoves();
  }


  protected Integer getWinner(GoGame prevGame, GoGame game, GameCommand<GoGame> move)
  {
    Integer w;
    if(null != game.winner)
    {
      w = game.winner;
    }
    else
    {
      if(null != prevGame && firstCaptureWins)
      {
        if(count(game, prevGame.currentPlayer) < 1 + count(prevGame, prevGame.currentPlayer))
        {
          w = game.currentPlayer;
        }
        else if(count(game, game.currentPlayer) < count(prevGame, game.currentPlayer))
        {
          w = prevGame.currentPlayer;
        }
        else
        {
          w = null;
        }
      }
      else
      {
        w = null;
      }
    }
    return w;
  }

  protected static int count(GoGame prevGame, int player)
  {
    int stoneCount = 0;
    for(IslandNode i : prevGame.islands.values())
    {
      if(i.getPlayer() == player)
      {
        stoneCount += i.geometry.getSize();
      }
    }
    return stoneCount;
  }
}
