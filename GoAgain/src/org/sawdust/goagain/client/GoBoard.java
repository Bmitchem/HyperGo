package org.sawdust.goagain.client;

import org.sawdust.goagain.shared.GoGame;
import org.sawdust.goagain.shared.Island;
import org.sawdust.goagain.shared.Tile;

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.CssColor;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class GoBoard {
  static final CssColor black = CssColor.make("black");
  static final CssColor grey = CssColor.make("grey");
  static final CssColor tan = CssColor.make("tan");
  static final CssColor white = CssColor.make("white");

  double boldness = 1.;
  public GoGame game;
  private transient TextArea info = new TextArea();
  
  public GoBoard(GoGame game) {
    super();
    info.setVisibleLines(4);
    info.setWidth("100%");
    this.game = game;
  }

  public void draw(Context2d context, double width, int height) {
    boldness = 9. / game.tileRows;
    double size = (width<height)?width:height;
    context.setFillStyle(tan);
    context.fillRect(0, 0, size, size);
    for(Tile tile : game.tiles.values())
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
    for(Island i : game.islands)
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
            context.setStrokeStyle(getColor(game.getState(tile)));
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
    for(Tile tile : game.tiles.values())
    {
      double x1 = tile.x * size;
      double y1 = tile.y * size;
      int state = game.getState(tile);
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
    if(1 == game.currentPlayer)
    {
      sb.append("Black's Turn");
    }
    else
    {
      sb.append("White's Turn");
    }
    sb.append("\n");
    sb.append("Black Points: ");
    sb.append(game.points[0]);
    sb.append("\n");
    sb.append("White Points: ");
    sb.append(game.points[1]);
    sb.append("\n");
    ((TextArea)getInfo()).setText(sb.toString());
  }
  
  private CssColor getColor(int state) {
    if(1 == state) return black;
    if(2 == state) return white;
    return null;
  }

  public Widget getInfo() {
    return info;
  }

  
  public Widget getConfigWidget() {
    VerticalPanel verticalPanel = new VerticalPanel();
    
    {
      HorizontalPanel panel = new HorizontalPanel();
      panel.add(new Label("Rows: "));
      IntegerBox v = new IntegerBox();
      v.setValue(game.tileRows);
      panel.add(v);
      v.addValueChangeHandler(new ValueChangeHandler<Integer>() {
        public void onValueChange(ValueChangeEvent<Integer> event) {
          game.tileRows = event.getValue();
          game.reset();
        }
      });
      verticalPanel.add(panel);
    }
    
    {
      HorizontalPanel panel = new HorizontalPanel();
      panel.add(new Label("Columns: "));
      IntegerBox v = new IntegerBox();
      v.setValue(game.tileCols);
      panel.add(v);
      v.addValueChangeHandler(new ValueChangeHandler<Integer>() {
        public void onValueChange(ValueChangeEvent<Integer> event) {
          game.tileCols = event.getValue();
          game.reset();
        }
      });
      verticalPanel.add(panel);
    }
    
    return verticalPanel;
  }
}
