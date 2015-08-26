package de.fu_berlin.inf.dpp.ui.commandHandlers;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.ui.util.CollaborationUtils;
import de.fu_berlin.inf.dpp.ui.util.selection.retriever.SelectionRetrieverFactory;

/**
 * Handles the addition of selected {@link JID}s to the running
 * {@link ISarosSession}.
 */
public class SessionAddSelectedBuddiesHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {

        List<JID> jids = SelectionRetrieverFactory.getSelectionRetriever(
            JID.class).getSelection();

        CollaborationUtils.addContactsToSession(jids);
        return null;
    }

}
