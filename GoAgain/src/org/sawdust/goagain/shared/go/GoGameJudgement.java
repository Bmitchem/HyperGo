package org.sawdust.goagain.shared.go;

import org.sawdust.goagain.shared.ai.GameFitness;

@SuppressWarnings("serial")
public class GoGameJudgement implements GameFitness<GoGame> {

  public double gameFitness(GoGame game, int playerIdx) {
    if (null == game) return Integer.MIN_VALUE;
    int otherIdx = (playerIdx == 1) ? 2 : 1;

    // Score-level fitness
    int score1 = game.getScore(playerIdx);
    int score2 = game.getScore(otherIdx);
    int scoreDiff = score1 - score2;
    double fitness = scoreDiff * 100;

    for (Island island : game.islands) {
      if(island.getPlayer() == 0) continue;
      double bias = (playerIdx == island.getPlayer()) ? 1.0 : -1.0;

      // Liberty fitness
      int libertyF = island.getLiberties(game).size();
      if(libertyF > 2) libertyF = 2;
      libertyF *= 100;
      fitness += bias * libertyF * island.getPositions().size() * 10;
      
      // Freedom-level fitness
      double freedom = 0;
      for (Tile t : island.getPerimiter()) {
        if (game.getState(t) == 0) {
          freedom += 1;
        }
      }
      if(freedom == 1)
      {
        freedom = -10;
      }
      else if(freedom > 4)
      {
        freedom = 4;
      }
      fitness += bias * freedom * island.getPositions().size();
    }

    return fitness;
  }

}
