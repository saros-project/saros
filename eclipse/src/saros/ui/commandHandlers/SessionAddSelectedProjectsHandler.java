package saros.ui.commandHandlers;

import java.util.List;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;
import saros.session.ISarosSession;
import saros.ui.util.CollaborationUtils;
import saros.ui.util.selection.retriever.SelectionRetrieverFactory;

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
