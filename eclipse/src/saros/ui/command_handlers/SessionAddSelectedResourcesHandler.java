package saros.ui.command_handlers;

import java.util.HashSet;
import java.util.List;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;
import saros.session.ISarosSession;
import saros.ui.util.WizardUtils;
import saros.ui.util.selection.retriever.SelectionRetrieverFactory;

/**
 * Handles the addition of selected {@link IResource}s to the running {@link ISarosSession}.
 *
 * <p>An {@link saros.ui.wizards.AddResourcesToSessionWizard} is opened for this purpose with the
 * currently selected resources preselected.
 *
 * <p>This class is used to define the behavior of the package explorer context menu entry to add
 * reference points to a running session.
 */
public class SessionAddSelectedResourcesHandler extends AbstractHandler {

  @Override
  public Object execute(ExecutionEvent event) throws ExecutionException {

    List<IResource> selectedResources =
        SelectionRetrieverFactory.getSelectionRetriever(IResource.class).getSelection();

    WizardUtils.openAddResourcesToSessionWizard(new HashSet<>(selectedResources));

    return null;
  }
}
