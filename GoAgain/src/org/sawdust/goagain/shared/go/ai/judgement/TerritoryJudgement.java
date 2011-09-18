package org.sawdust.goagain.shared.go.ai.judgement;

import org.sawdust.goagain.shared.ai.FitnessValue;
import org.sawdust.goagain.shared.ai.GameFitness;
import org.sawdust.goagain.shared.ai.IterativeResult;
import org.sawdust.goagain.shared.go.GoGame;

@SuppressWarnings("serial")
public class TerritoryJudgement implements GameFitness<GoGame> {

  public IterativeResult<FitnessValue> gameFitness(GoGame g, int playerIdx) {
    GoGame game = (GoGame) g.unwrap();
    if (null == game) return wrap(FitnessValue.MIN_VALUE);
    int otherIdx = (playerIdx == 1) ? 2 : 1;

    if(game.winner != null)
    {
      return wrap(game.winner.equals(playerIdx) ? FitnessValue.MAX_VALUE : FitnessValue.MIN_VALUE);
    }
    
    // Score-level fitness
    int score1 = game.getScore(playerIdx);
    int score2 = game.getScore(otherIdx);
    int scoreDiff = score1 - score2;
    return wrap(scoreDiff);
  }

  public static IterativeResult<FitnessValue> wrap(double fitness) {
    return wrap(new FitnessValue(fitness, 0));
  }

  public static IterativeResult<FitnessValue> wrap(final FitnessValue f) {
    return new IterativeResult<FitnessValue>() {
      
      public double think() {
        return 1.;
      }
      
      public FitnessValue best() {
        return f;
      }

      public void hint(FitnessValue hint) {
        // TODO Auto-generated method stub
        
      }
    };
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("TerritoryJudgement");
    return builder.toString();
  }

}
