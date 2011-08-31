package org.sawdust.goagain.shared;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Stack;
import java.util.TreeSet;

public class TreeSearchContemplation implements IterativeResult<GoGame> {

  private final int breadth;
  private final MoveFitness<GoGame> intuition;
  private final GameFitness<GoGame> judgement;
  double totalProgress = 0;

  public class Frame
  {
    final GoGame game;
    final Iterator<GameCommand<GoGame>> moves;
    final int depth;
    GameCommand<GoGame> bestMove = null;
    double bestFitness = Integer.MIN_VALUE;
    public final double progress;
    public int moveCount;
    
    public Frame(GoGame game, int depth, double progress) {
      this.game = game;
      this.depth = depth;
      Collection<GameCommand<GoGame>> nextMoves = intuition(game);
      this.moves = nextMoves.iterator();
      this.moveCount = nextMoves.size();
      this.progress = progress;
    }
  }
  final Stack<Frame> stack = new Stack<Frame>();
  
  
  public TreeSearchContemplation(GoGame game, int depth, int breadth, MoveFitness<GoGame> intuition, GameFitness<GoGame> judgement) {
    this.stack.push(new Frame(game, depth, 1.0));
    this.breadth = breadth;
    this.intuition = intuition;
    this.judgement = judgement;
  }

  public double think() {
    Frame frame = stack.peek();
    if(frame.moves.hasNext())
    {
      GameCommand<GoGame> thisMove = frame.moves.next();
      GoGame hypotheticalGame = new GoGame(frame.game);
      try {
        thisMove.move(hypotheticalGame);
        if (frame.depth > 1)
        {
          this.stack.push(new Frame(hypotheticalGame, frame.depth - 1, frame.progress / frame.moveCount));
        }
        else
        {
          double fitness = judgement.gameFitness(hypotheticalGame, frame.game.currentPlayer);
          if (fitness > frame.bestFitness)
          {
            frame.bestMove = thisMove;
            frame.bestFitness = fitness;
          }
        }
      } catch (Exception e) {
      }
    }
    else if(stack.size() > 1)
    {
      stack.pop();
      Frame parent = stack.peek();
      if (frame.bestFitness > parent.bestFitness)
      {
        parent.bestMove = frame.bestMove;
        parent.bestFitness = frame.bestFitness;
      }
      totalProgress += frame.progress;
    }
    else
    {
      return 1;
    }
    return totalProgress;
  }

  public GameCommand<GoGame> best() {
    return stack.peek().bestMove;
  }

  protected GameCommand<GoGame> move(final GoGame game, int d)
  {
      Collection<GameCommand<GoGame>> moves = intuition(game);
      GameCommand<GoGame> bestMove = null;
      double bestFitness = Integer.MIN_VALUE;
      for (GameCommand<GoGame> thisMove : moves)
      {
        GoGame hypotheticalGame = new GoGame(game);
        try {
          thisMove.move(hypotheticalGame);
          if (d > 1)
          {
            move(hypotheticalGame, d - 1);
          }
          double fitness = judgement.gameFitness(hypotheticalGame, game.currentPlayer);
          if (fitness > bestFitness)
          {
            bestMove = thisMove;
            bestFitness = fitness;
          }
        } catch (Exception e) {
        }
      }
      if (null != bestMove)
      {
          bestMove.move(game);
      }
      return bestMove;
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
