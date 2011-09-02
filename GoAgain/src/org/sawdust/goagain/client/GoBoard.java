package org.sawdust.goagain.client;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.sawdust.goagain.shared.boards.BoardLayout;
import org.sawdust.goagain.shared.boards.HexagonalLayout;
import org.sawdust.goagain.shared.boards.RectangularLayout;
import org.sawdust.goagain.shared.boards.TriangularLayout;
import org.sawdust.goagain.shared.go.GoGame;
import org.sawdust.goagain.shared.go.Island;
import org.sawdust.goagain.shared.go.Tile;

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.CssColor;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class GoBoard {
  static final CssColor black = CssColor.make("black");
  static final CssColor grey = CssColor.make("grey");
  static final CssColor tan = CssColor.make("tan");
  static final CssColor white = CssColor.make("white");

  
  double boldness = 1.;
  boolean connectDots = true;
  private transient TextArea info = new TextArea();
  
  public GoBoard() {
    super();
    info.setVisibleLines(7);
    info.setWidth("100%");
  }

  public void draw(GoGame game, Context2d context, double width, int height) {
    double size = (width<height)?width:height;
    double boldness = this.boldness * game.layout.getScale();
    context.setFillStyle(tan);
    context.fillRect(0, 0, size, size);
    for(Tile tile : game.layout.getTiles().values())
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
    if(connectDots)
    {
      for(Island i : game.islands)
      {
        if(i.getPlayer() == 0) continue;
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
              context.setStrokeStyle(getColor(i.getPlayer()));
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
    }
    for(Island i : game.islands)
    {
      for(Tile tile : i.getPositions())
      {
        double x1 = tile.x * size;
        double y1 = tile.y * size;
        if(0 < i.getPlayer())
        {
          context.setFillStyle(getColor(i.getPlayer()));
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

    if(game.scorePrisoners)
    {
      sb.append("\n");
      sb.append("Black Prisoners: ");
      sb.append(game.prisoners[0]);
      sb.append("\n");
      sb.append("White Prisoners: ");
      sb.append(game.prisoners[1]);
      
      int territoryBlack = game.getTerritory(1);
      int territoryWhite = game.getTerritory(2);
      
      sb.append("\n");
      sb.append("Black Territory: ");
      sb.append(territoryBlack);
      sb.append("\n");
      sb.append("White Territory: ");
      sb.append(territoryWhite);
    }

    sb.append("\n");
    sb.append("Black Score: ");
    sb.append(game.getScore(1));
    sb.append("\n");
    sb.append("White Score: ");
    sb.append(game.getScore(2));

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

  
  public Widget getConfigWidget(final GoGame game) {
    VerticalPanel verticalPanel = new VerticalPanel();

    {
      CheckBox w = new CheckBox("Connect the dots");
      w.setValue(connectDots);
      w.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
        
        public void onValueChange(ValueChangeEvent<Boolean> event) {
          connectDots = event.getValue();
        }
      });
      verticalPanel.add(w);
    }

    {
      CheckBox w = new CheckBox("Score Prisoners");
      w.setValue(game.scorePrisoners);
      w.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
        
        public void onValueChange(ValueChangeEvent<Boolean> event) {
          game.scorePrisoners = event.getValue();
        }
      });
      verticalPanel.add(w);
    }
    
    final ListBox w = new ListBox();
    for(Entry<String, BoardLayout> e : BoardLayout.layouts.entrySet())
    {
      w.addItem(e.getKey());
      if(game.layout == e.getValue()) 
      {
        w.setSelectedIndex(w.getItemCount()-1);
      }
    }
    final VerticalPanel layoutConfig = new VerticalPanel();
    w.addChangeHandler(new ChangeHandler() {
      public void onChange(ChangeEvent event) {
        game.layout = BoardLayout.layouts.get(w.getValue(w.getSelectedIndex()));
        layoutConfig.clear();
        populateLayoutConfigWidget(game, layoutConfig);
      }
    });
    verticalPanel.add(w);
    verticalPanel.add(layoutConfig);
    populateLayoutConfigWidget(game, layoutConfig);
    return verticalPanel;
  }

  protected void populateLayoutConfigWidget(final GoGame game, VerticalPanel layoutConfig) {
    if(game.layout instanceof RectangularLayout)
    {
      rectangularLayoutConfig(game, layoutConfig);
    }
    else if(game.layout instanceof HexagonalLayout)
    {
      hexLayoutConfig(game, layoutConfig);
    }
    else if(game.layout instanceof TriangularLayout)
    {
      triangularLayoutConfig(game, layoutConfig);
    }
    else 
    {
      layoutConfig.add(new Label("Unknown layout type - cannot configure"));
    }
  }

  protected void rectangularLayoutConfig(final GoGame game, VerticalPanel verticalPanel) {
    final RectangularLayout layout = (RectangularLayout) game.layout;
    {
      HorizontalPanel panel = new HorizontalPanel();
      panel.add(new Label("Rows: "));
      IntegerBox v = new IntegerBox();
      v.setValue(layout.tileRows);
      panel.add(v);
      v.addValueChangeHandler(new ValueChangeHandler<Integer>() {
        public void onValueChange(ValueChangeEvent<Integer> event) {
          layout.tileRows = event.getValue();
          layout.calculateLayout();
          game.reset();
        }
      });
      verticalPanel.add(panel);
    }
    
    {
      HorizontalPanel panel = new HorizontalPanel();
      panel.add(new Label("Columns: "));
      IntegerBox v = new IntegerBox();
      v.setValue(layout.tileCols);
      panel.add(v);
      v.addValueChangeHandler(new ValueChangeHandler<Integer>() {
        public void onValueChange(ValueChangeEvent<Integer> event) {
          layout.tileCols = event.getValue();
          layout.calculateLayout();
          game.reset();
        }
      });
      verticalPanel.add(panel);
    }
  }

  protected void hexLayoutConfig(final GoGame game, VerticalPanel verticalPanel) {
    final HexagonalLayout layout = (HexagonalLayout) game.layout;
    {
      HorizontalPanel panel = new HorizontalPanel();
      panel.add(new Label("Size: "));
      IntegerBox v = new IntegerBox();
      v.setValue(layout.size);
      panel.add(v);
      v.addValueChangeHandler(new ValueChangeHandler<Integer>() {
        public void onValueChange(ValueChangeEvent<Integer> event) {
          layout.size = event.getValue();
          layout.calculateLayout();
          game.reset();
        }
      });
      verticalPanel.add(panel);
    }
  }

  protected void triangularLayoutConfig(final GoGame game, VerticalPanel verticalPanel) {
    final TriangularLayout layout = (TriangularLayout) game.layout;
    {
      HorizontalPanel panel = new HorizontalPanel();
      panel.add(new Label("Size: "));
      IntegerBox v = new IntegerBox();
      v.setValue(layout.size);
      panel.add(v);
      v.addValueChangeHandler(new ValueChangeHandler<Integer>() {
        public void onValueChange(ValueChangeEvent<Integer> event) {
          layout.size = event.getValue();
          layout.calculateLayout();
          game.reset();
        }
      });
      verticalPanel.add(panel);
    }
  }
}
