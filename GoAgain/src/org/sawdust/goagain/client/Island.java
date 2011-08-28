package org.sawdust.goagain.client;

import java.util.Collection;
import java.util.HashSet;

public class Island
{
   
    private final int state;
    protected final Board board;
    boolean freeSpace = false;
    HashSet<Tile> positions = new HashSet<Tile>();
    public HashSet<Tile> perimiter = new HashSet<Tile>();
    
    public Island(Board board, Tile tile, Island... array) {
      this.state = board.getState(tile);
      this.board = board;
      positions.add(tile);
      perimiter.addAll(tile.neighbors());
      for(Island i : array)
      {
        positions.addAll(i.positions);
        perimiter.addAll(i.perimiter);
      }
      perimiter.removeAll(positions);
    }

    public boolean contains(final Tile p)
    {
        return positions.contains(p);
    }
    
    public Collection<Tile> getPositions()
    {
        return positions;
    }
        
    public Collection<Tile> getPerimiter()
    {
        return perimiter;
    }
    
    public int getPlayer()
    {
        return state;
    }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + state;
      result = prime * result + ((positions == null) ? 0 : positions.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj) return true;
      if (obj == null) return false;
      if (getClass() != obj.getClass()) return false;
      Island other = (Island) obj;
      if (state != other.state) return false;
      if (positions == null)
      {
         if (other.positions != null) return false;
      }
      else if (!positions.equals(other.positions)) return false;
      return true;
   }
    

   public int getSize()
   {
      return getPositions().size();
   }

  public boolean isDead() {
    for(Tile t : perimiter)
    {
      if(0 == board.getState(t)) return false;
    }
    return true;
  }
}
