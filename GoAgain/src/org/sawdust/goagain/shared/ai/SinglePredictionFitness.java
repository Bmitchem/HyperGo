package org.sawdust.goagain.shared.ai;

import org.sawdust.goagain.shared.Game;
import org.sawdust.goagain.shared.ai.Ai.GameProjection;

@SuppressWarnings("serial")
public class SinglePredictionFitness<T extends Game<T>> implements GameFitness<T> {

  private final class Contemplation implements IterativeResult<FitnessValue> {
    private IterativeResult<GameProjection<T>> opponentContemplation;
    private int playerIdx;
    IterativeResult<FitnessValue> gameFitness;
    
    public Contemplation(T game, int playerIdx) {
      this.opponentContemplation = opponent.newContemplation(game);
      this.playerIdx = playerIdx;
    }

    public double think() {
      double progress = opponentContemplation.think();
      if(progress < 1.) 
      {
        return progress / 2.;
      }
      else if(null == gameFitness)
      {
        GameProjection<T> best = opponentContemplation.best();
        @SuppressWarnings("unchecked") T newGame = (T) best.finalGame();
        gameFitness = fitness.gameFitness(newGame, playerIdx);
      }
      progress = gameFitness.think();
      return .5 + (progress/2.);
    }

    public void hint(FitnessValue hint) {
    }

    public FitnessValue best() {
      if(null == gameFitness) return null;
      return gameFitness.best();
    }
  }

  private GameFitness<T> fitness;
  private Ai<T> opponent;

  public SinglePredictionFitness(GameFitness<T> judgement, Ai<T> inner) {
    this.fitness = judgement;
    inner = new MemoryAi<T>(inner);
    this.opponent = inner;
  }

  public IterativeResult<FitnessValue> gameFitness(T game, int playerIdx) {
    return new Contemplation(game, playerIdx);
  }

}
