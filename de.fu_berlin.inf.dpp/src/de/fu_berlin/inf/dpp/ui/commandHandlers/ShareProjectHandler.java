package de.fu_berlin.inf.dpp.ui.commandHandlers;

import de.fu_berlin.inf.dpp.ui.menuContributions.StartSessionWithContacts;
import de.fu_berlin.inf.dpp.ui.menuContributions.StartSessionWithProjects;
import de.fu_berlin.inf.dpp.ui.util.WizardUtils;
import de.fu_berlin.inf.dpp.ui.util.selection.retriever.SelectionRetrieverFactory;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;

/**
 * @JTourBusStop 1, Invitation Process:
 *
 * <p>Clicking on "Share project..." brings execution here. This class is connected to the GUI via
 * Eclipse handlers. Right-click on the word ShareProjectHandler below and click
 * References->Project. This will show all the other resources in the project which reference this
 * class.
 *
 * <p>As it happens, only one resource does this: plugin.xml, an important configuration file for
 * the project. Take a look at plugin.xml to see how it works.
 *
 * <p>(Also see {@link StartSessionWithProjects} and {@link StartSessionWithContacts} for
 * alternative invitation methods.)
 *
 * <p>This begins a process that includes:
 *
 * <p>- Establishing a new session.
 *
 * <p>- Asking which project to share and with whom (if you chose to share via the Saros menu)
 *
 * <p>- Sending invitations to the chosen people.
 *
 * <p>- Determining which files need to be sent to the invitees depending on their response.
 *
 * <p>- Sending/receiving those files.
 *
 * <p>Notice that this is done via the ShareProjectWizard.
 */
public class ShareProjectHandler extends AbstractHandler {

  @Override
  public Object execute(ExecutionEvent event) throws ExecutionException {
    WizardUtils.openStartSessionWizard(
        SelectionRetrieverFactory.getSelectionRetriever(IResource.class).getSelection());
    return null;
  }
}
