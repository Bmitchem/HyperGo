package org.sawdust.goagain.shared;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Stack;
import java.util.TreeSet;

public class TreeSearchContemplation implements IterativeResult<GoGame> {

  final Stack<GoGame> games = new Stack<GoGame>();
  private final int depth;
  private final int breadth;
  private final MoveFitness<GoGame> intuition;
  private final GameFitness<GoGame> judgement;
  
  public TreeSearchContemplation(GoGame game, int depth, int breadth, MoveFitness<GoGame> intuition, GameFitness<GoGame> judgement) {
    this.games.push(game);
    this.depth = depth;
    this.breadth = breadth;
    this.intuition = intuition;
    this.judgement = judgement;
    this.best = move(new GoGame(games.peek()), this.depth);
  }

  public double think() {
    return 1.0;
  }

  private GameCommand<GoGame> best; // HACK
  public GameCommand<GoGame> best() {
    return best;
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
