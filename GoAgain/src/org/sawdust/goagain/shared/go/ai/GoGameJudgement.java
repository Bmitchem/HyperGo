package org.sawdust.goagain.shared.go.ai;

import org.sawdust.goagain.shared.ai.FitnessValue;
import org.sawdust.goagain.shared.ai.GameFitness;
import org.sawdust.goagain.shared.ai.IterativeResult;
import org.sawdust.goagain.shared.go.GoGame;
import org.sawdust.goagain.shared.go.IslandNode;

@SuppressWarnings("serial")
public class GoGameJudgement implements GameFitness<GoGame> {

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
    double fitness = scoreDiff * 100;

    for (IslandNode island : game.islands.values()) {
      if(island.getPlayer() == 0) continue;
      double bias = (playerIdx == island.getPlayer()) ? 1.0 : -1.0;

      // Liberty fitness
      IslandContext surround = new IslandContext(game, island);
      int libertyCount = surround.liberties.size();
      if(libertyCount >= 2) libertyCount = 2;
      int size = island.geometry.getPositions().size();
      for(IslandNode i : surround.liberties)
      {
        size += i.geometry.getSize();
      }
      fitness += 100 * bias * libertyCount * size;
      
//      if(libertyCount < 2)
//      {
//        int potentialLiberties = libertyCount;
//        int whitesapce = 0;
//        for(Island i : surround.contested)
//        {
//          potentialLiberties += i.thin().getSize();
//          whitesapce += i.getSize();
//        }
//        for(Island i : surround.territory)
//        {
//          potentialLiberties += i.thin().getSize();
//          whitesapce += i.getSize();
//        }
//        if(potentialLiberties < 2)
//        {
//          fitness -= 100 * bias * size;
//          fitness -= 100 * bias * whitesapce;
//        }
//      }
      
      // Freedom-level fitness
      double freedom = island.getFreedom(game);
      if(freedom == 1)
      {
        freedom = -10;
      }
      else if(freedom > 4)
      {
        freedom = 4;
      }
      fitness += bias * freedom * size;
    }

    return wrap(fitness);
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
    };
  }

}
