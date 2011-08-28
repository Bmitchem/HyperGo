package org.sawdust.goagain.server;

import org.sawdust.goagain.shared.GoGame;
import org.sawdust.goagain.shared.GoAI;
import org.sawdust.goagain.shared.GreetingService;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

@SuppressWarnings("serial")
public class GreetingServiceImpl extends RemoteServiceServlet implements
    GreetingService {

  public GoGame move(GoGame game, GoAI goAI) {
    goAI.move(game);
    return game;
  }

}
