package org.sawdust.goagain.shared.ai;

import java.util.Collection;
import java.util.Iterator;

import org.sawdust.goagain.shared.Game;
import org.sawdust.goagain.shared.Move;

@SuppressWarnings("serial")
public class SimpleFitnessAi<T extends Game<T>> implements Ai<T> {

  GameFitness<T> fitness;
  
  public SimpleFitnessAi(GameFitness<T> fitness) {
    super();
    this.fitness = fitness;
  }

  public IterativeResult<Move<T>> newContemplation(final Game<T> game) {
    return new IterativeResult<Move<T>>() {
      
      Move<T> best = null;
      FitnessValue bestFitness = null;
      @SuppressWarnings("unchecked") Collection<Move<T>> moves = (Collection<Move<T>>) game.getMoves();
      int size = moves.size();
      Iterator<Move<T>> iterator = moves.iterator();
      double progress = 0;
      
      public double think() {
        if(!iterator.hasNext()) return 1.;
        Move<T> move = iterator.next();
        Game<T> newGame = move.move();
        if(null != newGame)
        {
          IterativeResult<FitnessValue> result = fitness.gameFitness(newGame.unwrap(), game.player());
          while(1. > result.think()){}
          FitnessValue gameFitness = result.best();
          if(null == bestFitness || bestFitness.compareTo(gameFitness) < 0)
          {
            best = move;
            bestFitness = gameFitness;
          }
        }
        progress += 1. / size;
        return progress;
      }
      
      public Move<T> best() {
        return best;
      }
    };
  }
}
