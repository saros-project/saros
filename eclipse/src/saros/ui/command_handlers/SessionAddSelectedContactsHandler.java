package saros.ui.command_handlers;

import java.util.List;
import javax.inject.Named;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.services.IServiceConstants;
import saros.SarosPluginContext;
import saros.communication.connection.ConnectionHandler;
import saros.net.xmpp.JID;
import saros.net.xmpp.contact.XMPPContactsService;
import saros.repackaged.picocontainer.annotations.Inject;
import saros.session.ISarosSession;
import saros.session.ISarosSessionManager;
import saros.ui.util.CollaborationUtils;
import saros.ui.util.selection.retriever.SelectionRetrieverFactory;

/** Handles the addition of selected {@link JID}s to the running {@link ISarosSession}. */
public class SessionAddSelectedContactsHandler {

  @Inject private ConnectionHandler connectionHandler;
  @Inject private ISarosSessionManager sessionManager;
  @Inject private XMPPContactsService contactsService;

  public SessionAddSelectedContactsHandler() {
    SarosPluginContext.initComponent(this);
  }

  @Execute
  public Object execute() {

    List<JID> jids = SelectionRetrieverFactory.getSelectionRetriever(JID.class).getSelection();

    CollaborationUtils.addContactsToSession(jids);
    return null;
  }

  @CanExecute
  public boolean canExecute(@Named(IServiceConstants.ACTIVE_SELECTION) @Optional JID contact) {
    final JID jid = contact;
    return (connectionHandler != null
        && connectionHandler.isConnected()
        && sessionManager != null
        && sessionManager.getSession() != null
        && sessionManager.getSession().hasWriteAccess()
        && !sessionManager.getSession().getUsers().stream().anyMatch(u -> u.getJID().equals(jid)));
  }
}
