package de.fu_berlin.inf.dpp.server.session;

import de.fu_berlin.inf.dpp.negotiation.hooks.SessionNegotiationHookManager;
import de.fu_berlin.inf.dpp.session.ServerStatusHook;

/** Implementation of the ServerStatusHook to report this instance as a server */
public class ServerStatusHookImpl extends ServerStatusHook {
  public ServerStatusHookImpl(SessionNegotiationHookManager manager) {
    super(manager);
  }

  @Override
  public boolean isServer() {
    return true;
  }
}
