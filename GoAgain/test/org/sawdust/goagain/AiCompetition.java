package org.sawdust.goagain;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicLong;

import org.sawdust.goagain.shared.Game;
import org.sawdust.goagain.shared.Move;
import org.sawdust.goagain.shared.Util;
import org.sawdust.goagain.shared.ai.Ai;
import org.sawdust.goagain.shared.ai.IterativeResult;
import org.sawdust.goagain.shared.ai.Ai.GameProjection;

public class AiCompetition<T extends Game<T>>
{
  Map<Integer,AtomicLong> timers = new HashMap<Integer, AtomicLong>();
  Map<Integer,AtomicLong> winrecord = new HashMap<Integer, AtomicLong>();
  public Integer winner;
  private Ai<T>[] ai;
  
  public AiCompetition(Ai<T>... ai) {
    this.ai = ai;
  }

  @SuppressWarnings("unchecked")
  public void compete(T game) {
    int turns = 0;
    long gameTimer = -System.currentTimeMillis();
    while(null == game.winner())
    {
      long moveTimer = -System.currentTimeMillis();
      int player = game.player();
      Move<T> move = move(game);
      game = (T) move.move();
      turns++;
      moveTimer += System.currentTimeMillis();
      AtomicLong playerTimer = incrementPlayerClock(player, moveTimer);
      if(2 <= verbosity) System.out.println(String.format("Player %s moved after %.3fs: %s (player clock: %.3f)", player, moveTimer/1000., move.getCommandText(), playerTimer.get()/1000.));
    }
    gameTimer += System.currentTimeMillis();
    winner = game.winner();
    incrementPlayerWin(winner);
    if(1 <= verbosity) System.out.println(String.format("Player %s wins after %s turns (game clock: %.3fs)", winner, turns, gameTimer/1000.));
  }

  final int minReps = 1;
  final int maxReps = 100000;
  public double targetThinkTime = 5000.;
  public int verbosity = 3;
  
  protected Move<T> move(T game) {
    double reps = minReps;
    long moveTimer = -System.currentTimeMillis();
    double progress = 0;
    int player = game.player();
    IterativeResult<GameProjection<T>> contemplation = ai[player-1].newContemplation(game);
    while(1. > progress){
      if(minReps > reps) reps = minReps;
      if(maxReps < reps) reps = maxReps;
      long thinkTimer = -System.currentTimeMillis();
      for(int j=0;j<reps;j++)
      {
        progress = contemplation.think();
        if(1. <= progress) 
        {
          break;
        }
      }
      thinkTimer += System.currentTimeMillis();
      if(1. > progress) 
      {
        reps *= Math.pow(targetThinkTime/thinkTimer, 0.5);
      }
      GameProjection<T> best = contemplation.best();
      Move<T> move = null==best?null:best.firstMove();
      double elapsed = (moveTimer + System.currentTimeMillis())/1000.;
      if(1. < elapsed && 1. > progress)
      {
        if(3 <= verbosity) System.out.println(String.format("%s thinking... %.1f%%: %s (%.3f)", player, progress * 100, null==move?null:move.getCommandText(), elapsed));
      }
    }
    GameProjection<T> best = contemplation.best();
    Move<T> move = best.firstMove();
    return move;
  }

  protected AtomicLong incrementPlayerClock(int player, long moveTimer) {
    AtomicLong playerTimer;
    if (timers.containsKey(player)) {
      playerTimer = timers.get(player);
    }
    else
    {
      playerTimer = new AtomicLong(0);
      timers.put(player, playerTimer);
    }
    playerTimer.addAndGet(moveTimer);
    return playerTimer;
  }

  protected AtomicLong incrementPlayerWin(int player) {
    AtomicLong wins;
    if (winrecord.containsKey(player)) {
      wins = winrecord.get(player);
    }
    else
    {
      wins = new AtomicLong(0);
      winrecord.put(player, wins);
    }
    wins.addAndGet(1);
    return wins;
  }

  public void print() {
    for(Entry<Integer, AtomicLong> e : timers.entrySet())
    {
      Ai<T> winnerAi = ai[e.getKey()-1];
      AtomicLong wins = winrecord.get(e.getKey());
      System.out.println(String.format("Player %s took a total of %.3fs and won %s times (%s)", e.getKey(), e.getValue().get() / 1000., null==wins?0:wins.get(), winnerAi));
    }
  }

  @SuppressWarnings("unchecked")
  public static <T extends Game<T>> T randomMoves(T game, int moves) {
    for(int i=0;i<moves;i++)
    {
      T newMove;
      do
      {
        newMove = (T) Util.randomValue(game.getMoves()).move();
      } while(null == newMove);
      game = newMove;
    }
    return game;
  }
}