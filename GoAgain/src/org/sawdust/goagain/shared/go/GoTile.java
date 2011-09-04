package org.sawdust.goagain.shared.go;

import java.util.ArrayList;
import java.util.Collection;


@SuppressWarnings("serial")
public final class GoTile extends Tile {
  private ArrayList<Tile> list;

  protected GoTile() {
    super();
  }

  public GoTile(int idx, double x, double y, ArrayList<Tile> list) {
    super(idx, x, y);
    this.list = list;
  }

  @Override
  public Collection<Tile> neighbors() {
    return list;
  }
}