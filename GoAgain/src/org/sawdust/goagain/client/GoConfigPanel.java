package org.sawdust.goagain.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class GoConfigPanel extends SimplePanel {

  private final GoGameController controller;
  
  private final VerticalPanel vpanel = new VerticalPanel();
  private final Button aiEnabledButton;
  private final Button demoButton;

  private boolean aiEnabled = false;
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
                controller.board.getGame().reset();
                controller.board.redraw();
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
      
                dialogVPanel.add(controller.board.getConfigWidget(controller.board.getGame()));
      
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
}
