package de.fu_berlin.inf.dpp.server;

import de.fu_berlin.inf.dpp.account.XMPPAccount;
import de.fu_berlin.inf.dpp.account.XMPPAccountStore;
import de.fu_berlin.inf.dpp.communication.connection.ConnectionHandler;
import de.fu_berlin.inf.dpp.context.AbstractContextLifecycle;
import de.fu_berlin.inf.dpp.context.ContainerContext;
import de.fu_berlin.inf.dpp.context.IContextFactory;
import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.filesystem.IReferencePoint;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.session.IReferencePointManager;
import de.fu_berlin.inf.dpp.session.ISarosSessionManager;
import de.fu_berlin.inf.dpp.session.ReferencePointManager;
import de.fu_berlin.inf.dpp.session.SessionEndReason;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import org.apache.log4j.Logger;

public class ServerLifecycle extends AbstractContextLifecycle {

  private static final Logger log = Logger.getLogger(ServerLifecycle.class);

  @Override
  protected Collection<IContextFactory> additionalContextFactories() {
    List<IContextFactory> factories = new ArrayList<IContextFactory>();
    factories.add(new ServerContextFactory());
    return factories;
  }

  @Override
  protected void initializeContext(final ContainerContext context) {
    connectToXMPPServer(context);
    context
        .getComponent(ISarosSessionManager.class)
        .startSession(new HashMap<IReferencePoint, List<IResource>>(), new ReferencePointManager());
  }

  @Override
  protected void finalizeContext(final ContainerContext context) {
    context.getComponent(ISarosSessionManager.class).stopSession(SessionEndReason.LOCAL_USER_LEFT);

    context.getComponent(ConnectionHandler.class).disconnect();
  }

  /*
   * FIXME This is currently ALPHA, the server always assumes it can connect to
   * the XMPP server and never gets disconnected. Of course this is unrealistic !
   */
  private void connectToXMPPServer(final ContainerContext context) {
    String jidString = ServerConfig.getJID();
    String password = ServerConfig.getPassword();

    if (jidString == null || password == null) {
      log.fatal(
          "XMPP credentials are missing! Pass the "
              + "system properties de.fu_berlin.inf.dpp.server.jid and"
              + "de.fu_berln.inf.dpp.server.password to the server");
      stop();
      System.exit(1);
    }

    /*
     * TODO we do not need a JID store, add a method the ConnectionHandler
     * instead
     */
    XMPPAccountStore store = context.getComponent(XMPPAccountStore.class);
    XMPPAccount account = store.findAccount(jidString);
    if (account == null) {
      JID jid = new JID(jidString);
      account = store.createAccount(jid.getName(), password, jid.getDomain(), "", 0, true, true);
    }

    ConnectionHandler connectionHandler = context.getComponent(ConnectionHandler.class);
    connectionHandler.connect(account, false);
  }
}
