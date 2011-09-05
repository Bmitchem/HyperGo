package org.sawdust.goagain.client;

import java.util.Map.Entry;

import org.sawdust.goagain.shared.boards.BoardLayout;
import org.sawdust.goagain.shared.boards.HexagonalLayout;
import org.sawdust.goagain.shared.boards.RectangularLayout;
import org.sawdust.goagain.shared.boards.TriangularLayout;
import org.sawdust.goagain.shared.go.GoGame;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class GoConfigPanel extends SimplePanel {

  private final GoGameController controller;
  
  private final VerticalPanel vpanel = new VerticalPanel();
  private final Button aiEnabledButton;
  private final Button demoButton;

  private boolean aiEnabled = true;
  private boolean autoplay = false;
  private boolean persist = false;
  
  public GoConfigPanel(GoGameController goAgain) {
    super();
    this.controller = goAgain;
    add(vpanel);

    vpanel.setSpacing(0);

    {
      final Button button = new Button("Pass");
      button.setWidth("100%");
      button.addClickHandler(new ClickHandler() {
              public void onClick(ClickEvent event) {
                controller.board.getGame().pass();
                controller.board.redraw();
                controller.board.save();
              }
            });
      vpanel.add(button);
      vpanel.setCellHeight(button, button.getOffsetHeight() + "px");
    }

    {
      final Button button = new Button("Reset Game");
      button.setWidth("100%");
      button.addClickHandler(new ClickHandler() {
              public void onClick(ClickEvent event) {
                controller.board.setGame(controller.board.getGame().reset());
              }
            });
      vpanel.add(button);
      vpanel.setCellHeight(button, button.getOffsetHeight() + "px");
    }

    {
      demoButton = new Button("Demo " + (isAutoplay() ? "ON" : "OFF"));
      Button button = demoButton;
      button.setWidth("100%");
      button.addClickHandler(new ClickHandler() {
              public void onClick(ClickEvent event) {
                setAutoplay(!isAutoplay());
              }
            });
      vpanel.add(button);
      vpanel.setCellHeight(button, button.getOffsetHeight() + "px");
    }

    {
      aiEnabledButton = new Button("Computer Player " + (isAiEnabled() ? "ON" : "OFF"));
      Button button = aiEnabledButton;
      button.setWidth("100%");
      button.addClickHandler(new ClickHandler() {
              public void onClick(ClickEvent event) {
                boolean newV = !isAiEnabled();
                setAiEnabled(newV);
                if(newV)
                {
                  controller.ai.start();
                }
              }
            });
      vpanel.add(button);
      vpanel.setCellHeight(button, button.getOffsetHeight() + "px");
    }

    {
      final Button button = new Button("Persistance " + (isPersist() ? "ON" : "OFF"));
      ClickHandler handler = new ClickHandler() {
        public void onClick(ClickEvent event) {
          setPersist(!isPersist());
          button.setText("Persistance " + (isPersist() ? "ON" : "OFF"));
          if(isPersist())
          {
            controller.saveStateAsync();
          }
        }
      };
      button.setWidth("100%");
      button.addClickHandler(handler);
      vpanel.add(button);
      vpanel.setCellHeight(button, button.getOffsetHeight() + "px");
    }

    {
      final Button button = new Button("Configure Game");
      button.setWidth("100%");
      button.addClickHandler(new ClickHandler() {
              public void onClick(ClickEvent event) {
                final DialogBox dialogBox = new DialogBox();
                VerticalPanel dialogVPanel = new VerticalPanel();
                dialogBox.add(dialogVPanel);
      
                dialogVPanel.add(getConfigWidget());
      
                Button close = new Button("OK");
                dialogVPanel.add(close);
                close.addClickHandler(new ClickHandler() {
                  public void onClick(ClickEvent event) {
                    dialogBox.hide();
                    controller.board.redraw();
                    controller.saveState();
                  }
                });
      
                dialogBox.center();
                dialogBox.show();
              }
            });
      vpanel.add(button);
      vpanel.setCellHeight(button, button.getOffsetHeight() + "px");
    }

    {
      final Button button = new Button("Black AI");
      button.setWidth("100%");
      button.addClickHandler(new ClickHandler() {
              public void onClick(ClickEvent event) {
                final DialogBox dialogBox = new DialogBox();
                VerticalPanel dialogVPanel = new VerticalPanel();
                dialogBox.add(dialogVPanel);
      
                dialogVPanel.add(GoAiConfig.getConfigWidget(controller.ai.ai[0]));
      
                Button close = new Button("OK");
                dialogVPanel.add(close);
                close.addClickHandler(new ClickHandler() {
                  public void onClick(ClickEvent event) {
                    dialogBox.hide();
                    controller.saveState();
                  }
                });
      
                dialogBox.center();
                dialogBox.show();
              }
            });
      vpanel.add(button);
      vpanel.setCellHeight(button, button.getOffsetHeight() + "px");
    }

    {
      final Button button = new Button("White AI");
      button.setWidth("100%");
      button.addClickHandler(new ClickHandler() {
              public void onClick(ClickEvent event) {
                final DialogBox dialogBox = new DialogBox();
                VerticalPanel dialogVPanel = new VerticalPanel();
                dialogBox.add(dialogVPanel);
      
                dialogVPanel.add(GoAiConfig.getConfigWidget(controller.ai.ai[1]));
      
                Button close = new Button("OK");
                dialogVPanel.add(close);
                close.addClickHandler(new ClickHandler() {
                  public void onClick(ClickEvent event) {
                    dialogBox.hide();
                    controller.saveState();
                  }
                });
      
                dialogBox.center();
                dialogBox.show();
              }
            });
      vpanel.add(button);
      vpanel.setCellHeight(button, button.getOffsetHeight() + "px");
    }

    Widget boardInfo = controller.board.getInfo();
    vpanel.add(boardInfo);
    int h = vpanel.getOffsetHeight() - boardInfo.getAbsoluteTop();
    vpanel.setCellHeight(boardInfo, h + "px");
    // vpanel.setCellVerticalAlignment(boardInfo, VerticalPanel.ALIGN_MIDDLE);
  
  }

  void setAiEnabled(boolean aiEnabled) {
    this.aiEnabled = aiEnabled;
    aiEnabledButton.setText("Computer Player " + (isAiEnabled() ? "ON" : "OFF"));
  }

  public void setAutoplay(boolean autoplay) {
    this.autoplay = autoplay;
    demoButton.setText("Demo " + (isAutoplay() ? "ON" : "OFF"));
  }
  
  boolean isAiEnabled() {
    return aiEnabled;
  }

  public boolean isAutoplay() {
    return autoplay;
  }

  public void setPersist(boolean persist) {
    this.persist = persist;
  }

  public boolean isPersist() {
    return persist;
  }

  
  public Widget getConfigWidget() {
    VerticalPanel verticalPanel = new VerticalPanel();
    {
      CheckBox w = new CheckBox("Connect the dots");
      w.setValue(controller.board.connectDots);
      w.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
        
        public void onValueChange(ValueChangeEvent<Boolean> event) {
          controller.board.connectDots = event.getValue();
        }
      });
      verticalPanel.add(w);
    }

    {
      CheckBox w = new CheckBox("Score Prisoners");
      w.setValue(controller.board.getGame().scorePrisoners);
      w.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
        
        public void onValueChange(ValueChangeEvent<Boolean> event) {
          controller.board.getGame().scorePrisoners = event.getValue();
        }
      });
      verticalPanel.add(w);
    }
    
    final ListBox w = new ListBox();
    for(Entry<String, BoardLayout> e : BoardLayout.layouts.entrySet())
    {
      w.addItem(e.getKey());
      if(controller.board.getGame().getLayout() == e.getValue()) 
      {
        w.setSelectedIndex(w.getItemCount()-1);
      }
    }
    final VerticalPanel layoutConfig = new VerticalPanel();
    w.addChangeHandler(new ChangeHandler() {
      public void onChange(ChangeEvent event) {
        controller.board.setGame(controller.board.getGame().setLayout(BoardLayout.layouts.get(w.getValue(w.getSelectedIndex()))));
        layoutConfig.clear();
        populateLayoutConfigWidget(layoutConfig);
      }
    });
    verticalPanel.add(w);
    verticalPanel.add(layoutConfig);
    populateLayoutConfigWidget(layoutConfig);
    return verticalPanel;
  }

  protected void populateLayoutConfigWidget(VerticalPanel layoutConfig) {
    final GoGame game = controller.board.getGame();
    if(game.getLayout() instanceof RectangularLayout)
    {
      rectangularLayoutConfig(layoutConfig);
    }
    else if(game.getLayout() instanceof HexagonalLayout)
    {
      hexLayoutConfig(layoutConfig);
    }
    else if(game.getLayout() instanceof TriangularLayout)
    {
      triangularLayoutConfig(layoutConfig);
    }
    else 
    {
      layoutConfig.add(new Label("Unknown layout type - cannot configure"));
    }
  }

  protected void rectangularLayoutConfig(VerticalPanel verticalPanel) {
    final GoGame game = controller.board.getGame();
    final RectangularLayout layout = (RectangularLayout) game.getLayout();
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
          controller.board.setGame(game.reset());
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
          controller.board.setGame(game.reset());
        }
      });
      verticalPanel.add(panel);
    }
  }

  protected void hexLayoutConfig(VerticalPanel verticalPanel) {
    final GoGame game = controller.board.getGame();
    final HexagonalLayout layout = (HexagonalLayout) game.getLayout();
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
          controller.board.setGame(game.reset());
        }
      });
      verticalPanel.add(panel);
    }
  }

  protected void triangularLayoutConfig( VerticalPanel verticalPanel) {
    final GoGame game = controller.board.getGame();
    final TriangularLayout layout = (TriangularLayout) game.getLayout();
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
          controller.board.setGame(game.reset());
        }
      });
      verticalPanel.add(panel);
    }
  }

}
