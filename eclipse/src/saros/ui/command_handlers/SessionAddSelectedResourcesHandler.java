package saros.ui.command_handlers;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.inject.Named;
import org.eclipse.core.resources.IResource;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.services.IServiceConstants;
import saros.SarosPluginContext;
import saros.communication.connection.ConnectionHandler;
import saros.filesystem.IReferencePoint;
import saros.filesystem.ResourceConverter;
import saros.repackaged.picocontainer.annotations.Inject;
import saros.session.ISarosSession;
import saros.session.ISarosSessionManager;
import saros.ui.expressions.ResourcePropertyTester;
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
public class SessionAddSelectedResourcesHandler {
  @Inject private ConnectionHandler connectionHandler;
  @Inject private ISarosSessionManager sessionManager;
  @Inject private ResourcePropertyTester resourcePropertyTester;

  public SessionAddSelectedResourcesHandler() {
    SarosPluginContext.initComponent(this);
  }

  @Execute
  public Object execute() {

    List<IResource> selectedResources =
        SelectionRetrieverFactory.getSelectionRetriever(IResource.class).getSelection();

    WizardUtils.openAddResourcesToSessionWizard(new HashSet<>(selectedResources));

    return null;
  }

  @CanExecute
  public boolean canExecute(
      @Named(IServiceConstants.ACTIVE_SELECTION) @Optional IResource resource) {
    if (!(connectionHandler != null
        && connectionHandler.isConnected()
        && sessionManager != null
        && sessionManager.getSession() != null
        && sessionManager.getSession().hasWriteAccess())) {
      return false;
    }
    final ISarosSession session = sessionManager.getSession();
    Set<IReferencePoint> sharedReferencePoints = session.getReferencePoints();
    saros.filesystem.IResource wrappedResource =
        ResourceConverter.convertToResource(sharedReferencePoints, resource);
    return !session.isShared(wrappedResource);
  }
}
