package org.sawdust.goagain.client;

import org.sawdust.goagain.shared.go.GoGame;
import org.sawdust.goagain.shared.go.IslandNode;
import org.sawdust.goagain.shared.go.Tile;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.CssColor;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
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
  boolean connectDots = true;
  
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
        GoGame currentGame = GoBoardWidget.this.getGame();
        if(null != currentGame)
        {
          Tile nearestTile = currentGame.nearestTile(event.getX(), event.getY(), getWidth(), getHeight());
          if(null != nearestTile)
          {
            GoGame newGame = currentGame.play(nearestTile);
            if(null != newGame)
            {
              GoBoardWidget.this.setGame(newGame);
              save();
            }
          }
        }
      }
    });
  }
  
  protected abstract void save();

  public void draw(Context2d context) {
    if(null == game) return;
    double size = (width<height)?width:height;
    double boldness = this.boldness * game.getLayout().getScale();
    context.setFillStyle(tan);
    context.fillRect(0, 0, size, size);
    for(Tile tile : game.getLayout().getTiles().values())
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
    if (null != game && null != game.winner) {
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
          setGame(game.reset());
          dialogBox.hide();
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
