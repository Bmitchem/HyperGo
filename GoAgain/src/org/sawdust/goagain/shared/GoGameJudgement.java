package org.sawdust.goagain.shared;

@SuppressWarnings("serial")
public class GoGameJudgement implements GameFitness<GoGame> {

  public double gameFitness(GoGame game, int playerIdx) {
    if (null == game) return Integer.MIN_VALUE;
    int otherIdx = (playerIdx == 1) ? 2 : 1;

    // Score-level fitness
    int score1 = game.getScore(playerIdx);
    int score2 = game.getScore(otherIdx);
    int scoreDiff = score1 - score2;
    double fitness = scoreDiff * 1000;

    // Freedom-level fitness
    for (Island island : game.islands) {
      double freedom = 0;
      for (Tile t : island.getPerimiter()) {
        if (game.getState(t) == 0) {
          freedom += 1;
        }
      }
      double bias = (playerIdx == island.getPlayer()) ? 1.0 : -1.0;
      fitness += bias * freedom * island.getPositions().size();
    }

    return fitness;
  }

}
