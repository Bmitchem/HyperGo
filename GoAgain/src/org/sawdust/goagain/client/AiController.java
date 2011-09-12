package org.sawdust.goagain.client;

import org.sawdust.goagain.shared.GameCommand;
import org.sawdust.goagain.shared.ai.IterativeResult;
import org.sawdust.goagain.shared.go.Game;
import org.sawdust.goagain.shared.go.GoGame;
import org.sawdust.goagain.shared.go.ai.GoAI;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

public class AiController {
  
  private final GoGameController goGameController;

  public final class AiTask extends Timer {
    private double progress = 0;
    private final IterativeResult<GameCommand<GoGame>> contemplation;
    private final AsyncCallback<Void> aiChainHandler;
    double reps = 10;

    public AiTask(GoAI goAI, AsyncCallback<Void> aiChainHandler) {
      this.aiChainHandler = aiChainHandler;
      contemplation = goAI.newContemplation(AiController.this.goGameController.board.getGame());
    }

    @Override
    public void run() {
      if (!AiController.this.goGameController.config.isAiEnabled())
      {
        hide();
      }
      else if(progress >= 1.)
      {
        finish();
      }
      else
      {
        long timer = -System.currentTimeMillis();
        for(int j=0;j<reps;j++)
        {
          progress = contemplation.think();
        }
        timer += System.currentTimeMillis();
        reps *= Math.pow(250./timer, 0.3);
        pct.setText(((int)(progress*100.)) + "%");
      }
    }

    protected void hide() {
      this.cancel();
      aiDialogBox.hide();
    }

    protected void finish() {
      try {
        GameCommand<GoGame> best = contemplation.best();
        if(null != best)
        {
          GoBoardWidget board = AiController.this.goGameController.board;
          GoGame game = board.getGame();
          Game<GoGame> move = best.move(game);
          Game<GoGame> unwrap = move.unwrap();
          board.setGame((GoGame) unwrap);
          AiController.this.goGameController.saveState(aiChainHandler);
        }
        else
        {
          aiChainHandler.onFailure(null);
          showError();
        }
      } catch (Exception e) {
        e.printStackTrace(System.err);
        showError();
        aiChainHandler.onFailure(e);
      }
      hide();
    }

    protected void showError() {
      final DialogBox dialogBox = new DialogBox();
      VerticalPanel vp = new VerticalPanel();
      dialogBox.add(vp);
      vp.add(new Label("AI gave an invalid move!"));
      final Button b = new Button("OK");
      b.addClickHandler(new ClickHandler() {
        
        public void onClick(ClickEvent event) {
          dialogBox.hide();
        }
      });
      vp.add(b);
    }
  }

  public GoAI[] ai;
  public final DialogBox aiDialogBox = new DialogBox();
  public final Label pct = new Label();
  
  public AiController(GoGameController goGameController) {
    super();
    this.goGameController = goGameController;
    VerticalPanel vp = new VerticalPanel();
    aiDialogBox.add(vp);
    vp.add(new Label("AI is thinking"));
    pct.setText("0%");
    vp.add(pct);
    Button w = new Button("Cancel");
    w.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        AiController.this.goGameController.config.setAiEnabled(false);
        aiDialogBox.hide();
      }
    });
    vp.add(w);
    aiDialogBox.center();
    aiDialogBox.hide();
  }

  public void start() {
    if (!this.goGameController.config.isAiEnabled()) return;
    final AsyncCallback<Void> aiChainHandler = new AsyncCallback<Void>() {
      public void onFailure(Throwable caught) {
        AiController.this.goGameController.loadState();
      }
      
      public void onSuccess(Void result) {
        if(!AiController.this.goGameController.board.announceWinner())
        {
          if (AiController.this.goGameController.config.isAutoplay()) start();
        }
      }
    };
    final GoAI goAI = ai[this.goGameController.board.getGame().currentPlayer - 1];
    if (goAI.useServer && !GoAI.isServer) {
      aiDialogBox.show();
      pct.setText("");
      GoGameController.service.move(this.goGameController.board.getGame(), goAI, new AsyncCallback<GoGame>() {
        public void onFailure(Throwable caught) {
          caught.printStackTrace();
          aiDialogBox.hide();
          Util.showDialog(new Label("Server Error"), new Label(caught.getMessage()));
        }

        public void onSuccess(GoGame result) {
          if(AiController.this.goGameController.config.isAiEnabled())
          {
            AiController.this.goGameController.board.setGame(result);
            AiController.this.goGameController.saveStateAsync(aiChainHandler);
            aiDialogBox.hide();
          }
        }
      });
    } else {
      new Timer(){
        @Override
        public void run() {
          aiDialogBox.show();
          new AiTask(goAI, aiChainHandler).scheduleRepeating(1);
        }}.schedule(1);
    }
  }
}