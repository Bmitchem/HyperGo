package org.sawdust.goagain.shared;

import org.sawdust.goagain.shared.ai.Ai;
import org.sawdust.goagain.shared.go.GoGame;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The async counterpart of <code>GreetingService</code>.
 */
public interface GameServiceAsync {
  void move(GoGame name, Ai<GoGame> goAI, AsyncCallback<GoGame> callback);

  void newGame(GameData data, AsyncCallback<GameId> callback);

  void saveGame(GameId key, GameData name, AsyncCallback<GameId> callback);

  void getGame(GameId key, AsyncCallback<GameRecord> callback);

  void joinGame(GameId key, AsyncCallback<String> callback);

}
