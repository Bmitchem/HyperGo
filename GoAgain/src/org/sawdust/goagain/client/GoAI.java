package org.sawdust.goagain.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class GoAI {

  int depth = 1;
  int breadth = 20;
  
  public void move(Board board) {
    move(board, depth, breadth);
  }

  protected GameCommand<Board> move(final Board game, final int depth, final int width)
  {
      ArrayList<GameCommand<Board>> moves1 = game.getMoves();
      Collections.sort(moves1, new Comparator<GameCommand<Board>>()
      {
          public int compare(GameCommand<Board> o1, GameCommand<Board> o2)
          {
              double v1 = 0.0;
              double v2 = 0.0;
              v1 = moveFitness(o1, game, v1);
              v2 = moveFitness(o2, game, v2);
              int compare1 = Double.compare(v2, v1);
              if (0 == compare1) return (Math.random() < 0.5) ? -1 : 1;
              return compare1;
          }
      });
      ArrayList<GameCommand<Board>> moves = moves1;
      GameCommand<Board> bestMove = null;
      double bestFitness = Integer.MIN_VALUE;
      int currentWidth = 0;
      for (GameCommand<Board> thisMove : moves)
      {
          if (currentWidth++ > width) break;
          Board hypotheticalGame = new Board(game);
          ArrayList<GameCommand<Board>> moves2 = hypotheticalGame.getMoves();
          String commandText = thisMove.getCommandText();
          for (GameCommand<Board> i : moves2)
          {
            if (i.getCommandText().equals(commandText))
            {
              i.move(hypotheticalGame);
              break;
            }
          }
          if (depth > 0)
          {
            move(hypotheticalGame, depth - 1, width);
          }
          double fitness1 = gameFitness(hypotheticalGame);
          boolean isBetter = fitness1 > bestFitness;
          if (null == bestMove || isBetter)
          {
            bestMove = thisMove;
            bestFitness = fitness1;
          }
      }
      if (null != bestMove)
      {
          bestMove.move(game);
      }
      return bestMove;
  }

  protected double gameFitness(Board game)
  {
     Board goGame = game;
     if (null == game) return Integer.MIN_VALUE;
     int playerIdx = game.currentPlayer;
     int otherIdx = (playerIdx == 1) ? 2 : 1;

     // Score-level fitness
     int score1 = goGame.points[playerIdx-1];
     int score2 = goGame.points[otherIdx-1];
     int scoreDiff = score1 - score2;
     double fitness = scoreDiff * 1000;
     
     // Freedom-level fitness
     for (Island island : game.islands)
     {
        double bias = -1.0;
        double freedom = 0;
        for(Tile t : island.getPerimiter())
        {
          if(game.getState(t) == 0)
          {
            freedom += 1;
          }
        }
        if (playerIdx == island.getPlayer()) bias = 1.0;
        fitness += bias * freedom * island.getPositions().size();
     }
     
     return fitness;
  }
  
  private double moveFitness(GameCommand<Board> o1, Board game, double v1)
  {
    int playerIdx = game.currentPlayer;
    Board.Move move = ((Board.Move)o1);
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
    double x = 0.0;
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
    return x;
  }

  public Widget getConfigWidget() {
    VerticalPanel verticalPanel = new VerticalPanel();
    
    {
      HorizontalPanel panel = new HorizontalPanel();
      panel.add(new Label("Depth: "));
      IntegerBox v = new IntegerBox();
      v.setValue(depth);
      panel.add(v);
      v.addValueChangeHandler(new ValueChangeHandler<Integer>() {
        public void onValueChange(ValueChangeEvent<Integer> event) {
          depth = event.getValue();
        }
      });
      verticalPanel.add(panel);
    }
    
    {
      HorizontalPanel panel = new HorizontalPanel();
      panel.add(new Label("Bredth: "));
      IntegerBox v = new IntegerBox();
      v.setValue(breadth);
      panel.add(v);
      v.addValueChangeHandler(new ValueChangeHandler<Integer>() {
        public void onValueChange(ValueChangeEvent<Integer> event) {
          breadth = event.getValue();
        }
      });
      verticalPanel.add(panel);
    }
    
    return verticalPanel;
  }

}
