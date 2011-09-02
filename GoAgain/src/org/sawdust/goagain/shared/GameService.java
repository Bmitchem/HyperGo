package org.sawdust.goagain.shared;

import org.sawdust.goagain.shared.ai.Ai;
import org.sawdust.goagain.shared.go.GoGame;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("greet")
public interface GameService extends RemoteService {
  GoGame move(GoGame name, Ai<GoGame> goAI);
  GameId newGame(GameData data);
  GameId saveGame(GameId key, GameData name);
  GameRecord getGame(GameId key);
  String joinGame(GameId key);
}
