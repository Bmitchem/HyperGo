package org.sawdust.goagain.shared;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The async counterpart of <code>GreetingService</code>.
 */
public interface GameServiceAsync {
  void move(GoGame name, GoAI goAI, AsyncCallback<GoGame> callback);

  void newGame(GameData data, AsyncCallback<GameId> callback);

  void saveGame(GameId key, GameData name, AsyncCallback<GameId> callback);

  void getGame(GameId key, AsyncCallback<GameRecord> callback);

}
