package org.sawdust.goagain.shared;

import java.io.Serializable;

@SuppressWarnings("serial")
public class GameId implements Serializable {
  public String key;
  public int version;

  public GameId(String key, int version) {
    super();
    this.key = key;
    this.version = version;
  }
  
  public GameId(String key) {
    this(key, 1);
  }
  
  protected GameId() {
    this("NULL");
  }
  
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((key == null) ? 0 : key.hashCode());
    result = prime * result + version;
    return result;
  }
  
  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    GameId other = (GameId) obj;
    if (key == null) {
      if (other.key != null) return false;
    } else if (!key.equals(other.key)) return false;
    if (version != other.version) return false;
    return true;
  }
  
}
