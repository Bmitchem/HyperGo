package org.sawdust.goagain.shared;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Stack;
import java.util.TreeSet;

public class TreeSearchContemplation implements IterativeResult<GoGame> {

  private final MoveFitness<GoGame> intuition;
  private final GameFitness<GoGame> judgement;
  double totalProgress = 0;

  private int[] breadth;
  public class Frame
  {
    final GoGame game;
    final Iterator<GameCommand<GoGame>> moves;
    
    GameCommand<GoGame> bestMove = null;
    double bestFitness = Integer.MIN_VALUE;
    GoGame bestEndGame = null;

    public final double progress;
    private int counter;
    private int width;
    
    public Frame(GoGame game, double progress) {
      this.game = game;
      this.counter = 0;
      Collection<GameCommand<GoGame>> nextMoves = intuition(game);
      this.width = breadth[stack.size()];
      if(this.width > nextMoves.size()) this.width = nextMoves.size();
      this.moves = nextMoves.iterator();
      this.progress = progress;
    }
  }
  final Stack<Frame> stack = new Stack<Frame>();
  
  
  public TreeSearchContemplation(GoGame game, MoveFitness<GoGame> intuition, GameFitness<GoGame> judgement, int... breadth) {
    this.breadth = breadth;
    this.stack.push(new Frame(game, 1.0));
    this.intuition = intuition;
    this.judgement = judgement;
  }

  public double think() {
    Frame frame = stack.peek();
    if(frame.moves.hasNext() && frame.counter++ < frame.width)
    {
      GameCommand<GoGame> thisMove = frame.moves.next();
      GoGame hypotheticalGame = new GoGame(frame.game);
      try {
        thisMove.move(hypotheticalGame);
        if (stack.size() < breadth.length)
        {
          this.stack.push(new Frame(hypotheticalGame, frame.progress / frame.width));
        }
        else
        {
          double fitness = judgement.gameFitness(hypotheticalGame, frame.game.currentPlayer);
          if (fitness > frame.bestFitness)
          {
            frame.bestMove = thisMove;
            frame.bestFitness = fitness;
            frame.bestEndGame = hypotheticalGame;
          }
          totalProgress += frame.progress / frame.width;
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
        parent.bestMove = frame.bestMove;
        parent.bestFitness = frame.bestFitness;
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
      TreeSet<GameCommand<GoGame>> sortedMoves = new TreeSet<GameCommand<GoGame>>(new Comparator<GameCommand<GoGame>>()
          {
        public int compare(GameCommand<GoGame> o1, GameCommand<GoGame> o2)
        {
          double v1 = intuition.moveFitness(o1, game);
          double v2 = intuition.moveFitness(o2, game);
          int compare1 = Double.compare(v2, v1);
          if(0 == compare1) compare1 = o1.getCommandText().compareTo(o2.getCommandText()); 
          return compare1;
        }
          });
      // Not allowed in GWT:
      //Collections.shuffle(m);
      sortedMoves.addAll(allMoves);
      return sortedMoves;
    }
    return allMoves;
  }

}
