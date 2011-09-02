package org.sawdust.goagain.shared.ai;

import org.sawdust.goagain.shared.GameCommand;


public interface IterativeResult<T> {
  public abstract double think();

  public abstract GameCommand<T> best();

}