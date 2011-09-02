package org.sawdust.goagain.shared.go;

import java.util.TreeMap;

import org.sawdust.goagain.shared.ai.Ai;
import org.sawdust.goagain.shared.ai.GameFitness;
import org.sawdust.goagain.shared.ai.IterativeResult;
import org.sawdust.goagain.shared.ai.MoveFitness;
import org.sawdust.goagain.shared.ai.TreeSearchContemplation;

@SuppressWarnings("serial")
public class GoAI implements Ai<GoGame> {

  public static boolean isServer = false;
  public boolean useServer = false;
  
  public String depth = "50 50";
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

  public IterativeResult<GoGame> newContemplation(GoGame game) {
    String[] split = depth.split(" |,");
    int[] treeWidth = new int[split.length];
    for(int i=0;i<split.length;i++) treeWidth[i] = Integer.parseInt(split[i]);
    return new TreeSearchContemplation(game, intuition, judgement, treeWidth);
  }

}
