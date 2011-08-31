package org.sawdust.goagain.shared;


public interface IterativeResult<T> {
  public abstract double think();

  public abstract GameCommand<T> best();

}