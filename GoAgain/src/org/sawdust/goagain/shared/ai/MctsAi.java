package org.sawdust.goagain.shared.ai;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.sawdust.goagain.shared.GameCommand;
import org.sawdust.goagain.shared.go.Game;
import org.sawdust.goagain.shared.go.GoGame;

/**
 * See Also: 
 * * http://www.mcts.ai/?q=mcts
 * * http://senseis.xmp.net/?MonteCarlo
 * * http://senseis.xmp.net/?UCT
 * 
 * @author acharneski
 *
 */
public class MctsAi implements IterativeResult<GameCommand<GoGame>> {

  public class Node {
    final Map<GameCommand<GoGame>, Node> moves = new HashMap<GameCommand<GoGame>, MctsAi.Node>();
    final Node parent;
    final Game<GoGame> game;
    final int generation;

    IterativeResult<FitnessValue> fitnessEval = null;

    private FitnessValue currentFitness;

    public Node(GoGame game)
    {
      this.game = game;
      this.parent = null;
      this.generation = 0;

      for(GameCommand<GoGame> m : getMoves(game))
      {
        moves.put(m, null);
      }
    }
    public Node(Node parent, GameCommand<GoGame> move, int generation) {
      this.parent = parent;
      this.generation = generation;
      this.game = move.move(parent.game);
      if(null != game)
      {
        if(maxGeneration > generation)
        {
          for(GameCommand<GoGame> m : getMoves(game))
          {
            moves.put(m, null);
          }
        }
      }
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

    private FitnessValue play()
    {
      if(null == fitnessEval)
      {
        fitnessEval = fitness.gameFitness(game, player());
      }
      fitnessEval.think();
      return fitnessEval.best();
    }

    protected int player() {
      if(null == parent) return -1;
      return parent.game.player();
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
          return e.getValue().select();
        }
        else
        {
          Node expand = expand(move);
          if(null != expand) return expand;
        }
      }
      return null;
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
          winRatio = node.currentFitness.fitness + node.currentFitness.uncertianty;
        }
        winRatio += Math.random() * 0.1;
        sortedMoves.put(-winRatio, e);
      }
      return sortedMoves;
    }

    @Override
    public String toString()
    {
      StringBuilder builder = new StringBuilder();
      builder.append("Node [generation=");
      builder.append(generation);
      builder.append("]");
      return builder.toString();
    }

    public void updateFitness(FitnessValue value) {
      if(null != value)
      {
        this.currentFitness = value;
      }
      else
      {
        ArrayList<FitnessValue> list = new ArrayList<FitnessValue>();
        for(Node node : moves.values())
        {
          if(null != node && null != node.currentFitness)
          {
            list.add(node.currentFitness);
          }
        }
        this.currentFitness = FitnessValue.avg(list.toArray(new FitnessValue[]{}));
      }
      if(null != parent)
      {
        parent.updateFitness(null);
      }
    }
    
  }

  private final Node root;
  private final int maxGeneration = 2;
  private final double scenarios = 5000;
  private final GameFitness<GoGame> fitness = new MonteCarloFitness<GoGame>();
  private int totalEvals = 0;

  public MctsAi(GoGame game) {
    this.root = new Node(game);
  }

  public GameCommand<GoGame> best() {
    TreeMap<Double, Entry<GameCommand<GoGame>, Node>> sortedChildren = root.sortedChildren();
    for(Entry<Double, Entry<GameCommand<GoGame>, Node>> e : sortedChildren.entrySet())
    {
      System.out.println(e.getKey() + " - " + e.getValue().getKey().toString() + " - " + e.getValue().getValue().toString());
    }
    return sortedChildren.entrySet().iterator().next().getValue().getKey();
  }

  protected Collection<GameCommand<GoGame>> getMoves(Game<GoGame> game)
  {
    return game.getMoves();
  }

  public double think() {
    Node node = root.select();
    node.updateFitness(node.play());
    return ((double)totalEvals++) / (scenarios);
  }

}
