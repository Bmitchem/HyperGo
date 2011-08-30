package org.sawdust.goagain.shared;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

@SuppressWarnings("serial")
public class GoAI implements Serializable {

  public static boolean isServer = false;
  public boolean useServer = false;
  public int depth = 3;
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

  public class Contemplation
  {
    private final GoGame game;
    private final int depth;
    private final TreeSet<Scenario> scenarios = new TreeSet<Scenario>(new Comparator<Scenario>(){
      public int compare(Scenario o1, Scenario o2) {
        int size = o1.commands.size();
        if(size > o2.commands.size()) size = o2.commands.size();
        size--;
        double a = o1.getLiklihood(size);
        double b = o2.getLiklihood(size);
        return Double.compare(a, b);
      }});

    private Contemplation(GoGame game, int depth) {
      this.game = game;
      this.depth = depth;
    }

    public void think() {
      if(scenarios.size() > 100)
      {
        Scenario best = scenarios.last();
        try {
          Scenario mutate = best.mutate(game);
          if(null != mutate) scenarios.add(mutate);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
      else
      {
        try {
          scenarios.add(new Scenario(game, depth));
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }

    public GameCommand<GoGame> best() {
      Scenario best = scenarios.last();
      List<GameCommand<GoGame>> commands = best.commands;
      GameCommand<GoGame> move = commands.get(0);
      return move;
    }
    
  }

  private class Scenario
  {
    List<Double> fitness = new ArrayList<Double>();
    List<Boolean> isAlly = new ArrayList<Boolean>();
    List<GameCommand<GoGame>> commands = new ArrayList<GameCommand<GoGame>>();

    public Scenario(GoGame game, int depth) {
      GoGame end = new GoGame(game);
      int player = end.currentPlayer;
      fitness.add(gameFitness(end, player));
      for(int i=0; i<depth; i++)
      {
        GameCommand<GoGame> move = weightedRandomMove(end);
        addMove(end, player, move);
      }
    }

    public double getLiklihood(int size) {
      double a = 0;
      for(int i=0;i<size;i++)
      {
        double delta = fitness.get(i+1) - fitness.get(i);
        if(isAlly.get(i)) delta *= -1;
        a += delta;
        a *= 2;
      }
      return a;
    }

    protected void addMove(GoGame end, int player, GameCommand<GoGame> move) {
      move.move(end);
      boolean ally = end.currentPlayer == player;
      double gameFitness = gameFitness(end, player);
      isAlly.add(ally);
      fitness.add(gameFitness);
      commands.add(move);
    }

    protected GameCommand<GoGame> weightedRandomMove(GoGame end) {
      double total = 0;
      TreeMap<Double,GameCommand<GoGame>> commandSpace = new TreeMap<Double, GameCommand<GoGame>>();
      for(GameCommand<GoGame> c : end.getMoves())
      {
        commandSpace.put(total, c);
        double moveFitness = moveFitness(c, end);
        if(0 > moveFitness) throw new RuntimeException("Move with negative fitness: " + c.getCommandText());
        total += moveFitness;
      }
      Double floorKey = floorKey(commandSpace, total * Math.random());
      GameCommand<GoGame> move = commandSpace.get(floorKey);
      return move;
    }

    public Scenario() {
    }

    public Scenario mutate(GoGame game) {
      int step = (int) (commands.size() * Math.random());
      Scenario scenario = new Scenario();
      GoGame end = new GoGame(game);
      int player = end.currentPlayer;
      scenario.fitness.add(gameFitness(end, player));
      for(int i=0; i<commands.size(); i++)
      {
        GameCommand<GoGame> move;
        if (i != step) {
          move = commands.get(i);
        }
        else
        {
          move = weightedRandomMove(end);
        }
        scenario.addMove(end, player, move);
      }
      return scenario;
    }
  }
  
  protected double gameFitness(GoGame game, int playerIdx) {
    if (null == game) return Integer.MIN_VALUE;
     int otherIdx = (playerIdx == 1) ? 2 : 1;

     // Score-level fitness
     int score1 = game.getScore(playerIdx);
     int score2 = game.getScore(otherIdx);
     int scoreDiff = score1 - score2;
     double fitness = scoreDiff * 1000;
     
     // Freedom-level fitness
     for (Island island : game.islands)
     {
        double freedom = 0;
        for(Tile t : island.getPerimiter())
        {
          if(game.getState(t) == 0)
          {
            freedom += 1;
          }
        }
        double bias = (playerIdx == island.getPlayer())?1.0:-1.0;
        fitness += bias * freedom * island.getPositions().size();
     }
     
     return fitness;
  }
  
  private double moveFitness(GameCommand<GoGame> o1, GoGame game)
  {
    int playerIdx = game.currentPlayer;
    double x = 10.0;
    if(o1 instanceof GoGame.Move)
    {
      GoGame.Move move = ((GoGame.Move)o1);
      int freindlyCount = 0;
      int enemyCount = 0;
      for(Tile t : move.tile.neighbors())
      {
        Integer state = game.tileState.get(t.idx);
        if(null != state)
        {
          if(state.equals(playerIdx))
          {
            freindlyCount++;
          }
          else
          {
            enemyCount++;
          }
        }
      }
      if(freindlyCount > 2)
      {
        x -= freindlyCount * 0.5;
      }
      else
      {
        x += freindlyCount;
      }
      if(enemyCount > 1)
      {
        x -= enemyCount;
      }
      else
      {
        x += enemyCount;
      }
    }
    else
    {
      x += 10;
    }
    return x;
  }

  public Contemplation newContemplation(GoGame game) {
    return new Contemplation(game, depth);
  }

}
