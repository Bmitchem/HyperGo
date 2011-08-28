package org.sawdust.goagain.shared;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The async counterpart of <code>GreetingService</code>.
 */
public interface GreetingServiceAsync {
  void move(GoGame name, GoAI goAI, AsyncCallback<GoGame> callback);
}
