package org.sawdust.goagain.shared.ai;

import java.util.Collection;
import java.util.Iterator;

import org.sawdust.goagain.shared.Game;
import org.sawdust.goagain.shared.Move;

@SuppressWarnings("serial")
public class SimplePredictionAi<T extends Game<T>> implements Ai<T> {

  GameFitness<T> fitness;
  Ai<T> opponent;

  public SimplePredictionAi(GameFitness<T> fitness, Ai<T> opponent) {
    super();
    this.fitness = fitness;
    this.opponent = opponent;
  }

  public IterativeResult<Move<T>> newContemplation(final Game<T> game) {
    return new IterativeResult<Move<T>>() {
      
      Move<T> best = null;
      Move<T> bestOpposition = null;
      FitnessValue bestFitness = null;
      @SuppressWarnings("unchecked") Collection<Move<T>> moves = (Collection<Move<T>>) game.getMoves();
      int size = moves.size();
      Iterator<Move<T>> iterator = moves.iterator();
      double progress = 0;
      
      public double think() {
        if(!iterator.hasNext()) return 1.;
        Move<T> move = iterator.next();
        Move<T> oppositionMove;
        Game<T> newGame = move.move();
        if(null != newGame)
        {
          IterativeResult<Move<T>> opponentContemplation = opponent.newContemplation(newGame);
          if(null != bestOpposition) opponentContemplation.hint(bestOpposition);
          boolean skipBranch = false;
          while(1. > opponentContemplation.think())
          {
            Game<T> testGame = opponentContemplation.best().move();
            IterativeResult<FitnessValue> result = fitness.gameFitness(testGame.unwrap(), game.player());
            while(1. > result.think()){}
            FitnessValue gameFitness = result.best();
            if(null != bestFitness && bestFitness.compareTo(gameFitness) > 0)
            {
              skipBranch = true;
              break;
            }
          }
          if(!skipBranch)
          {
            oppositionMove = opponentContemplation.best();
            newGame = oppositionMove.move();
            IterativeResult<FitnessValue> result = fitness.gameFitness(newGame.unwrap(), game.player());
            while(1. > result.think()){}
            FitnessValue gameFitness = result.best();
            if(null == bestFitness || bestFitness.compareTo(gameFitness) < 0)
            {
              best = move;
              bestOpposition = oppositionMove;
              bestFitness = gameFitness;
            }
          }
          progress += 1. / size;
        }
        return progress;
      }
      
      public Move<T> best() {
        return best;
      }

      public void hint(Move<T> hint) {
        // TODO Auto-generated method stub
        
      }
    };
  }
}
