package org.sawdust.goagain.shared.ai;

import java.util.Collection;
import java.util.Iterator;

import org.sawdust.goagain.shared.GameCommand;
import org.sawdust.goagain.shared.go.Game;

@SuppressWarnings("serial")
public class SimplePredictionAi<T extends Game<T>> implements Ai<T> {

  GameFitness<T> fitness;
  Ai<T> opponent;

  public SimplePredictionAi(GameFitness<T> fitness, Ai<T> opponent) {
    super();
    this.fitness = fitness;
    this.opponent = opponent;
  }

  public IterativeResult<GameCommand<T>> newContemplation(final Game<T> game) {
    return new IterativeResult<GameCommand<T>>() {
      
      GameCommand<T> best = null;
      FitnessValue bestFitness = null;
      Collection<GameCommand<T>> moves = game.getMoves();
      int size = moves.size();
      Iterator<GameCommand<T>> iterator = moves.iterator();
      double progress = 0;
      
      public double think() {
        if(!iterator.hasNext()) return 1.;
        GameCommand<T> move = iterator.next();
        Game<T> newGame = move.move(game);
        IterativeResult<GameCommand<T>> opponentContemplation = opponent.newContemplation(newGame);
        while(1. > opponentContemplation.think());
        newGame = opponentContemplation.best().move(newGame);
        IterativeResult<FitnessValue> result = fitness.gameFitness(newGame, game.player());
        while(1. > result.think()){}
        FitnessValue gameFitness = result.best();
        if(null == bestFitness || bestFitness.compareTo(gameFitness) < 0)
        {
          best = move;
          bestFitness = gameFitness;
        }
        progress += 1. / size;
        return progress;
      }
      
      public GameCommand<T> best() {
        return best;
      }
    };
  }
}
