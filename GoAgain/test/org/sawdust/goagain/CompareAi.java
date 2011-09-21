package org.sawdust.goagain;

import org.junit.Test;
import org.sawdust.goagain.shared.ai.FitnessAi;
import org.sawdust.goagain.shared.ai.GameFitness;
import org.sawdust.goagain.shared.ai.MemoryFitness;
import org.sawdust.goagain.shared.ai.PredictionAi;
import org.sawdust.goagain.shared.boards.RectangularLayout;
import org.sawdust.goagain.shared.go.GoGame;
import org.sawdust.goagain.shared.go.ai.intuition.GoMoveIntuition;
import org.sawdust.goagain.shared.go.ai.judgement.GoGameJudgement;
import org.sawdust.goagain.shared.go.ai.judgement.GoGameJudgement2;
import org.sawdust.goagain.shared.go.ai.judgement.TerritoryJudgement;

public class CompareAi {

  @Test
  public void testJudgement()
  {
    RectangularLayout layout = new RectangularLayout();
    @SuppressWarnings("unchecked") AiCompetition<GoGame> compete = new AiCompetition<GoGame>(
        new FitnessAi<GoGame>(new MemoryFitness<GoGame>(new TerritoryJudgement())),
        new FitnessAi<GoGame>(new MemoryFitness<GoGame>(new GoGameJudgement()))
        );
    compete.verbosity = 1;
    for(int i=0;i<10;i++) compete.compete(AiCompetition.randomMoves(new GoGame(layout), 6));
    compete.print();
  }

  @Test
  public void testJudgement2()
  {
    RectangularLayout layout = new RectangularLayout();
    @SuppressWarnings("unchecked") AiCompetition<GoGame> compete = new AiCompetition<GoGame>(
        new FitnessAi<GoGame>(new MemoryFitness<GoGame>(new GoGameJudgement())),
        new FitnessAi<GoGame>(new MemoryFitness<GoGame>(new GoGameJudgement2()))
        );
    compete.verbosity = 1;
    for(int i=0;i<10;i++) compete.compete(AiCompetition.randomMoves(new GoGame(layout), 6));
    compete.print();
  }

  @Test
  public void testDepth()
  {
    RectangularLayout layout = new RectangularLayout();
    @SuppressWarnings("unchecked") AiCompetition<GoGame> compete = new AiCompetition<GoGame>(
        new PredictionAi<GoGame>(new MemoryFitness<GoGame>(new GoGameJudgement()), 0, null),
        new PredictionAi<GoGame>(new MemoryFitness<GoGame>(new GoGameJudgement()), 1, null)
        );
    compete.verbosity = 3;
    for(int i=0;i<3;i++) compete.compete(AiCompetition.randomMoves(new GoGame(layout), 6));
    compete.print();
  }

  @Test
  public void testIntuition()
  {
    RectangularLayout layout = new RectangularLayout();
    GameFitness<GoGame> judgement = new MemoryFitness<GoGame>(new GoGameJudgement());
    int depth = 0;
    @SuppressWarnings("unchecked") AiCompetition<GoGame> compete = new AiCompetition<GoGame>(
        new PredictionAi<GoGame>(judgement, depth, null),
        new PredictionAi<GoGame>(judgement, depth, new GoMoveIntuition())
        );
    compete.verbosity = 2;
    for(int i=0;i<3;i++) compete.compete(AiCompetition.randomMoves(new GoGame(layout), 6));
    compete.print();
  }

}
