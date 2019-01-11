package de.fu_berlin.inf.dpp.ui.wizards;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.ui.ImageManager;
import de.fu_berlin.inf.dpp.ui.Messages;
import de.fu_berlin.inf.dpp.ui.util.CollaborationUtils;
import de.fu_berlin.inf.dpp.ui.views.SarosView;
import de.fu_berlin.inf.dpp.ui.wizards.pages.ContactSelectionWizardPage;
import de.fu_berlin.inf.dpp.ui.wizards.pages.ResourceSelectionWizardPage;
import java.util.Collection;
import java.util.List;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;

/**
 * Wizard for starting a session.
 *
 * <p>Starts sharing the selected resource(s) with the selected contacts(s) on finish.
 *
 * @author bkahlert
 * @author kheld
 */
public class StartSessionWizard extends Wizard {

  public static final String TITLE = Messages.SessionStartWizard_title;
  public static final ImageDescriptor IMAGE = ImageManager.WIZBAN_SESSION_OUTGOING;

  private final ResourceSelectionWizardPage resourceSelectionWizardPage;
  private final ContactSelectionWizardPage contactSelectionWizardPage;

  /** @param preselectedResources resources that should be preselected or <code>null</code> */
  public StartSessionWizard(final Collection<IResource> preselectedResources) {
    SarosPluginContext.initComponent(this);
    setWindowTitle(TITLE);
    setDefaultPageImageDescriptor(IMAGE);
    setNeedsProgressMonitor(true);
    setHelpAvailable(false);

    resourceSelectionWizardPage = new ResourceSelectionWizardPage(preselectedResources);

    contactSelectionWizardPage = new ContactSelectionWizardPage(true);
  }

  @Override
  public IWizardPage getNextPage(IWizardPage page) {
    /*
     * Remove any open notifications on page change in the wizard, in case
     * the user restored a selection in the ResourceSelectionComposite
     */
    SarosView.clearNotifications();
    return super.getNextPage(page);
  }

  @Override
  public void addPages() {
    addPage(resourceSelectionWizardPage);
    addPage(contactSelectionWizardPage);
  }

  /**
   * @JTourBusStop 2, Invitation Process:
   *
   * <p>The chosen resources are put into collections to be sent to the chosen contacts.
   */
  @Override
  public boolean performFinish() {

    List<IResource> selectedResources = resourceSelectionWizardPage.getSelectedResources();

    List<JID> selectedContacts = contactSelectionWizardPage.getSelectedContacts();

    if (selectedResources == null || selectedContacts == null) return false;

    if (selectedResources.isEmpty()) return false;

    resourceSelectionWizardPage.rememberCurrentSelection();

    SarosView.clearNotifications();

    CollaborationUtils.startSession(selectedResources, selectedContacts);

    return true;
  }
}
