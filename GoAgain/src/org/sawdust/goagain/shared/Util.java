package org.sawdust.goagain.shared;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

public class Util
{
  public static <T,K> T randomValue(Map<K, T> map) {
    TreeMap<Double, K> randomOrder = new TreeMap<Double, K>();
    for(K key : map.keySet())
    {
      randomOrder.put(Math.random(), key);
    }
    T t = map.get(randomOrder.entrySet().iterator().next().getValue());
    return t;
  }
  public static <T> T randomValue(Collection<T> collection) {
    Iterator<T> iterator = collection.iterator();
    T t = null;
    double index = collection.size()*Math.random();
    for(int i=0;i<index;i++) t = iterator.next();
    return t;
  }
}
