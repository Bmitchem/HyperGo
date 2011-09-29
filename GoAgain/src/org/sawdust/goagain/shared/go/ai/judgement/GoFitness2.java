package org.sawdust.goagain.shared.go.ai.judgement;

import java.util.ArrayList;
import java.util.Collection;
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
import org.sawdust.goagain.shared.go.Tile;

@SuppressWarnings("serial")
public class GoFitness2 implements GameFitness<GoGame>
{
  
  public IterativeResult<FitnessValue> gameFitness(GoGame game, final int playerIdx)
  {
    if (null == game) return wrap(FitnessValue.MIN_VALUE);
    if (game.winner != null) { return wrap(game.winner.equals(playerIdx) ? FitnessValue.MAX_VALUE : FitnessValue.MIN_VALUE); }
    double fitness = 0;
    fitness += fitness(game.islands, playerIdx, 0);
    fitness += 10 * fitness(getWorstCase(game, playerIdx), playerIdx, 1);
    fitness += 10 * fitness(getBestCase(game, playerIdx), playerIdx, -1);
    return wrap(fitness);
  }

  private double fitness(Map<Integer, IslandNode<Integer>> islands, final int playerIdx, int whiteSpaceMultiplier)
  {
    double fitness = 0;
    for (Entry<Integer, IslandNode<Integer>> e : islands.entrySet())
    {
      IslandNode<Integer> island = e.getValue();
      Integer player = island.getPlayer();
      if (null == player)
      {
        fitness += whiteSpaceMultiplier * island.geometry.getSize();
      }
      else if (player.equals(playerIdx))
      {
        fitness += island.geometry.getSize();
      }
      else
      {
        fitness += island.geometry.getSize();
      }
    }
    return fitness;
  }

  public static Map<Integer, IslandNode<Integer>> getBestCase(GoGame game, final int playerIdx)
  {
    int otherIdx = (playerIdx == 1) ? 2 : 1;
    return getWorstCase(game, otherIdx);
  }

  public static Map<Integer, IslandNode<Integer>> getWorstCase(GoGame game, final int playerIdx)
  {
    int otherIdx = (playerIdx == 1) ? 2 : 1;
    Map<Integer, IslandNode<Integer>> worstCase;
    worstCase = extrapolateWorstCase(game, game.islands, playerIdx, otherIdx, false, false, false);
    worstCase = extrapolateWorstCase(game, worstCase, playerIdx, otherIdx, true, false, false);
    while(true)
    {
      Map<Integer, IslandNode<Integer>> worstCase2 = extrapolateWorstCase(game, worstCase, playerIdx, otherIdx, false, false, true);
      if(worstCase2.size() == worstCase.size()) break;
      else worstCase = worstCase2;
    }
    worstCase = extrapolateWorstCase(game, worstCase, playerIdx, otherIdx, false, true, false);
    return worstCase;
  }

  private static Map<Integer, IslandNode<Integer>> extrapolateWorstCase(GoGame game, Map<Integer, IslandNode<Integer>> worstCase1, final int playerIdx, int otherIdx,
      boolean flipUnoccupied, boolean flipOpponent, boolean flipPlayer)
  {
    Map<Integer, IslandNode<Integer>> worstCase2 = new HashMap<Integer, IslandNode<Integer>>();
    Collection<IslandGeometry> unboxCollection = IslandNode.unboxCollection(worstCase2.values());
    for(Entry<Integer, IslandNode<Integer>> e : worstCase1.entrySet())
    {
      IslandNode<Integer> island = e.getValue();
      Integer player = island.getPlayer();
      if(null == player)
      {
        if (flipUnoccupied)
        {
          if (getPotentialLiberties(game.islands, island.geometry).size() >= 2)
          {
            player = otherIdx;
          }
        }
      }
      else if(player.equals(playerIdx))
      {
        if (flipPlayer)
        {
          if (countNeighborLiberties(game.islands, island) < 2)
          {
            player = null;
          }
        }
      }
      else 
      {
        if (flipOpponent)
        {
          if (getPotentialLiberties(game.islands, island.geometry).size() < 2)
          {
            player = null;
          }
        }
      }
      worstCase2.put(island.getId(), new IslandNode<Integer>(player, island.geometry, unboxCollection));
    }
    Map<Integer, IslandNode<Integer>> worstCase3 = new GoAggregator<Integer>()
    {
      public boolean join(Integer i, IslandNode<?> n)
      {
        if (i.equals(playerIdx)) return false;
        if (null == n.getPlayer()) return true;
        if (n.getPlayer().equals(i)) return true;
        return false;
      }
    }.aggregate(worstCase2);
    return worstCase3;
  }

  public static int countNeighborLiberties(Map<Integer, IslandNode<Integer>> bestCase1, IslandNode<Integer> island)
  {
    List<IslandGeometry> liberties = new ArrayList<IslandGeometry>();
    for(IslandNode<Integer> n : island.neighbors(bestCase1).keySet())
    {
      if(null == n.getPlayer())
      {
        liberties.add(n.geometry);
      }
    }
    return liberties.size();
  }

  public static boolean isImmortal(GoGame game, IslandGeometry geometry)
  {
    Map<Integer, IslandNode<Integer>> islands = game.islands;
    IslandGeometry thin = geometry.thin();
    List<Tile> potentialLiberties = getPotentialLiberties(islands, thin);
    boolean isImmortal = 2 <= potentialLiberties.size();
    return isImmortal;
  }

  public static List<Tile> getPotentialLiberties(Map<Integer, IslandNode<Integer>> islands, IslandGeometry thin)
  {
    List<Tile> potentialLiberties = new ArrayList<Tile>();
    for(Tile t : thin.getPositions())
    {
      for(IslandNode<Integer> i : islands.values())
      {
        if(i.geometry.contains(t))
        {
          if(null == i.getPlayer())
          {
            potentialLiberties.add(t);
          }
          break;
        }
      }
    }
    // TODO: Generalize this:
    if(potentialLiberties.size() == 3)
    {
      Collection<Tile> neighbors = potentialLiberties.get(0).neighbors();
      boolean n1 = neighbors.contains(potentialLiberties.get(1));
      boolean n2 = neighbors.contains(potentialLiberties.get(2));
      if(n1 && n2)
      {
        potentialLiberties.remove(0);
      }
      else if(n1)
      {
        potentialLiberties.remove(1);
      }
      else if(n2)
      {
        potentialLiberties.remove(2);
      }
    }
    return potentialLiberties;
  }
  
  public static abstract class GoAggregator<T>
  {
    public T key(IslandNode<T> island)
    {
      return island.getPlayer();
    }
    
    public abstract boolean join(T i, IslandNode<?> n);
    
    public Map<Integer, IslandNode<T>> aggregate(Map<Integer, IslandNode<T>> islands)
    {
      Map<IslandGeometry, T> joinedIslands = new HashMap<IslandGeometry, T>();
      Set<IslandGeometry> countedInput = new HashSet<IslandGeometry>();
      for (IslandNode<T> island : islands.values())
      {
        T key = key(island);
        if (null == key) continue;
        if (countedInput.contains(island)) continue;
        List<IslandNode<T>> neighbors = new ArrayList<IslandNode<T>>();
        neighbors.add(island);
        while (true)
        {
          List<IslandNode<T>> toAdd = new ArrayList<IslandNode<T>>();
          for (IslandNode<T> i : neighbors)
          {
            for (IslandNode<T> n : i.neighbors(islands).keySet())
            {
              if (join(key, n) && !neighbors.contains(n))
              {
                toAdd.add(n);
              }
            }
          }
          if (0 == toAdd.size()) break;
          neighbors.addAll(toAdd);
        }
        List<IslandGeometry> geometries = new ArrayList<IslandGeometry>();
        for (IslandNode<T> n : neighbors)
          geometries.add(n.geometry);
        countedInput.addAll(geometries);
        joinedIslands.put(new IslandGeometry(geometries.toArray(new IslandGeometry[] {})), key);
      }
      Map<Integer, IslandNode<T>> nodes = new HashMap<Integer, IslandNode<T>>();
      for (Entry<IslandGeometry, T> e : joinedIslands.entrySet())
      {
        IslandNode<T> islandNode = new IslandNode<T>(e.getValue(), e.getKey(), joinedIslands.keySet());
        nodes.put(islandNode.getId(), islandNode);
      }
      return nodes;
    }
    
  }
  
  public static IterativeResult<FitnessValue> wrap(double fitness)
  {
    return wrap(new FitnessValue(fitness, 0));
  }
  
  public static IterativeResult<FitnessValue> wrap(final FitnessValue f)
  {
    return new IterativeResult<FitnessValue>()
    {
      
      public double think()
      {
        return 1.;
      }
      
      public FitnessValue best()
      {
        return f;
      }
      
      public void hint(FitnessValue hint)
      {
      }
    };
  }
  
  @Override
  public String toString()
  {
    return this.getClass().getSimpleName();
  }
  
}
