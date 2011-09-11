package org.sawdust.goagain.shared.ai;

public interface IterativeResult<T> {
  public abstract double think();

  public abstract T best();

}