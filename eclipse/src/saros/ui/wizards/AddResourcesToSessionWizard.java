package saros.ui.wizards;

import java.util.Collection;
import java.util.List;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import saros.ui.ImageManager;
import saros.ui.Messages;
import saros.ui.util.CollaborationUtils;
import saros.ui.views.SarosView;
import saros.ui.wizards.pages.ResourceSelectionWizardPage;

/**
 * Wizard for adding resources to a running session.
 *
 * @author bkahlert
 */
public class AddResourcesToSessionWizard extends Wizard {
  public static final String TITLE = Messages.SessionAddProjectsWizard_title;
  public static final ImageDescriptor IMAGE = ImageManager.WIZBAN_SESSION_ADD_PROJECTS;

  private final ResourceSelectionWizardPage resourceSelectionWizardPage;

  /** @param preselectedResources resources that should be preselected or <code>null</code> */
  public AddResourcesToSessionWizard(final Collection<IResource> preselectedResources) {
    setWindowTitle(TITLE);
    setDefaultPageImageDescriptor(IMAGE);
    setHelpAvailable(false);
    resourceSelectionWizardPage = new ResourceSelectionWizardPage(preselectedResources);
  }

  @Override
  public IWizardPage getNextPage(IWizardPage page) {
    SarosView.clearNotifications();
    return super.getNextPage(page);
  }

  @Override
  public void addPages() {
    addPage(resourceSelectionWizardPage);
  }

  @Override
  public boolean performFinish() {
    List<IResource> selectedResources = resourceSelectionWizardPage.getSelectedResources();

    if (selectedResources == null || selectedResources.isEmpty()) return false;

    resourceSelectionWizardPage.rememberCurrentSelection();

    SarosView.clearNotifications();

    CollaborationUtils.addResourcesToSession(selectedResources);

    return true;
  }
}
