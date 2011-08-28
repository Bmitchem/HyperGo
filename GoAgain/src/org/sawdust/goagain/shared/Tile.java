package org.sawdust.goagain.shared;

import java.io.Serializable;
import java.util.Collection;

@SuppressWarnings("serial")
public abstract class Tile implements Serializable {

  int idx;
  public double x;
  public double y;

  protected Tile() {
    super();
  }

  public Tile(int idx, double x, double y) {
    super();
    this.idx = idx;
    this.x = x;
    this.y = y;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    Tile other = (Tile) obj;
    if (idx != other.idx) return false;
    return true;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + idx;
    return result;
  }

  public abstract Collection<Tile> neighbors();

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("Tile [idx=");
    builder.append(idx);
    builder.append(", x=");
    builder.append(x);
    builder.append(", y=");
    builder.append(y);
    builder.append("]");
    return builder.toString();
  }
}
