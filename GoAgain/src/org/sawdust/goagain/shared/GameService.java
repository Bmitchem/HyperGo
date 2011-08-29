package org.sawdust.goagain.shared;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("greet")
public interface GameService extends RemoteService {
  GoGame move(GoGame name, GoAI goAI);
  GameId newGame(GameData data);
  GameId saveGame(GameId key, GameData name);
  GameRecord getGame(GameId key);
  String joinGame(GameId key);
}
