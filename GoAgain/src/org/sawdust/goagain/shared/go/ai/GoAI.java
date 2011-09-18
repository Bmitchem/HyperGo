package org.sawdust.goagain.shared.go.ai;

import org.sawdust.goagain.shared.Game;
import org.sawdust.goagain.shared.ai.Ai;
import org.sawdust.goagain.shared.ai.GameFitness;
import org.sawdust.goagain.shared.ai.IterativeResult;
import org.sawdust.goagain.shared.ai.MemoryFitness;
import org.sawdust.goagain.shared.ai.MoveFitness;
import org.sawdust.goagain.shared.ai.PredictionAi;
import org.sawdust.goagain.shared.go.GoGame;

@SuppressWarnings("serial")
public class GoAI implements Ai<GoGame> {

  public static boolean isServer = false;
  public boolean useServer = false;
  public boolean useExperiment = false;
  public MoveFitness<GoGame> intuition = new GoMoveIntuition();
  public GameFitness<GoGame> judgement = new GoGameJudgement();
  public int depth = 1;
  
  
  public IterativeResult<GameProjection<GoGame>> newContemplation(Game<GoGame> game) {
    if(useExperiment)
    {
      judgement = new MemoryFitness<GoGame>(judgement);
      Ai<GoGame> ai3 = new PredictionAi<GoGame>(judgement, depth, intuition);
      return ai3.newContemplation(game);
    }
    else
    {
      judgement = new MemoryFitness<GoGame>(judgement);
      Ai<GoGame> ai3 = new PredictionAi<GoGame>(judgement, depth, intuition);
      return ai3.newContemplation(game);
    }
  }

}
