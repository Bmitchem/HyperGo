package org.sawdust.goagain.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.CssColor;
import com.google.gwt.canvas.dom.client.FillStrokeStyle;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class Board {

  public static final class Move extends GameCommand<Board> {
    final Tile tile;

    public Move(Tile tile) {
      this.tile = tile;
    }

    @Override
    public void move(Board board) {
      board.occupy(tile, board.currentPlayer);
    }

    @Override
    public String getCommandText() {
      return tile.toString();
    }
  }

  int tileRows = 10;
  int tileCols = 10;
  Map<Integer, Tile> tiles;
  
  double boldness = 1.;
  final ArrayList<Island> islands = new ArrayList<Island>();
  final Map<Integer, Integer> tileState = new HashMap<Integer, Integer>();
  TextArea info = new TextArea();
  final int[] points = {0, 0};
  int currentPlayer = 1;
  Integer winner = null;
  
  public Board() {
    reset();
    info.setVisibleLines(4);
    info.setWidth("100%");
  }

  protected Map<Integer, Tile> calculateLayout() {
    Map<Integer, Tile> _tiles = new HashMap<Integer, Tile>();
    @SuppressWarnings("unchecked") final ArrayList<Tile>[][] lists = new ArrayList[tileRows][tileCols];
    for(int i=1; i<1+tileRows; i++)
    {
      for(int j=1; j<1+tileCols; j++)
      {
        final ArrayList<Tile> list = new ArrayList<Tile>();
        Tile tile = new Tile(getTileId(i, j), (1. / (1+tileCols)) * i, (1. / (1+tileRows)) * j){
          @Override
          public Collection<Tile> neighbors() {
            return list;
          }
        };
        lists[i-1][j-1] = list;
        _tiles.put(tile.idx, tile);
      }
    }
    for(int i=1; i<1+tileRows; i++)
    {
      for(int j=1; j<1+tileCols; j++)
      {
        final ArrayList<Tile> list = lists[i-1][j-1];
        if(1 < i) list.add(_tiles.get(getTileId((i-1), j)));
        if(tileRows > i) list.add(_tiles.get(getTileId((i+1), j)));
        if(1 < j) list.add(_tiles.get(getTileId(i, (j-1))));
        if(tileCols > j) list.add(_tiles.get(getTileId(i, (j+1))));
      }
    }
    return _tiles;
  }

  protected int getTileId(final int i, final int j) {
    return i + (j*tileCols);
  }

  public Board(Board game) {
    tiles = game.tiles;
    tileState.putAll(game.tileState);
    islands.addAll(game.islands);
    for(int i=0;i<points.length;i++) points[i] = game.points[i];
    currentPlayer = game.currentPlayer;
    winner = game.winner;
  }
  public Tile nearestTile(double x, double y, int width, int height) {
    double size = (width<height)?width:height;
    x /= size; y /= size;
    Tile tile = null;
    double dist = Double.MAX_VALUE;
    for(Tile t : tiles.values())
    {
      double dx = t.x - x;
      double dy = t.y - y;
      double d = Math.sqrt(dx*dx + dy*dy);
      if(d < dist)
      {
        dist = d;
        tile = t;
      }
    }
    return tile;
  }

  static final CssColor tan = CssColor.make("tan");
  static final CssColor white = CssColor.make("white");
  static final CssColor grey = CssColor.make("grey");
  static final CssColor black = CssColor.make("black");
  
  public void draw(Context2d context, double width, int height) {
    double size = (width<height)?width:height;
    context.setFillStyle(tan);
    context.fillRect(0, 0, size, size);
    for(Tile tile : tiles.values())
    {
      double x1 = tile.x * size;
      double y1 = tile.y * size;
      for(Tile n : tile.neighbors())
      {
        double x2 = n.x * size;
        double y2 = n.y * size;
        context.setStrokeStyle(grey);
        context.setLineWidth(size * 0.004 * boldness);
        context.beginPath();
        context.moveTo(x1, y1);
        context.lineTo(x2, y2);
        context.closePath();
        context.stroke();
      }
    }
    for(Island i : islands)
    {
      for(Tile tile : i.getPositions())
      {
        double x1 = tile.x * size;
        double y1 = tile.y * size;
        for(Tile n : tile.neighbors())
        {
          if(i.contains(n))
          {
            double x2 = n.x * size;
            double y2 = n.y * size;
            context.setStrokeStyle(getColor(getState(tile)));
            context.setLineWidth(size * 0.046 * boldness);
            context.beginPath();
            context.moveTo(x1, y1);
            context.lineTo(x2, y2);
            context.closePath();
            context.stroke();
          }
        }
      }
    }
    for(Tile tile : tiles.values())
    {
      double x1 = tile.x * size;
      double y1 = tile.y * size;
      int state = getState(tile);
      if(0 < state)
      {
        context.setFillStyle(getColor(state));
        context.beginPath();
        context.arc(x1, y1, size * 0.033 * boldness, 0, Math.PI * 2.0, true);
        context.closePath();
        context.fill();
      }
      else
      {
        context.setFillStyle(grey);
        context.beginPath();
        context.arc(x1, y1, size * 0.0083 * boldness, 0, Math.PI * 2.0, true);
        context.closePath();
        context.fill();
      }
    }
    StringBuffer sb = new StringBuffer();
    if(1 == currentPlayer)
    {
      sb.append("Black's Turn");
    }
    else
    {
      sb.append("White's Turn");
    }
    sb.append("\n");
    sb.append("Black Points: ");
    sb.append(points[0]);
    sb.append("\n");
    sb.append("White Points: ");
    sb.append(points[1]);
    sb.append("\n");
    info.setText(sb.toString());
  }

  private FillStrokeStyle getColor(int state) {
    if(1 == state) return black;
    if(2 == state) return white;
    return null;
  }

  public void occupy(Tile tile, int state) {
    if(0 != getState(tile)) return;
    tileState.put(tile.idx, state);
    ArrayList<Island> possiblyDeadIslands = new ArrayList<Island>();
    HashSet<Island> adjacentIslands = new HashSet<Island>();
    for(Tile t : tile.neighbors())
    {
      if(getState(t) == state)
      {
        adjacentIslands.add(getIsland(t));
      }
      else if(0 != getState(t))
      {
        possiblyDeadIslands.add(getIsland(t));
      }
    }
    for(Island island : adjacentIslands)
    {
      islands.remove(island);
    }
    Island e = new Island(this, tile, adjacentIslands.toArray(new Island[]{}));
    islands.add(e);
    possiblyDeadIslands.add(e);
    for(Island island : possiblyDeadIslands)
    {
      if(island.isDead())
      {
        islands.remove(island);
        for(Tile t : island.getPositions())
        {
          int capturedSide = getState(t);
          if(0 != capturedSide)
          {
            points[capturedSide-1]--;
            tileState.remove(t.idx);
          }
        }
      }
    }
    if(0 == getMoves().size())
    {
      int winningPoints = Integer.MIN_VALUE;
      for(int i=0;i<points.length;i++)
      {
        if(points[i] > winningPoints)
        {
          winner = i;
          winningPoints = points[i];
        }
      }
    }
    if(++currentPlayer == 3) currentPlayer = 1;
  }

  private Island getIsland(Tile t) {
    for(Island i : islands)
    {
      if(i.contains(t)) return i;
    }
    return null;
  }

  public int getState(Tile tile) {
    if(!tileState.containsKey(tile.idx)) return 0;
    return tileState.get(tile.idx);
  }

  public Widget getInfoWidget() {
    return info;
  }

  public ArrayList<GameCommand<Board>> getMoves() {
    ArrayList<GameCommand<Board>> list = new ArrayList<GameCommand<Board>>();
    for(final Tile tile : tiles.values())
    {
      if(!tileState.containsKey(tile.idx))
      {
        boolean isSurrounded = true;
        boolean isLiberty = true;
        for(Tile n : tile.neighbors())
        {
          Integer state = tileState.get(n.idx);
          if(state == null)
          {
            isSurrounded = false;
            isLiberty = false;
          }
          else if(state.equals(currentPlayer))
          {
            isSurrounded = false;
          }
          else
          {
            isLiberty = false;
          }
        }
        if(isSurrounded) continue;
        if(isLiberty) continue;
        list.add(new Move(tile));
      }
    }
    return list;
  }

  public void reset() {
    boldness = 9. / tileRows;
    tiles = Collections.unmodifiableMap(calculateLayout());
    tileState.clear();
    islands.clear();
    winner = null;
  }

  public Widget getConfigWidget() {
    VerticalPanel verticalPanel = new VerticalPanel();
    
    {
      HorizontalPanel panel = new HorizontalPanel();
      panel.add(new Label("Rows: "));
      IntegerBox v = new IntegerBox();
      v.setValue(tileRows);
      panel.add(v);
      v.addValueChangeHandler(new ValueChangeHandler<Integer>() {
        public void onValueChange(ValueChangeEvent<Integer> event) {
          tileRows = event.getValue();
          reset();
        }
      });
      verticalPanel.add(panel);
    }
    
    {
      HorizontalPanel panel = new HorizontalPanel();
      panel.add(new Label("Columns: "));
      IntegerBox v = new IntegerBox();
      v.setValue(tileCols);
      panel.add(v);
      v.addValueChangeHandler(new ValueChangeHandler<Integer>() {
        public void onValueChange(ValueChangeEvent<Integer> event) {
          tileCols = event.getValue();
          reset();
        }
      });
      verticalPanel.add(panel);
    }
    
    return verticalPanel;
  }

}
