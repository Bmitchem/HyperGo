package org.sawdust.goagain.shared.go;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

import org.sawdust.goagain.shared.Move;
import org.sawdust.goagain.shared.ai.FitnessValue;
import org.sawdust.goagain.shared.ai.GameFitness;
import org.sawdust.goagain.shared.ai.IterativeResult;
import org.sawdust.goagain.shared.ai.MoveFitness;
import org.sawdust.goagain.shared.go.ai.GoAI;

public class MonteCarloContemplation implements IterativeResult<Move<GoGame>>
{
  /**
   * 
   */
  //private final GoAI goAI;

  private class Scenario
  {
    List<Double> fitness = new ArrayList<Double>();
    List<Boolean> isAlly = new ArrayList<Boolean>();
    List<Move<GoGame>> commands = new ArrayList<Move<GoGame>>();

    public Scenario(GoGame game, int depth) {
      GoGame end = game.cloneGame();
      int player = end.currentPlayer;
      IterativeResult<FitnessValue> gameFitness = judgement.gameFitness(end, player);
      while(1. > gameFitness.think()) {}
      fitness.add(gameFitness.best().fitness);
      for(int i=0; i<depth; i++)
      {
        Move<GoGame> move = weightedRandomMove(end);
        end = addMove(end, player, move);
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

    protected GoGame addMove(GoGame end, int player, Move<GoGame> move) {
      end = (GoGame) move.move().unwrap();
      boolean ally = end.currentPlayer == player;
      IterativeResult<FitnessValue> gameFitness = judgement.gameFitness(end, player);
      while(1. > gameFitness.think()) {}
      isAlly.add(ally);
      fitness.add(gameFitness.best().fitness);
      commands.add(move);
      return end;
    }

    protected Move<GoGame> weightedRandomMove(GoGame end) {
      double total = 0;
      TreeMap<Double,Move<GoGame>> commandSpace = new TreeMap<Double, Move<GoGame>>();
      for(Move<GoGame> c : end.getMoves())
      {
        commandSpace.put(total, c);
        double moveFitness = intuition.moveFitness(c, end);
        if(0 > moveFitness) throw new RuntimeException("Move with negative fitness: " + c.getCommandText());
        total += moveFitness;
      }
      Double floorKey = GoAI.floorKey(commandSpace, total * Math.random());
      Move<GoGame> move = commandSpace.get(floorKey);
      return move;
    }

    public Scenario() {
    }

    public Scenario mutate(GoGame game) {
      int step = (int) (commands.size() * Math.random());
      Scenario scenario = new Scenario();
      GoGame end = game.cloneGame();
      int player = end.currentPlayer;
      IterativeResult<FitnessValue> gameFitness = judgement.gameFitness(end, player);
      while(1. > gameFitness.think()) {}
      scenario.fitness.add(gameFitness.best().fitness);
      for(int i=0; i<commands.size(); i++)
      {
        Move<GoGame> move;
        if (i != step) {
          move = commands.get(i);
        }
        else
        {
          move = weightedRandomMove(end);
        }
        end = scenario.addMove(end, player, move);
      }
      return scenario;
    }
  }

  private final GoGame game;
  private final int depth;
  private final int breadth;
  private final int counter = 0;
  private final TreeSet<Scenario> scenarios = new TreeSet<Scenario>(new Comparator<Scenario>(){
    public int compare(Scenario o1, Scenario o2) {
      int size = o1.commands.size();
      if(size > o2.commands.size()) size = o2.commands.size();
      size--;
      double a = o1.getLiklihood(size);
      double b = o2.getLiklihood(size);
      return Double.compare(a, b);
    }});
  private final GameFitness<GoGame> judgement;
  private final MoveFitness<GoGame> intuition;

  MonteCarloContemplation(GoGame game, int depth, int breadth, MoveFitness<GoGame> intuition, GameFitness<GoGame> judgement) {
    this.game = game;
    this.depth = depth;
    this.breadth = breadth;
    this.intuition = intuition;
    this.judgement = judgement;
  }

  public double think() {
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
    return ((double)counter) / breadth;
  }

  public Move<GoGame> best() {
    Scenario best = scenarios.last();
    List<Move<GoGame>> commands = best.commands;
    Move<GoGame> move = commands.get(0);
    return move;
  }
  
}