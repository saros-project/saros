package saros.ui.command_handlers;

import org.eclipse.core.resources.IResource;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import saros.SarosPluginContext;
import saros.communication.connection.ConnectionHandler;
import saros.repackaged.picocontainer.annotations.Inject;
import saros.session.ISarosSessionManager;
import saros.ui.menu_contributions.StartSessionWithProjects;
import saros.ui.util.WizardUtils;
import saros.ui.util.selection.retriever.SelectionRetrieverFactory;

/**
 * @JTourBusStop 1, Invitation Process:
 *
 * <p>Clicking on "Share Resource(s)..." brings execution here. This class is connected to the GUI
 * via Eclipse handlers. Right-click on the word ShareResourcesHandler below and click
 * References->Project. This will show all the other resources in the project which reference this
 * class.
 *
 * <p>As it happens, only one resource does this: plugin.xml, an important configuration file for
 * the project. Take a look at plugin.xml to see how it works.
 *
 * <p>(Also see {@link StartSessionWithProjects} for alternative invitation methods.)
 *
 * <p>This begins a process that includes:
 *
 * <p>- Establishing a new session.
 *
 * <p>- Asking which resources to share and with whom (if you chose to share via the Saros menu)
 *
 * <p>- Sending invitations to the chosen people.
 *
 * <p>- Determining which files need to be sent to the invitees depending on their response.
 *
 * <p>- Sending/receiving those files.
 *
 * <p>Notice that this is done via the {@link saros.ui.wizards.StartSessionWizard}.
 */
public class ShareResourcesHandler {

  @Inject private ConnectionHandler connectionHandler;
  @Inject private ISarosSessionManager sessionManager;

  public ShareResourcesHandler() {
    SarosPluginContext.initComponent(this);
  }

  @Execute
  public Object execute() {
    WizardUtils.openStartSessionWizard(
        SelectionRetrieverFactory.getSelectionRetriever(IResource.class).getSelection());
    return null;
  }

  @CanExecute
  public boolean canExecute() {
    return connectionHandler != null
        && connectionHandler.isConnected()
        && sessionManager != null
        && sessionManager.getSession() == null;
  }
}
