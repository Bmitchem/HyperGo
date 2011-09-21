package org.sawdust.goagain.shared.go.ai.judgement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.sawdust.goagain.shared.ai.FitnessValue;
import org.sawdust.goagain.shared.ai.GameFitness;
import org.sawdust.goagain.shared.ai.IterativeResult;
import org.sawdust.goagain.shared.go.GoGame;
import org.sawdust.goagain.shared.go.IslandGeometry;
import org.sawdust.goagain.shared.go.IslandNode;

@SuppressWarnings("serial")
public class GoGameJudgement2 implements GameFitness<GoGame> {

  public IterativeResult<FitnessValue> gameFitness(GoGame game, final int playerIdx) {
    if (null == game) return wrap(FitnessValue.MIN_VALUE);
    //int otherIdx = (playerIdx == 1) ? 2 : 1;

    if(game.winner != null)
    {
      return wrap(game.winner.equals(playerIdx) ? FitnessValue.MAX_VALUE : FitnessValue.MIN_VALUE);
    }
    
    double fitness = 0;

    Map<Integer, IslandNode<Integer>> nodes = new GoAggregator<Integer>(){
      public Integer key(IslandNode<?> island) {
        return (Integer) island.getPlayer();
      }
      public boolean join(Integer i, IslandNode<?> n) {
        if(i.equals(playerIdx)) return false;
        if(null == n.getPlayer()) return true;
        if(n.getPlayer().equals(i)) return true;
        return false;
      }
    }.aggregate(game);
    for(Entry<Integer, IslandNode<Integer>> e : nodes.entrySet())
    {
      if(e.getValue().getPlayer().equals(playerIdx))
      {
        fitness += e.getValue().geometry.getSize();
      }
      else
      {
        fitness -= e.getValue().geometry.getSize();
      }
//      Map<IslandNode<Integer>, Set<Tile>> x = e.getValue().neighbors(game);
//      if(x.size() == 1)
//      {
//        
//      }
    }

    return wrap(fitness);
  }
  
  public static abstract class GoAggregator<T>
  {
    public abstract T key(IslandNode<?> island);
    public abstract boolean join(T i, IslandNode<?> n);

    public Map<Integer, IslandNode<T>> aggregate(GoGame game) {
      Map<Integer, IslandNode<Integer>> islands = game.islands;
      Map<IslandGeometry, T> joinedIslands = new HashMap<IslandGeometry, T>();
      Set<IslandGeometry> countedInput = new HashSet<IslandGeometry>();
      for (IslandNode<Integer> island : islands.values()) {
        T key = key(island);
        if(null == key) continue;
        if(countedInput.contains(island)) continue;
        List<IslandNode<Integer>> neighbors = new ArrayList<IslandNode<Integer>>();
        neighbors.add(island);
        while(true)
        {
          List<IslandNode<Integer>> toAdd = new ArrayList<IslandNode<Integer>>();
          for(IslandNode<Integer> i : neighbors)
          {
            for(IslandNode<Integer> n : i.neighbors(game).keySet())
            {
              if(join(key, n) && !neighbors.contains(n))
              {
                toAdd.add(n);
              }
            }
          }
          if(0 == toAdd.size()) break;
          neighbors.addAll(toAdd);
        }
        List<IslandGeometry> geometries = new ArrayList<IslandGeometry>();
        for(IslandNode<Integer> n : neighbors) geometries.add(n.geometry);
        countedInput.addAll(geometries);
        joinedIslands.put(new IslandGeometry(geometries.toArray(new IslandGeometry[]{})), key);
      }
      Map<Integer, IslandNode<T>> nodes = new HashMap<Integer, IslandNode<T>>();
      for (Entry<IslandGeometry, T> e : joinedIslands.entrySet()) {
        IslandNode<T> islandNode = new IslandNode<T>(e.getValue(), e.getKey(), joinedIslands.keySet());
        nodes.put(islandNode.getId(), islandNode);
      }
      return nodes;
    }
    
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
      }
    };
  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName();
  }

}
