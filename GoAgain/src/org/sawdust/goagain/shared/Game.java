package org.sawdust.goagain.shared;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


public abstract class Game<T extends Game<T>> {
  public abstract Collection<? extends Move<T>> getMoves();
  public abstract int player();
  public abstract T unwrap();
  
  private Map<Object,Object> cache = new HashMap<Object, Object>();
  
  public Object getCache(Object key)
  {
    return cache.get(key);
  }
  
  public void putCache(Object key, Object value)
  {
    cache.put(key, value);
  }
}
