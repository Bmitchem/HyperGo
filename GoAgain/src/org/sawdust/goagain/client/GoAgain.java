package org.sawdust.goagain.client;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.dom.client.Style.VerticalAlign;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasVerticalAlignment.VerticalAlignmentConstant;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class GoAgain implements EntryPoint {

  //private final GreetingServiceAsync greetingService = GWT.create(GreetingService.class);

  private Canvas canvas;

  private int height;
  private int width;
  Board game = new Board();
  GoAI[] ai = {new GoAI(), new GoAI()};

  private Widget infoWidget;


  public void onModuleLoad() {

    final SplitLayoutPanel layoutPanel = new SplitLayoutPanel();
    RootPanel.get().add(layoutPanel);
    
    height = Window.getClientHeight();
    width = Window.getClientWidth();
    layoutPanel.setWidth("100%");
    layoutPanel.setHeight(height + "px");
    
    
    infoWidget = getInfoWidget(layoutPanel);
    
    
    canvas = Canvas.createIfSupported();
    if (canvas == null) {
      RootPanel.get().add(new Label("HTML Canvas Element not supported"));
      return;
    }
    canvas.setHeight(height + "px");
    canvas.setCoordinateSpaceWidth(width);
    canvas.setCoordinateSpaceHeight(height);
    layoutPanel.add(canvas);
    
    addHandlers();
    update();
    
    Window.addResizeHandler(new ResizeHandler() {
      public void onResize(ResizeEvent event) {
        height = event.getHeight();
        width = event.getWidth();
        layoutPanel.setHeight(height + "px");
        canvas.setHeight(height + "px");
        canvas.setCoordinateSpaceHeight(height);
        canvas.setCoordinateSpaceWidth(width);
        infoWidget.setHeight(height + "px");
        update();
      }
    });
    
  }

  boolean autoplay = false;
  boolean aiEnabled = true;
  
  private Widget getInfoWidget(SplitLayoutPanel layoutPanel) {
    VerticalPanel vpanel = new VerticalPanel();
    vpanel.setWidth("100%");
    vpanel.setHeight(height + "px");
    vpanel.setSpacing(0);
    layoutPanel.addEast(vpanel, 200);

    {
      final Button button = new Button("Reset Game");
      addControl(vpanel, button, new ClickHandler() {
        public void onClick(ClickEvent event) {
          game.reset();
          update();
        }
      });
    }
    
    {
        final Button button = new Button("Demo " + (autoplay?"ON":"OFF"));
        addControl(vpanel, button, new ClickHandler() {
          public void onClick(ClickEvent event) {
            autoplay = !autoplay;
            button.setText("Demo " + (autoplay?"ON":"OFF"));
          }
        });
    }
    
    {
      final Button button = new Button("Computer Player " + (aiEnabled?"ON":"OFF"));
      addControl(vpanel, button, new ClickHandler() {
        public void onClick(ClickEvent event) {
          aiEnabled = !aiEnabled;
          button.setText("Computer Player " + (aiEnabled?"ON":"OFF"));
        }
      });
    }
    

    {
      final Button button = new Button("Configure Game");
      addControl(vpanel, button, new ClickHandler() {
        public void onClick(ClickEvent event) {
          final DialogBox dialogBox = new DialogBox();
          VerticalPanel dialogVPanel = new VerticalPanel();
          dialogBox.add(dialogVPanel);
          
          dialogVPanel.add(game.getConfigWidget());

          Button close = new Button("OK");
          dialogVPanel.add(close);
          close.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
              dialogBox.hide();
              update();
            }
          });
          
          dialogBox.center();
          dialogBox.show();
        }
      });
    }

    {
      final Button button = new Button("Black AI");
      addControl(vpanel, button, new ClickHandler() {
        public void onClick(ClickEvent event) {
          final DialogBox dialogBox = new DialogBox();
          VerticalPanel dialogVPanel = new VerticalPanel();
          dialogBox.add(dialogVPanel);
          
          dialogVPanel.add(ai[0].getConfigWidget());

          Button close = new Button("OK");
          dialogVPanel.add(close);
          close.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
              dialogBox.hide();
              update();
            }
          });
          
          dialogBox.center();
          dialogBox.show();
        }
      });
    }

    {
      final Button button = new Button("White AI");
      addControl(vpanel, button, new ClickHandler() {
        public void onClick(ClickEvent event) {
          final DialogBox dialogBox = new DialogBox();
          VerticalPanel dialogVPanel = new VerticalPanel();
          dialogBox.add(dialogVPanel);
          
          dialogVPanel.add(ai[1].getConfigWidget());

          Button close = new Button("OK");
          dialogVPanel.add(close);
          close.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
              dialogBox.hide();
              update();
            }
          });
          
          dialogBox.center();
          dialogBox.show();
        }
      });
    }

    Widget boardInfo = game.getInfoWidget();
    vpanel.add(boardInfo);
    int h = vpanel.getOffsetHeight() - boardInfo.getAbsoluteTop();
    vpanel.setCellHeight(boardInfo, h + "px");
    //vpanel.setCellVerticalAlignment(boardInfo, VerticalPanel.ALIGN_MIDDLE);
    
    return vpanel;
  }

  protected void addControl(VerticalPanel vpanel, final Button button, ClickHandler handler) {
    button.setWidth("100%");
    button.addClickHandler(handler);
    vpanel.add(button);
    vpanel.setCellHeight(button, button.getOffsetHeight() + "px");
  }

  protected void updateAsync() {
    new Timer() {
      @Override
      public void run() {
        update();
      }
    }.schedule(1);
  }
  
  private void addHandlers() {
    canvas.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        Tile nearestTile = game.nearestTile(event.getX(),event.getY(), width, height);
        game.occupy(nearestTile, game.currentPlayer);
        update();
        if(aiEnabled) aiAsync();
      }
    });
  }
  
  protected void aiAsync() {
    new Timer() {
      @Override
      public void run() {
        if(!aiEnabled) return;
        ai[game.currentPlayer-1].move(game);
        update();
        if(null == game.winner && autoplay) aiAsync();
      }
    }.schedule(1);
  }

  private void update() {
    if(null != game.winner)
    {
      final DialogBox dialogBox = new DialogBox();
      VerticalPanel dialogVPanel = new VerticalPanel();
      dialogBox.add(dialogVPanel);
      
      HTML w = new HTML();
      w.setText((game.winner==0?"Black":"White")+" Player Won!");
      dialogVPanel.add(w);

      Button close = new Button("OK");
      dialogVPanel.add(close);
      close.addClickHandler(new ClickHandler() {
        public void onClick(ClickEvent event) {
          game.reset();
          dialogBox.hide();
          update();
        }
      });
      
      dialogBox.center();
      dialogBox.show();
    }
    Context2d context = canvas.getContext2d();
    context.clearRect(0, 0, width, height);
    game.draw(context, width, height);
  }
}
