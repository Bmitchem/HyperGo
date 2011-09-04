package org.sawdust.goagain.client;

import org.sawdust.goagain.shared.GameData;
import org.sawdust.goagain.shared.GameId;
import org.sawdust.goagain.shared.GameRecord;
import org.sawdust.goagain.shared.GameService;
import org.sawdust.goagain.shared.GameServiceAsync;
import org.sawdust.goagain.shared.go.GoGame;
import org.sawdust.goagain.shared.go.ai.GoAI;

import com.google.gwt.appengine.channel.client.Channel;
import com.google.gwt.appengine.channel.client.ChannelFactory;
import com.google.gwt.appengine.channel.client.ChannelFactory.ChannelCreatedCallback;
import com.google.gwt.appengine.channel.client.SocketError;
import com.google.gwt.appengine.channel.client.SocketListener;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;

public class GoGameController implements EntryPoint {
  
  public static final GameServiceAsync service = GWT.create(GameService.class);
  
  final GoBoardWidget board = new GoBoardWidget(){
    @Override
    protected void save() {
      saveState(new AsyncCallback<Void>() {
        public void onFailure(Throwable caught) {
          loadState();
        }
        public void onSuccess(Void result) {
          if(!announceWinner() && config.isAiEnabled())
          {
            ai.start();
          }
        }
      });
    }};

  AiController ai = new AiController(this);
  GoConfigPanel config = new GoConfigPanel(this);

  boolean init = false;
  boolean channelInit = false;
  GameId gameId;
  int latestRevision;
  

  protected void init() {
    if(init) return;
    init = true;
    final SplitLayoutPanel basePanel = new SplitLayoutPanel();
    RootPanel.get().add(basePanel);

    basePanel.setWidth("100%");
    basePanel.setHeight(Window.getClientHeight() + "px");

    config.setWidth("100%");
    config.setHeight(Window.getClientHeight() + "px");
    basePanel.addEast(config, 200);
    
    board.setHeight(Window.getClientHeight());
    board.setWidth(Window.getClientWidth());
    basePanel.add(board);
    
    Window.addResizeHandler(new ResizeHandler() {
      public void onResize(ResizeEvent event) {
        board.setHeight(event.getHeight());
        board.setWidth(event.getWidth());
        basePanel.setHeight(board.getHeight() + "px");
        config.setHeight(board.getHeight() + "px");
        board.redraw();
      }
    });
  }

  protected void initSocket() {
    if(!channelInit)
    {
      channelInit = true;
      service.joinGame(gameId, new AsyncCallback<String>() {
        
        public void onFailure(Throwable caught) {
          channelInit = false;
          caught.printStackTrace(System.err);
          System.err.println("Channel open failed!");
          initSocketAsync();
        }
        public void onSuccess(final String token) {
          ChannelFactory.createChannel(token, new ChannelCreatedCallback() {
            public void onChannelCreated(Channel channel) {
              channel.open(new SocketListener() {
                public void onClose() {
                  System.out.println("Channel closed: " + token);
                  channelInit = false;
                  initSocketAsync();
                }
                public void onError(SocketError error) {
                  System.out.println("Error: " + error.getDescription());
                }
                public void onMessage(String message) {
                  int parseInt = Integer.parseInt(message.replaceAll("[\n ]", ""));
                  System.out.println("Received: " + parseInt + 
                      " (Current Version: " + gameId.version + ")" + 
                      " (Channel: " + token + ")");
                  if(gameId.version < parseInt)
                  {
                    latestRevision = parseInt;
                    loadStateAsync();
                  }
                }
                public void onOpen() {
                  System.out.println("Channel opened: " + token);
                }
              });
            }
          });
        }
      });
    }
  }

  protected void initSocketAsync() {
    new Timer(){
      @Override
      public void run() {
        initSocket();
      }}.schedule(1);
  }

  protected void loadState() {
    loadState(new AsyncCallback<Void>() {
      public void onFailure(Throwable caught) {
        caught.printStackTrace();
        Util.showDialog(
            new Label("Error Getting Game"), 
            new Label(caught.getMessage())
          ).addCloseHandler(new CloseHandler<PopupPanel>() {
            public void onClose(CloseEvent<PopupPanel> event) {
              loadState();
            }
          });
      }
      public void onSuccess(Void result) {
      }
    });
  }
  
  protected void loadState(final AsyncCallback<Void> onComplete) {
    if(!config.isPersist()) {
      onComplete.onSuccess(null);
      return;
    }
    service.getGame(gameId, new AsyncCallback<GameRecord>() {
      public void onFailure(Throwable caught) {
        System.err.println("Get Game Failed");
        onComplete.onFailure(caught);
      }
      public void onSuccess(GameRecord result) {
        System.out.println("Loaded Game Version " + result.activeId.version);
        if(latestRevision > result.activeId.version)
        {
          System.out.println("Retrying Load");
          loadStateAsync();
        }
        else
        {
          ai.ai = result.data.ai;
          gameId = result.activeId;
          init();
          board.setGame(result.data.game);
          onComplete.onSuccess(null);
        }
      }
    });
    initSocket();
  }

  protected void loadStateAsync() {
    new Timer(){
      @Override
      public void run() {
        loadState();
      }
    }.schedule(1);
  }

  public void onModuleLoad() {
    String key = Window.Location.getParameter("gameId");
    if (null == key) {
      config.setPersist(false);
      ai.ai = new GoAI[] { new GoAI(), new GoAI() };
      gameId = null;
      init();
      board.setGame(new GoGame());
      board.redraw();
    } else {
      config.setPersist(true);
      gameId = new GameId(key, 0);
      loadState(new AsyncCallback<Void>() {
        
        public void onFailure(Throwable caught) {
          caught.printStackTrace();
          Util.showDialog(
              new Label("Error Loading Game"), 
              new Label(caught.getMessage())
            );
        }
        
        public void onSuccess(Void result) {
          config.setAiEnabled(false);
        }
      });
    };
  }

  protected void saveState() {
    saveState(new AsyncCallback<Void>() {
          public void onFailure(Throwable caught) {
            loadState();
          }
          public void onSuccess(Void result) {
          }
        });
  }

  protected void saveState(final AsyncCallback<Void> onComplete) {
    if(!config.isPersist()) {
      onComplete.onSuccess(null);
      return;
    }
    GameData data = new GameData();
    data.game = board.getGame();
    data.ai = ai.ai;
    if(null == gameId)
    {
      service.newGame(data, new AsyncCallback<GameId>() {
        public void onFailure(Throwable caught) {
          caught.printStackTrace();
          Util.showDialog(new Label("Error Creating Game"), new Label(caught.getMessage()));
        }
      
        public void onSuccess(GameId result) {
          gameId = result;
          String queryString = Window.Location.getQueryString();
          if (queryString.contains("?"))
          {
            Window.Location.assign(queryString + "&gameId=" + gameId.key);
          }
          else
          {
            Window.Location.assign(queryString + "?gameId=" + gameId.key);
          }
        }
      });
    }
    else
    {
      service.saveGame(gameId, data, new AsyncCallback<GameId>() {
        public void onFailure(final Throwable caught) {
          System.err.println("Save Game Failed");
          caught.printStackTrace();
          Util.showDialog(new Label("Save Game Failed"), new Label(caught.getMessage())).addCloseHandler(new CloseHandler<PopupPanel>() {
            public void onClose(CloseEvent<PopupPanel> event) {
              onComplete.onFailure(caught);
            }
          });
        }
        public void onSuccess(GameId result) {
          System.out.println("Saved Game Version " + result.version);
          gameId = result;
          onComplete.onSuccess(null);
        }
      });
    }
  }

  void saveStateAsync() {
    saveStateAsync(new AsyncCallback<Void>() {
      public void onFailure(Throwable caught) {
        loadState();
      }
      public void onSuccess(Void result) {
      }
    });
  }

  void saveStateAsync(final AsyncCallback<Void> handler) {
    new Timer(){
      @Override
      public void run() {
        saveState(handler);
      }
    }.schedule(1);
  }

  protected void updateAsync() {
    new Timer() {
      @Override
      public void run() {
        board.announceWinner();
        board.redraw();
        saveState();
      }
    }.schedule(1);
  }
}
