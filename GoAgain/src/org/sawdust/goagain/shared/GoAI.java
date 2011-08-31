package org.sawdust.goagain.shared;

import java.util.TreeMap;

@SuppressWarnings("serial")
public class GoAI implements Ai<GoGame> {

  public static boolean isServer = false;
  public boolean useServer = false;
  
  public int depth = 2;
  public MoveFitness<GoGame> intuition = new GoMoveIntuition();
  public GameFitness<GoGame> judgement = new GoGameJudgement();
  public int breadth = 1000;
  
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
    return new TreeSearchContemplation(game, depth, breadth, intuition, judgement);
    //return new MonteCarloContemplation(game, depth, breadth, intuition, judgement);
  }

}
