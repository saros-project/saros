package saros.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import org.apache.log4j.Logger;
import saros.account.XMPPAccount;
import saros.account.XMPPAccountStore;
import saros.communication.connection.ConnectionHandler;
import saros.context.AbstractContextLifecycle;
import saros.context.ContainerContext;
import saros.context.IContextFactory;
import saros.filesystem.IProject;
import saros.filesystem.IResource;
import saros.net.xmpp.JID;
import saros.session.ISarosSessionManager;
import saros.session.SessionEndReason;

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
        .startSession(new HashMap<IProject, List<IResource>>());
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
              + "system properties saros.server.jid and"
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
