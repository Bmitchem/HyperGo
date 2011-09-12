package org.sawdust.goagain.shared.ai;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.sawdust.goagain.shared.Game;
import org.sawdust.goagain.shared.Move;

@SuppressWarnings("serial")
public class SimpleFitnessAi<T extends Game<T>> implements Ai<T> {

  private final class SimpleFitnessContemplation implements IterativeResult<Move<T>> {
    private final Game<T> game;
    private final Map<String,Move<T>> moves = new HashMap<String, Move<T>>();
    private final Map<String,Void> hints = new HashMap<String,Void>();
    private final int size;
    
    private Move<T> best = null;
    private FitnessValue bestFitness = null;
    private Iterator<String> iterator;
    private double progress = 0;

    private SimpleFitnessContemplation(Game<T> game) {
      this.game = game;
      for(Move<T> m : game.getMoves())
      {
        moves.put(m.getCommandText(), m);
      }
      iterator = moves.keySet().iterator();
      size = moves.size();
    }

    public double think() {
      if(!iterator.hasNext()) return 1.;
      String nextKey = iterator.next();
      if(!hints.containsKey(nextKey))
      {
        consider(moves.get(nextKey));
      }
      progress += 1. / size;
      return progress;
    }

    protected void consider(Move<T> move) {
      if(null == move) return;
      Game<T> newGame = move.move();
      if(null == newGame) return;
      IterativeResult<FitnessValue> result = fitness.gameFitness(newGame.unwrap(), game.player());
      while(1. > result.think()){}
      FitnessValue gameFitness = result.best();
      if(null == bestFitness || bestFitness.compareTo(gameFitness) < 0)
      {
        best = move;
        bestFitness = gameFitness;
      }
    }

    public Move<T> best() {
      return best;
    }

    public void hint(Move<T> hint) {
      String key = hint.getCommandText();
      consider(moves.get(key));
      hints.put(key, null);
    }
  }

  GameFitness<T> fitness;
  
  public SimpleFitnessAi(GameFitness<T> fitness) {
    super();
    this.fitness = fitness;
  }

  public IterativeResult<Move<T>> newContemplation(final Game<T> game) {
    return new SimpleFitnessContemplation(game);
  }
}
