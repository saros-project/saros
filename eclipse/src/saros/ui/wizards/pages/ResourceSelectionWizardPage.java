package saros.ui.wizards.pages;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.log4j.Logger;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import saros.preferences.EclipsePreferenceConstants;
import saros.ui.Messages;
import saros.ui.util.SWTUtils;
import saros.ui.views.SarosView;
import saros.ui.widgets.viewer.project.ResourceSelectionComposite;
import saros.ui.widgets.viewer.project.events.FilterClosedProjectsChangedEvent;
import saros.ui.widgets.viewer.project.events.ResourceSelectionChangedEvent;
import saros.ui.widgets.viewer.project.events.ResourceSelectionListener;

public class ResourceSelectionWizardPage extends WizardPage {
  private static final Logger LOG = Logger.getLogger(ResourceSelectionWizardPage.class);

  private ResourceSelectionComposite resourceSelectionComposite;

  private Collection<IResource> preselectedResources;

  /**
   * This {@link ResourceSelectionListener} changes the {@link WizardPage} 's state according to the
   * selected {@link IResource resources}.
   */
  private final ResourceSelectionListener resourceSelectionListener =
      new ResourceSelectionListener() {
        @Override
        public void resourceSelectionChanged(ResourceSelectionChangedEvent event) {
          if (resourceSelectionComposite == null || resourceSelectionComposite.isDisposed()) return;

          if (!resourceSelectionComposite.hasSelectedResources()) {
            setErrorMessage(Messages.ProjectSelectionWizardPage_selected_no_project);
            setPageComplete(false);
          } else {
            setErrorMessage(null);
            setPageComplete(true);
          }
        }

        @Override
        public void filterClosedProjectsChanged(FilterClosedProjectsChangedEvent event) {
          PlatformUI.getPreferenceStore()
              .setValue(
                  EclipsePreferenceConstants.PROJECTSELECTION_FILTERCLOSEDPROJECTS,
                  event.isFilterClosedProjects());
        }
      };

  /** @param preselectedResources resources that should be preselected or <code>null</code> */
  public ResourceSelectionWizardPage(final Collection<IResource> preselectedResources) {
    super(ResourceSelectionWizardPage.class.getName());
    setTitle(Messages.ProjectSelectionWizardPage_title);
    setDescription(Messages.ProjectSelectionWizardPage_description);
    this.preselectedResources = preselectedResources;
  }

  @Override
  public void createControl(Composite parent) {
    Composite composite = new Composite(parent, SWT.NONE);
    setControl(composite);

    composite.setLayout(new GridLayout(1, false));

    createResourceSelectionComposite(composite);

    resourceSelectionComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
  }

  /**
   * Create the composite and initialize it's selection asynchronously with the current selection of
   * the active "navigator"-type views in the current workspace perspective
   *
   * @param parent
   */
  private void createResourceSelectionComposite(Composite parent) {

    resourceSelectionComposite =
        new ResourceSelectionComposite(
            parent,
            SWT.BORDER | SWT.V_SCROLL,
            PlatformUI.getPreferenceStore()
                .getBoolean(EclipsePreferenceConstants.PROJECTSELECTION_FILTERCLOSEDPROJECTS));

    /*
     * Initialize the selection asynchronously, so the wizard opens
     * INSTANTLY instead of waiting up to XX seconds with flickering cursor
     * until the selection was applied.
     */
    final Runnable takeOverPerspectiveSelection =
        new Runnable() {
          @Override
          public void run() {

            final List<IResource> selectedResources = new ArrayList<IResource>();

            if (preselectedResources != null && preselectedResources.size() > 0) {

              selectedResources.addAll(preselectedResources);
              resourceSelectionComposite.setSelectedResources(selectedResources);
            }

            preselectedResources = null; // GC

            resourceSelectionComposite.addResourceSelectionListener(resourceSelectionListener);
            /*
             * If nothing is selected and only one project exists in the
             * workspace, select it in the Wizard.
             */
            if (selectedResources.size() == 0
                && resourceSelectionComposite.getProjectsCount() == 1) {

              selectedResources.addAll(resourceSelectionComposite.getResources());

              resourceSelectionComposite.setSelectedResources(selectedResources);
            }
            /*
             * Add the current automatically applied selection to the
             * undo-stack, so the user can undo it, and undo/redo works
             * properly.
             */
            resourceSelectionComposite.rememberSelection();
            setPageComplete(selectedResources.size() > 0);
          }
        };

    SWTUtils.runSafeSWTAsync(
        LOG,
        new Runnable() {

          @Override
          public void run() {

            final Shell shell = ResourceSelectionWizardPage.this.getShell();

            if (shell == null || shell.isDisposed()) return;

            BusyIndicator.showWhile(shell.getDisplay(), takeOverPerspectiveSelection);
          }
        });
  }

  @Override
  public void setVisible(boolean visible) {
    super.setVisible(visible);
    if (!visible) return;

    SarosView.clearNotifications();
    resourceSelectionComposite.setFocus();
  }

  /*
   * WizardPage Results
   */

  public List<IResource> getSelectedResources() {
    if (resourceSelectionComposite == null || resourceSelectionComposite.isDisposed()) return null;

    return resourceSelectionComposite.getSelectedResources();
  }

  /** Saves the current selection under a fixed name "-Last selection-" */
  public void rememberCurrentSelection() {
    resourceSelectionComposite.saveSelectionWithName("-Last selection-");
  }
}
