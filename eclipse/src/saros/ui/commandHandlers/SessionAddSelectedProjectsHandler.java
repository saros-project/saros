package de.fu_berlin.inf.dpp.ui.commandHandlers;

import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.ui.util.CollaborationUtils;
import de.fu_berlin.inf.dpp.ui.util.selection.retriever.SelectionRetrieverFactory;
import java.util.List;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;

/** Handles the addition of selected {@link IResource}s to the running {@link ISarosSession}. */
public class SessionAddSelectedProjectsHandler extends AbstractHandler {

  @Override
  public Object execute(ExecutionEvent event) throws ExecutionException {

    List<IResource> selectedResources =
        SelectionRetrieverFactory.getSelectionRetriever(IResource.class).getSelection();

    CollaborationUtils.addResourcesToSession(selectedResources);
    return null;
  }
}
