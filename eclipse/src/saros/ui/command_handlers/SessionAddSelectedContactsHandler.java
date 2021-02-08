package saros.ui.command_handlers;

import java.util.List;
import javax.inject.Named;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.viewers.ISelection;
import saros.SarosPluginContext;
import saros.communication.connection.ConnectionHandler;
import saros.net.xmpp.JID;
import saros.repackaged.picocontainer.annotations.Inject;
import saros.session.ISarosSession;
import saros.session.ISarosSessionManager;
import saros.ui.util.CollaborationUtils;
import saros.ui.util.selection.SelectionUtils;

/** Handles the addition of selected {@link JID}s to the running {@link ISarosSession}. */
public class SessionAddSelectedContactsHandler {

  @Inject private ConnectionHandler connectionHandler;
  @Inject private ISarosSessionManager sessionManager;

  public SessionAddSelectedContactsHandler() {
    SarosPluginContext.initComponent(this);
  }

  @Execute
  public Object execute(@Named(IServiceConstants.ACTIVE_SELECTION) @Optional ISelection selection) {

    List<JID> jids = SelectionUtils.getAdaptableObjects(selection, JID.class);

    CollaborationUtils.addContactsToSession(jids);
    return null;
  }

  @CanExecute
  public boolean canExecute(
      @Named(IServiceConstants.ACTIVE_SELECTION) @Optional ISelection selection) {
    final ISarosSession session = sessionManager.getSession();
    if (!(connectionHandler.isConnected() && session != null && session.hasWriteAccess())) {
      return false;
    }
    List<JID> jids = SelectionUtils.getAdaptableObjects(selection, JID.class);
    for (JID jid : jids) {
      if (session.getUsers().stream().anyMatch(u -> u.getJID().equals(jid))) {
        return false;
      }
    }
    return true;
  }
}
