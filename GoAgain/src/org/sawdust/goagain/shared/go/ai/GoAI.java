package org.sawdust.goagain.shared.go.ai;

import java.util.TreeMap;

import org.sawdust.goagain.shared.Game;
import org.sawdust.goagain.shared.Move;
import org.sawdust.goagain.shared.ai.Ai;
import org.sawdust.goagain.shared.ai.GameFitness;
import org.sawdust.goagain.shared.ai.GameMemoryTree;
import org.sawdust.goagain.shared.ai.IterativeResult;
import org.sawdust.goagain.shared.ai.MemoryFitness;
import org.sawdust.goagain.shared.ai.MoveFitness;
import org.sawdust.goagain.shared.ai.SimpleFitnessAi;
import org.sawdust.goagain.shared.ai.SimplePredictionAi;
import org.sawdust.goagain.shared.go.GoGame;

@SuppressWarnings("serial")
public class GoAI implements Ai<GoGame> {

  public static boolean isServer = false;
  public boolean useServer = false;
  public String depth = "50 50";
  public boolean useMCTS = false;
  public boolean useExperiment = true;
  public MoveFitness<GoGame> intuition = new GoMoveIntuition();
  public GameFitness<GoGame> judgement = new GoGameJudgement();
  
  public static <T extends Comparable<T>> T floorKey(TreeMap<T, ?> commandSpace, T d) {
    T last = null;
    for(T item : commandSpace.keySet())
    {
      if(item.compareTo(d) > 0) return last;
      last = item;
    }
    return last;
  }

  
  public IterativeResult<Move<GoGame>> newContemplation(Game<GoGame> game) {
    if (useMCTS)
    {
      return new MctsAi((GoGame) game);
    }
    else if(useExperiment)
    {
      GameFitness<GoGame> judgement2 = false?new MemoryFitness<GoGame>(judgement):judgement;
      Game<GoGame> game2 = false?new GameMemoryTree<GoGame>((GoGame) game):game;
      Ai<GoGame> ai = new SimplePredictionAi<GoGame>(judgement2, new SimpleFitnessAi<GoGame>(judgement2));
      return ai.newContemplation(game2);
    }
    else
    {
      String[] split = depth.split(" |,");
      int[] treeWidth = new int[split.length];
      for (int i = 0; i < split.length; i++)
        treeWidth[i] = Integer.parseInt(split[i]);
      return new MinMaxAi((GoGame) game, intuition, judgement, treeWidth);
    }
  }

}
