package saros.ui.handlers.menu;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.inject.Named;
import org.eclipse.core.resources.IResource;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.viewers.ISelection;
import saros.SarosPluginContext;
import saros.communication.connection.ConnectionHandler;
import saros.filesystem.IReferencePoint;
import saros.filesystem.ResourceConverter;
import saros.repackaged.picocontainer.annotations.Inject;
import saros.session.ISarosSession;
import saros.session.ISarosSessionManager;
import saros.ui.util.WizardUtils;
import saros.ui.util.selection.SelectionUtils;

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

  public SessionAddSelectedResourcesHandler() {
    SarosPluginContext.initComponent(this);
  }

  @Execute
  public Object execute(@Named(IServiceConstants.ACTIVE_SELECTION) @Optional ISelection selection) {

    List<IResource> selectedResources =
        SelectionUtils.getAdaptableObjects(selection, IResource.class);

    WizardUtils.openAddResourcesToSessionWizard(new HashSet<>(selectedResources));

    return null;
  }

  @CanExecute
  public boolean canExecute(
      @Named(IServiceConstants.ACTIVE_SELECTION) @Optional ISelection selection) {
    List<IResource> resources = SelectionUtils.getAdaptableObjects(selection, IResource.class);
    if (!(resources.size() > 0)) {
      return false;
    }
    final ISarosSession session = sessionManager.getSession();
    if (!(connectionHandler.isConnected() && session != null && session.hasWriteAccess())) {
      return false;
    }
    Set<IReferencePoint> sharedReferencePoints = session.getReferencePoints();
    for (IResource resource : resources) {
      saros.filesystem.IResource wrappedResource =
          ResourceConverter.convertToResource(sharedReferencePoints, resource);
      if (session.isShared(wrappedResource)) {
        return false;
      }
    }
    return true;
  }
}
