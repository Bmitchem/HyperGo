package org.sawdust.goagain.shared.go;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@SuppressWarnings("serial")
public class Island implements Serializable
{
    boolean freeSpace = false;
    public HashSet<Tile> perimiter = new HashSet<Tile>();
    HashSet<Tile> positions = new HashSet<Tile>();
    private int state;
   
    protected Island() {
      super();
    }

    public Island(Tile tile, int player) {
      this(player, tile);
    }

    public Island(Tile tile, Island... array) {
      this.state = array[0].getPlayer();
      positions.add(tile);
      Collection<Tile> neighbors = tile.neighbors();
      if(null != neighbors) perimiter.addAll(neighbors);
      for(Island i : array)
      {
        positions.addAll(i.positions);
        perimiter.addAll(i.perimiter);
      }
      perimiter.removeAll(positions);
    }

    public Island(int player, Tile... tiles) {
      this.state = player;
      for(Tile tile : tiles)
      {
        positions.add(tile);
        Collection<Tile> neighbors = tile.neighbors();
        if(null != neighbors) perimiter.addAll(neighbors);
      }
      perimiter.removeAll(positions);
    }

    public boolean contains(final Tile p)
    {
        return positions.contains(p);
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
        
    public Collection<Tile> getPerimiter()
    {
        return perimiter;
    }
    
    public int getPlayer()
    {
        return state;
    }

   public Collection<Tile> getPositions()
  {
      return positions;
  }

   public int getSize()
   {
      return getPositions().size();
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

  public boolean isDead(GoGame board) {
    for(Tile t : perimiter)
    {
      if(0 == board.getState(t)) return false;
    }
    return true;
  }

  public List<Island> getLiberties(GoGame game) {
    ArrayList<Island> list = new ArrayList<Island>();
    for(Island i : game.islands)
    {
      if(i.getPlayer()==0)
      {
        boolean isThin = i.getSize() < 4;
        if(isThin && surrounds(i))
        {
          list.add(i);
        }
      }
    }
    return list;
  }

  private boolean surrounds(Island i) {
    if(i.getPerimiter().size() > getPerimiter().size()) return false;
    for(Tile t : i.getPerimiter())
    {
      if(!getPerimiter().contains(t)) return false;
    }
    return true;
  }
}
