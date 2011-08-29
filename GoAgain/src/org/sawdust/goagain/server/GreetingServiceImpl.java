package org.sawdust.goagain.server;

import java.util.HashMap;
import java.util.Map;

import org.sawdust.goagain.shared.GameData;
import org.sawdust.goagain.shared.GameId;
import org.sawdust.goagain.shared.GameRecord;
import org.sawdust.goagain.shared.GoGame;
import org.sawdust.goagain.shared.GoAI;
import org.sawdust.goagain.shared.GreetingService;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

@SuppressWarnings("serial")
public class GreetingServiceImpl extends RemoteServiceServlet implements
    GreetingService {

  public Map<String, GameRecord> db = new HashMap<String, GameRecord>();
  
  public GoGame move(GoGame game, GoAI goAI) {
    goAI.move(game);
    return game;
  }

  public GameId saveGame(GameId key, GameData data) {
    if(!db.containsKey(key.key)) throw new RuntimeException("Game not found: " + key.key);
    GameRecord gameRecord = db.get(key.key);
    if(!gameRecord.activeId.equals(key)) throw new RuntimeException("Bad version: " + key.version);
    gameRecord.activeId = new GameId(key.key, key.version + 1);
    gameRecord.data = data;
    return gameRecord.activeId;
  }

  public GameRecord getGame(GameId key) {
    if(!db.containsKey(key.key)) throw new RuntimeException("Game not found: " + key.key);
    GameRecord record = db.get(key.key);
    if(key.version > 0)
    {
      if(!record.activeId.equals(key)) throw new RuntimeException("Bad version: " + key.key);
    }
    return record;
  }

  public GameId newGame(GameData data) {
    GameId newId = new GameId(Long.toHexString(System.currentTimeMillis()));
    db.put(newId.key, new GameRecord(newId, data));
    return newId;
  }

}
