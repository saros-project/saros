package saros.ui.commandHandlers;

import java.util.List;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import saros.net.xmpp.JID;
import saros.session.ISarosSession;
import saros.ui.util.CollaborationUtils;
import saros.ui.util.selection.retriever.SelectionRetrieverFactory;

/** Handles the addition of selected {@link JID}s to the running {@link ISarosSession}. */
public class SessionAddSelectedContactsHandler extends AbstractHandler {

  @Override
  public Object execute(ExecutionEvent event) throws ExecutionException {

    List<JID> jids = SelectionRetrieverFactory.getSelectionRetriever(JID.class).getSelection();

    CollaborationUtils.addContactsToSession(jids);
    return null;
  }
}
