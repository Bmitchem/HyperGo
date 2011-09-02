package org.sawdust.goagain.shared.ai;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;
import java.util.TreeSet;

import org.sawdust.goagain.shared.GameCommand;
import org.sawdust.goagain.shared.go.GoGame;

public class TreeSearchContemplation implements IterativeResult<GoGame> {

  private final MoveFitness<GoGame> intuition;
  private final GameFitness<GoGame> judgement;
  double totalProgress = 0;

  private int[] breadth;
  private long breadthProduct = 1;
  public class Frame
  {
    final GoGame game;
    final Iterator<GameCommand<GoGame>> moves;
    
    GameCommand<GoGame> thisMove = null;
    GameCommand<GoGame> bestMove = null;
    double bestFitness = Integer.MIN_VALUE;
    GoGame bestEndGame = null;

    public final long denominator;
    private int counter;
    private int width;
    
    public Frame(GoGame game, long denominator) {
      this(game, denominator, breadth[stack.size()]);
    }
      public Frame(GoGame game, long denominator, int b) {
      this.width = b;
      this.game = game;
      this.counter = 0;
      Collection<GameCommand<GoGame>> nextMoves = intuition(game);
      if(this.width > nextMoves.size()) this.width = nextMoves.size();
      this.moves = nextMoves.iterator();
      this.denominator = denominator;
    }
  }
  final Stack<Frame> stack = new Stack<Frame>();
  
  
  public TreeSearchContemplation(GoGame game, MoveFitness<GoGame> intuition, GameFitness<GoGame> judgement, int... breadth) {
    this.breadth = breadth;
    for(int b : breadth) breadthProduct *= b;
    this.stack.push(new Frame(game, 1));
    this.intuition = intuition;
    this.judgement = judgement;
  }

  public double think() {
    Frame frame = stack.peek();
    if(frame.moves.hasNext() && frame.counter++ < frame.width)
    {
      frame.thisMove = frame.moves.next();
      GoGame hypotheticalGame = frame.game.cloneGame();
      try {
        frame.thisMove.move(hypotheticalGame);
        if (stack.size() < breadth.length)
        {
          this.stack.push(new Frame(hypotheticalGame, frame.denominator * frame.width));
        }
        else if (frame.denominator * frame.width * breadth[breadth.length-1] < breadthProduct)
        {
          this.stack.push(new Frame(hypotheticalGame, frame.denominator * frame.width, breadth[breadth.length-1]));
        }
        else
        {
          double fitness = judgement.gameFitness(hypotheticalGame, frame.game.currentPlayer);
          if (fitness > frame.bestFitness)
          {
            frame.bestMove = frame.thisMove;
            frame.bestFitness = fitness;
            frame.bestEndGame = hypotheticalGame;
          }
          totalProgress += 1. / (frame.width*frame.denominator);
        }
      } catch (Exception e) {
      }
    }
    else if(stack.size() > 1)
    {
      stack.pop();
      Frame parent = stack.peek();
      double fitness = judgement.gameFitness(frame.bestEndGame, parent.game.currentPlayer);
      if (fitness > parent.bestFitness)
      {
        parent.bestMove = parent.thisMove;
        parent.bestFitness = fitness;
        parent.bestEndGame = frame.bestEndGame;
      }
    }
    else
    {
      return 1;
    }
    return totalProgress;
  }

  public GameCommand<GoGame> best() {
    for(Frame f : stack)
    {
      if(null != f.bestMove) return f.bestMove;
    }
    return null;
  }

  protected Collection<GameCommand<GoGame>> intuition(final GoGame game) {
    ArrayList<GameCommand<GoGame>> allMoves = game.getMoves();
    if(null != intuition)
    {
      final Map<GameCommand<GoGame>,Double> fitnessCache = new HashMap<GameCommand<GoGame>,Double>();
      TreeSet<GameCommand<GoGame>> sortedMoves = new TreeSet<GameCommand<GoGame>>(new Comparator<GameCommand<GoGame>>()
          {
            public int compare(GameCommand<GoGame> o1, GameCommand<GoGame> o2)
            {
              double v1 = getFitness(o1);
              double v2 = getFitness(o2);
              int compare1 = Double.compare(v2, v1);
              if(0 == compare1) compare1 = o1.getCommandText().compareTo(o2.getCommandText()); 
              return compare1;
            }

            protected double getFitness(GameCommand<GoGame> move) {
              double fitness;
              if (!fitnessCache.containsKey(move)) {
                fitness = intuition.moveFitness(move, game);
                fitnessCache.put(move, fitness);
              }
              else
              {
                fitness = fitnessCache.get(move);
              }
              return fitness;
            }
          });
      // Not allowed in GWT:
      //Collections.shuffle(m);
      for(GameCommand<GoGame> move : allMoves)
      {
        double fitness;
        if (!fitnessCache.containsKey(move)) {
          fitness = intuition.moveFitness(move, game);
          fitnessCache.put(move, fitness);
        }
        else
        {
          fitness = fitnessCache.get(move);
        }
        if(fitness >= 0) sortedMoves.add(move);
      }
      return sortedMoves;
    }
    return allMoves;
  }

}
