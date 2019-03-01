package saros.ui.widgets.viewer.project;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import saros.ui.ImageManager;
import saros.ui.Messages;
import saros.ui.util.DialogUtils;
import saros.ui.util.LayoutUtils;
import saros.ui.util.WizardUtils;
import saros.ui.views.SarosView;
import saros.ui.widgets.viewer.project.events.BaseResourceSelectionListener;
import saros.ui.widgets.viewer.project.events.FilterClosedProjectsChangedEvent;
import saros.ui.widgets.viewer.project.events.ResourceSelectionListener;

public class ResourceSelectionComposite extends BaseResourceSelectionComposite {
  protected boolean filterClosedProjects;
  protected Button filterClosedProjectsButton;

  protected ViewerFilter closedProjectsFilter =
      new ViewerFilter() {
        @Override
        public boolean select(Viewer viewer, Object parentElement, Object element) {
          if (element instanceof IFile || element instanceof IFolder) {
            return true;
          } else if (element instanceof IProject) {
            IProject project = (IProject) element;
            return project.isOpen();
          }
          return false;
        }
      };

  protected Button undoSelectionButton;
  protected Button redoSelectionButton;
  private Combo savedSelectionPresetsCombo;

  /**
   * Constructs a new {@link ResourceSelectionComposite}
   *
   * @param parent
   * @param style
   * @param filterClosedProjects true if initially closed projects should not be displayed
   */
  public ResourceSelectionComposite(Composite parent, int style, boolean filterClosedProjects) {
    super(parent, style);
    createControls();

    setFilterClosedProjects(filterClosedProjects);
  }

  /** Creates additional controls */
  protected void createControls() {
    Composite controlComposite = new Composite(this, SWT.NONE);
    controlComposite.setLayoutData(LayoutUtils.createFillHGrabGridData());
    controlComposite.setLayout(new GridLayout(7, false));

    /*
     * Save and reuse selections: [Undo] [Redo] [New Project] [dropdown]
     * [Load] [Save] [Remove]
     */

    Label savedSelectionsLabel = new Label(controlComposite, SWT.NONE);
    GridData sGD = new GridData(SWT.BEGINNING, SWT.FILL, true, false);
    sGD.horizontalSpan = 4;
    savedSelectionsLabel.setLayoutData(sGD);
    savedSelectionsLabel.setText("You can save and reuse selections:");

    undoSelectionButton = new Button(controlComposite, SWT.PUSH);
    undoSelectionButton.setLayoutData(new GridData(SWT.END, SWT.FILL, true, false));
    undoSelectionButton.setToolTipText(Messages.ResourceSelectionComposite_undo);
    undoSelectionButton.setEnabled(false);
    undoSelectionButton.setImage(
        PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_TOOL_UNDO));
    undoSelectionButton.addSelectionListener(
        new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            BusyIndicator.showWhile(
                getShell().getDisplay(),
                new Runnable() {
                  @Override
                  public void run() {
                    undoSelection();
                  }
                });
            undoSelectionButton.setEnabled(isUndoEnabled());
          }
        });
    redoSelectionButton = new Button(controlComposite, SWT.PUSH);
    redoSelectionButton.setToolTipText(Messages.ResourceSelectionComposite_redo);
    redoSelectionButton.setLayoutData(new GridData(SWT.END, SWT.FILL, false, false));
    redoSelectionButton.setEnabled(false);
    redoSelectionButton.setImage(
        PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_TOOL_REDO));
    redoSelectionButton.addSelectionListener(
        new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            BusyIndicator.showWhile(
                getShell().getDisplay(),
                new Runnable() {
                  @Override
                  public void run() {
                    redoSelection();
                  }
                });
            redoSelectionButton.setEnabled(isRedoEnabled());
          }
        });

    Button newProjectButton = new Button(controlComposite, SWT.PUSH);
    newProjectButton.setImage(ImageManager.ETOOL_NEW_PROJECT);
    newProjectButton.setLayoutData(new GridData(SWT.END, SWT.FILL, false, false));
    newProjectButton.setText(Messages.ResourceSelectionComposite_new_project);
    newProjectButton.addSelectionListener(
        new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            newProject();
          }
        });

    // --- new row starts here! ---

    /*
     * Restoring a saved selection.. Get a list of all names of saved
     * presets and update this whenever the user adds a new name
     */
    List<String> names = getSavedSelectionNames();
    savedSelectionPresetsCombo = new Combo(controlComposite, SWT.DROP_DOWN);
    GridData gd = new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
    gd.widthHint = 200;
    savedSelectionPresetsCombo.setLayoutData(gd);
    savedSelectionPresetsCombo.setItems(names.toArray(new String[names.size()]));
    savedSelectionPresetsCombo.addFocusListener(
        new FocusListener() {
          @Override
          public void focusLost(FocusEvent e) {
            // NOP
          }

          @Override
          public void focusGained(FocusEvent e) {
            /*
             * remove any notifications that were shown because the user
             * tried something without having selections, or a bad name,
             * etc.
             */
            SarosView.clearNotifications();
          }
        });
    /*
     * Auto-Select first item, which is useful if the user has just one
     * config saved. Hint: Even if no items saved, this does not explode:
     * read javadocs of combo.select()
     */
    savedSelectionPresetsCombo.select(0);
    // Not allowing very long names...
    savedSelectionPresetsCombo.setTextLimit(100);

    Button restoreSelection = new Button(controlComposite, SWT.PUSH);
    GridData gd2 = new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
    gd2.widthHint = 80;
    restoreSelection.setLayoutData(gd2);
    restoreSelection.addSelectionListener(
        new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            SarosView.clearNotifications();
            /*
             * Remember the current selection before the restoring of a
             * saved selection so the user can undo it just like a normal
             * selection!
             */
            rememberSelection();

            BusyIndicator.showWhile(
                Display.getDefault(),
                new Runnable() {
                  @Override
                  public void run() {
                    if (!restoreSelectionWithName(savedSelectionPresetsCombo.getText().trim())) {
                      /*
                       * could not restore the selection, because the name
                       * was not used for storing selections so far.
                       */

                      if (savedSelectionPresetsCombo.getItemCount() == 0) {
                        SarosView.showNotification(
                            "No stored selections yet",
                            "You do not have any stored selections yet. Make a selection in the tree above, enter a name here, and press 'Save' to save a selection for easy reusing",
                            savedSelectionPresetsCombo);
                      } else {
                        SarosView.showNotification(
                            "Error while restoring a selection",
                            "Could not restore a selection because there is none with the name you entered",
                            savedSelectionPresetsCombo);
                      }

                    } else {
                      SarosView.showNotification(
                          "Selection was restored",
                          "Your saved selection was successfully restored",
                          checkboxTreeViewer.getControl());
                    }
                  }
                });
          }
        });
    restoreSelection.setText("Restore");

    Button saveSelection = new Button(controlComposite, SWT.PUSH);
    GridData gd3 = new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
    gd3.widthHint = 60;
    saveSelection.setLayoutData(gd3);
    saveSelection.setText("Save");

    saveSelection.addSelectionListener(
        new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            SarosView.clearNotifications();
            final List<String> savedSelectionNames = getSavedSelectionNames();
            // save
            String theName = savedSelectionPresetsCombo.getText().trim();
            if (theName.isEmpty()) {
              SarosView.showNotification(
                  "Name missing",
                  "Please enter a name for the selection first to save it",
                  savedSelectionPresetsCombo);
              return;
            }

            if (savedSelectionNames.contains(theName)
                && !DialogUtils.openQuestionMessageDialog(
                    getShell(),
                    Messages.ResourceSelectionComposite_overwrite_dialog_title,
                    MessageFormat.format(
                        Messages.ResourceSelectionComposite_overwrite_dialog_message, theName))) {
              return;
            }

            saveSelectionWithName(theName);
            final List<String> selectionNames = getSavedSelectionNames();
            savedSelectionPresetsCombo.setItems(
                selectionNames.toArray(new String[selectionNames.size()]));
            savedSelectionPresetsCombo.setText(theName);
            SarosView.showNotification(
                "Selection was remembered",
                "The current selection was remembered with name '"
                    + theName
                    + "'. You can restore this selection from now on for a quick session startup, even with complicated partial sharing.",
                savedSelectionPresetsCombo);
          }
        });

    Button deleteSelection = new Button(controlComposite, SWT.PUSH);
    deleteSelection.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
    deleteSelection.setText("");
    deleteSelection.setImage(ImageManager.ELCL_DELETE);
    deleteSelection.setToolTipText("Remove this saved selection");

    deleteSelection.addSelectionListener(
        new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            SarosView.clearNotifications();
            /*
             * Remove the stored selection (if one with the given name
             * exists). Update the combobox.
             */
            String theName = savedSelectionPresetsCombo.getText(); // .trim()

            if (theName.length() == 0) {
              return;
            }

            if (!DialogUtils.openQuestionMessageDialog(
                getShell(),
                Messages.ResourceSelectionComposite_delete_dialog_title,
                MessageFormat.format(
                    Messages.ResourceSelectionComposite_delete_dialog_message, theName))) {
              return;
            }

            removeStoredSelection(theName);
            final List<String> savedSelectionNames = getSavedSelectionNames();
            savedSelectionPresetsCombo.setItems(
                savedSelectionNames.toArray(new String[savedSelectionNames.size()]));
            savedSelectionPresetsCombo.setText("");
            SarosView.showNotification(
                "Selection was removed",
                "The selection with name '" + theName + "' was removed",
                savedSelectionPresetsCombo);
          }
        });
  }

  /** This take care of enabling redo/undo controls when they are available */
  @Override
  public void updateRedoUndoControls() {
    redoSelectionButton.setEnabled(this.isRedoEnabled());
    undoSelectionButton.setEnabled(this.isUndoEnabled());
  }

  /**
   * Defines whether closed projects should be displayed or not
   *
   * @param filterClosedProjects true if closed projects should not be displayed
   */
  public void setFilterClosedProjects(boolean filterClosedProjects) {
    if (this.filterClosedProjects == filterClosedProjects) return;

    this.filterClosedProjects = filterClosedProjects;

    if (this.filterClosedProjectsButton != null
        && !this.filterClosedProjectsButton.isDisposed()
        && this.filterClosedProjectsButton.getSelection() != filterClosedProjects) {
      this.filterClosedProjectsButton.setSelection(filterClosedProjects);
    }

    if (filterClosedProjects) {
      getViewer().addFilter(closedProjectsFilter);
    } else {
      getViewer().removeFilter(closedProjectsFilter);
    }

    notifyProjectSelectionListener(filterClosedProjects);
  }

  /** Opens a wizard for {@link IProject} creation and sets the new project as the selected one. */
  protected void newProject() {
    IWorkspace workspace = ResourcesPlugin.getWorkspace();

    IProject[] projectsBefore = workspace.getRoot().getProjects();

    WizardUtils.openNewProjectWizard();

    IProject[] projectsAfter = workspace.getRoot().getProjects();

    IProject newProject = null;

    for (IProject project : projectsAfter) {
      if (!org.apache.commons.lang3.ArrayUtils.contains(projectsBefore, project)) {
        // Did not find the project, so it's the new one
        newProject = project;
        break;
      }
    }

    // If a project was successfully added: select it
    if (newProject != null) {
      getViewer().refresh();
      List<IResource> selectedResources = new ArrayList<IResource>();
      selectedResources.addAll(this.getSelectedResources());
      /*
       * HINT: do not directly use the return value of
       * getSelectedResources because that is an abstract collection which
       * does not implement add()!
       */
      selectedResources.add(newProject);

      checkboxTreeViewer.setCheckedElements(selectedResources.toArray());
      checkboxTreeViewer.setSubtreeChecked(newProject, true);
    }
  }

  /**
   * Notify all {@link ResourceSelectionListener}s about a changed {@link
   * ResourceSelectionComposite#filterClosedProjects} option.
   *
   * @param filterClosedProjects
   */
  public void notifyProjectSelectionListener(boolean filterClosedProjects) {
    FilterClosedProjectsChangedEvent event =
        new FilterClosedProjectsChangedEvent(filterClosedProjects);
    for (BaseResourceSelectionListener resourceSelectionListener :
        this.resourceSelectionListeners) {
      if (resourceSelectionListener instanceof ResourceSelectionListener)
        ((ResourceSelectionListener) resourceSelectionListener).filterClosedProjectsChanged(event);
    }
  }
}
