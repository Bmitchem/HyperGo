package org.sawdust.goagain.shared.ai;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.sawdust.goagain.shared.Game;
import org.sawdust.goagain.shared.Move;

@SuppressWarnings("serial")
public class FitnessAi<T extends Game<T>> implements Ai<T> {

  protected static class FitnessContemplation<T extends Game<T>> implements IterativeResult<GameProjection<T>> {
    protected final Game<T> game;
    protected final Map<String,Move<T>> moves = new HashMap<String, Move<T>>();
    protected final Set<String> processedHints = new HashSet<String>();
    protected final List<String> pendingHints = new ArrayList<String>();
    protected final List<GameProjection<T>> allHints = new ArrayList<GameProjection<T>>();
    protected final int player;
    protected final int size;
    protected final FitnessAi<T> parent;
    
    protected Move<T> best = null;
    protected FitnessValue bestFitness = null;
    protected Iterator<String> iterator;
    protected double progress = 0;
    protected Move<T> currentMove;
    protected Game<T> currentGame;
    protected IterativeResult<FitnessValue> newGameFitness;

    protected FitnessContemplation(FitnessAi<T> parent, Game<T> game) {
      this.game = game;
      this.parent = parent;
      this.player = game.player();
      List<String> order = new ArrayList<String>();
      for(Move<T> m : parent.getMoves(game))
      {
        String commandText = m.getCommandText();
        moves.put(commandText, m);
        order.add(commandText);
      }
      iterator = order.iterator();
      size = moves.size();
    }

    public double think() {
      if(null == this.newGameFitness)
      {
        String nextKey = nextKey();
        if(null == nextKey) return 1.;
        currentMove = moves.get(nextKey);
        currentGame = currentMove.move();
        if(null == currentGame) 
        {
          currentMove = null;
          currentGame = null;
          progress += 1. / size;
          return progress;
        }
        else
        {
          newGameFitness = parent.fitness.gameFitness(currentGame.unwrap(), player);
        }
      }
      double innerProgress = this.newGameFitness.think();
      if(1. <= innerProgress)
      {
        FitnessValue gameFitness = newGameFitness.best();
        if(null == bestFitness || bestFitness.compareTo(gameFitness) < 0)
        {
          best = currentMove;
          bestFitness = gameFitness;
        }
        currentMove = null;
        currentGame = null;
        newGameFitness = null;
        progress += 1. / size;
        return progress;
      }
      else
      {
        return progress + (innerProgress / size);
      }
    }

    protected String nextKey() {
      String nextKey;
      do
      {
        if(0 < pendingHints.size())
        {
          nextKey = pendingHints.get(0);
          pendingHints.remove(0);
        }
        else
        {
          if(!iterator.hasNext()) 
          {
            nextKey = null;
          }
          else
          {
            nextKey = iterator.next();
          }
        }
      } while(null != nextKey && (processedHints.contains(nextKey) || !moves.containsKey(nextKey)));
      return nextKey;
    }

    public GameProjection<T> best() {
      if(null == best) return null;
      return new GameProjection<T>(best);
    }

    public void hint(GameProjection<T> hint) {
      allHints.add(hint);
      pendingHints.add(hint.firstMove().getCommandText());
    }

    public boolean canPrune() {
      return true;
    }
  }

  protected GameFitness<T> fitness;
  protected MoveFitness<T> intuition;
  
  protected FitnessAi(GameFitness<T> fitness, MoveFitness<T> intuition) {
    super();
    this.fitness = fitness;
    this.intuition = intuition;
  }

  public FitnessAi(GameFitness<T> judgement) {
    this(judgement, null);
  }

  public FitnessContemplation<T> newContemplation(final Game<T> game) {
    return new FitnessContemplation<T>(this, game);
  }
  
  public static class MoveSort implements Comparable<MoveSort>
  {
    final double majorSort;
    final double minorSort = Math.random();
    
    public MoveSort(double majorSort) {
      this.majorSort = majorSort;
    }

    public int compareTo(MoveSort o) {
      int result = Double.compare(majorSort, o.majorSort);
      if(0 == result) result = Double.compare(minorSort, o.minorSort);
      return result;
    }
  }
  
  protected Collection<? extends Move<T>> getMoves(Game<T> game) {
    Collection<? extends Move<T>> moves = game.getMoves();
    if(null == intuition) return moves;
    TreeMap<MoveSort,Move<T>> map = new TreeMap<MoveSort, Move<T>>();
    for(Move<T> move : moves)
    {
      T unwrap = game.unwrap();
      double fitness = intuition.moveFitness(move, unwrap);
      map.put(new MoveSort(-fitness), move);
    }
    return map.values();
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("FitnessAi [fitness=");
    builder.append(fitness);
    builder.append(", intuition=");
    builder.append(intuition);
    builder.append("]");
    return builder.toString();
  }
  
}
