package org.sawdust.goagain.shared.ai;

import org.sawdust.goagain.shared.Game;

@SuppressWarnings("serial")
public class PredictionAi<T extends Game<T>> extends FitnessAi<T> {

  protected static final class PredictionContemplation<T extends Game<T>> extends FitnessContemplation<T> {
    protected GameProjection<T> bestOpposition = null;
    protected FitnessContemplation<T> opponentContemplation = null;

    private PredictionContemplation(PredictionAi<T> parent, Game<T> game) {
      super(parent, game);
    }

    public double think() {
      if(null == opponentContemplation)
      {
        String nextKey = nextKey();
        if(null == nextKey) return 1.;
        currentMove = moves.get(nextKey);
        currentGame = currentMove.move();
        if(null != currentGame)
        {
          opponentContemplation = (FitnessContemplation<T>) ((PredictionAi<T>)parent).opponent.newContemplation(currentGame);
          if(null != bestOpposition) opponentContemplation.hint(bestOpposition);
          for(GameProjection<T> hint : allHints)
          {
            opponentContemplation.hint(hint.pop());
          }
        }
        else
        {
          progress += 1. / size;
        }
      }
      else
      {
        double oppositionProgress = opponentContemplation.think();
        if(1. <= oppositionProgress)
        {
          org.sawdust.goagain.shared.ai.Ai.GameProjection<T> currentOppositionBest = opponentContemplation.best();
          GameProjection<T> oppositionProjection = currentOppositionBest;
          IterativeResult<FitnessValue> result = parent.fitness.gameFitness(oppositionProjection.finalGame().unwrap(), player);
          while(1. > result.think()){}
          FitnessValue gameFitness = result.best();
          if(null == bestFitness || bestFitness.compareTo(gameFitness) < 0)
          {
            best = currentMove;
            bestOpposition = oppositionProjection;
            bestFitness = gameFitness;
          }
          opponentContemplation = null;
          progress += 1. / size;
        }
        else 
        {
          if(opponentContemplation.canPrune())
          {
            org.sawdust.goagain.shared.ai.Ai.GameProjection<T> currentOppositionBest = opponentContemplation.best();
            if(null != currentOppositionBest)
            {
              Game<T> testGame = currentOppositionBest.finalGame();
              IterativeResult<FitnessValue> result = parent.fitness.gameFitness(testGame.unwrap(), game.player());
              while(1. > result.think()){}
              FitnessValue gameFitness = result.best();
              if(null != bestFitness && bestFitness.compareTo(gameFitness) > 0)
              {
                progress += 1. / size;
                opponentContemplation = null;
                return progress;
              }
            }
          }
          return progress + (oppositionProgress / size);
        }
      }
      return progress;
    }

    public boolean canPrune() {
      return (null == opponentContemplation);
    }

    public GameProjection<T> best() {
      if(null == best) return null;
      return new GameProjection<T>(best, bestOpposition);
    }
  }

  FitnessAi<T> opponent;

  public PredictionAi(GameFitness<T> fitness, FitnessAi<T> opponent, MoveFitness<T> intuition) {
    super(fitness, intuition);
    this.fitness = fitness;
    this.opponent = opponent;
  }

  public PredictionAi(GameFitness<T> judgement, int depth, MoveFitness<T> intuition) {
    this(judgement, nextLayer(judgement, depth, intuition), intuition);
  }

  private static <T extends Game<T>> FitnessAi<T> nextLayer(GameFitness<T> judgement, int depth, MoveFitness<T> intuition) {
    if(0 == depth) return new FitnessAi<T>(judgement, intuition);
    assert(0 < depth);
    return new PredictionAi<T>(judgement, depth-1, intuition);
  }

  public PredictionContemplation<T> newContemplation(final Game<T> game) {
    return new PredictionContemplation<T>(this, game);
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("PredictionAi [opponent=");
    builder.append(opponent);
    builder.append(", fitness=");
    builder.append(fitness);
    builder.append(", intuition=");
    builder.append(intuition);
    builder.append("]");
    return builder.toString();
  }
  
}
