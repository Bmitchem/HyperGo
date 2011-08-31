package org.sawdust.goagain.shared;

import java.io.Serializable;

public interface Ai<T> extends Serializable {

  IterativeResult<T> newContemplation(T game);

}