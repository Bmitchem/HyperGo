package org.sawdust.goagain.client;

import java.util.Map.Entry;

import org.sawdust.goagain.shared.boards.BoardLayout;
import org.sawdust.goagain.shared.boards.HexagonalLayout;
import org.sawdust.goagain.shared.boards.RectangularLayout;
import org.sawdust.goagain.shared.boards.TriangularLayout;
import org.sawdust.goagain.shared.go.GoGame;
import org.sawdust.goagain.shared.go.IslandNode;
import org.sawdust.goagain.shared.go.Tile;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.CssColor;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public abstract class GoBoardWidget extends SimplePanel {
  static final CssColor black = CssColor.make("black");
  static final CssColor grey = CssColor.make("grey");
  static final CssColor tan = CssColor.make("tan");
  static final CssColor white = CssColor.make("white");

  
  private double boldness = 1.;
  private boolean connectDots = true;
  
  private final TextArea info = new TextArea();
  private final Canvas canvas = Canvas.createIfSupported();
  private int height;
  private int width;
  private GoGame game;

  public GoBoardWidget() {
    this(null);
  }
  
  public GoBoardWidget(GoGame game) {
    super();
    
    this.setGame(game);
    
    if (canvas == null) {
      add(new Label("HTML Canvas Element not supported"));
      return;
    }
    add(canvas);
    
    info.setVisibleLines(7);
    info.setWidth("100%");

    canvas.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        Tile nearestTile = GoBoardWidget.this.getGame().nearestTile(event.getX(), event.getY(), getWidth(), getHeight());
        GoBoardWidget.this.getGame().play(nearestTile);
        redraw();
        save();
      }
    });
  }
  
  protected abstract void save();

  public void draw(Context2d context) {
    if(null == game) return;
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
      for(IslandNode i : game.islands.values())
      {
        if(i.getPlayer() == 0) continue;
        for(Tile tile : i.geometry.getPositions())
        {
          double x1 = tile.x * size;
          double y1 = tile.y * size;
          for(Tile n : tile.neighbors())
          {
            if(i.geometry.contains(n))
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
    for(IslandNode i : game.islands.values())
    {
      for(Tile tile : i.geometry.getPositions())
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

  public void redraw() {
    Context2d context = canvas.getContext2d();
    context.clearRect(0, 0, getWidth(), getHeight());
    draw(context);
  }

  public void setHeight(int height) {
    this.height = height;
    canvas.setHeight(height + "px");
    canvas.setCoordinateSpaceHeight(height);
  }

  public int getHeight() {
    return height;
  }

  public void setWidth(int width) {
    this.width = width;
    canvas.setCoordinateSpaceWidth(width);
  }

  public int getWidth() {
    return width;
  }

  public void setGame(GoGame game) {
    this.game = game;
    redraw();
  }

  public GoGame getGame() {
    return game;
  }


  protected boolean announceWinner() {
    if (null != game.winner) {
      final DialogBox dialogBox = new DialogBox();
      VerticalPanel dialogVPanel = new VerticalPanel();
      dialogBox.add(dialogVPanel);

      HTML w = new HTML();
      w.setText((game.winner == 0 ? "Black" : "White") + " Player Won!");
      dialogVPanel.add(w);

      Button close = new Button("OK");
      dialogVPanel.add(close);
      close.addClickHandler(new ClickHandler() {
        public void onClick(ClickEvent event) {
          game.reset();
          dialogBox.hide();
          redraw();
          save();
        }
      });

      dialogBox.center();
      dialogBox.show();
      return true;
    }
    return false;
  }
}