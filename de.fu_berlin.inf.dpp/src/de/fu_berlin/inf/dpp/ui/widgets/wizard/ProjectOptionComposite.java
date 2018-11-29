package de.fu_berlin.inf.dpp.ui.widgets.wizard;

import de.fu_berlin.inf.dpp.ui.Messages;
import de.fu_berlin.inf.dpp.ui.widgets.wizard.events.ProjectNameChangedEvent;
import de.fu_berlin.inf.dpp.ui.widgets.wizard.events.ProjectOptionListener;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;

public class ProjectOptionComposite extends Composite {

  private final List<ProjectOptionListener> listeners = new ArrayList<ProjectOptionListener>();

  private final String remoteProjectID;

  private Button newProjectRadioButton;
  private Text newProjectNameText;

  private Button existingProjectRadioButton;
  private Text existingProjectNameText;
  private Button browseProjectsButton;

  public ProjectOptionComposite(final Composite parent, final String remoteProjectID) {

    super(parent, SWT.BORDER);

    this.remoteProjectID = remoteProjectID;

    GridLayout layout = new GridLayout();
    layout.numColumns = 3;
    super.setLayout(layout);

    createNewProjectGroup();
    createUpdateProjectGroup();

    addDisposeListener(
        new DisposeListener() {
          @Override
          public void widgetDisposed(DisposeEvent e) {
            listeners.clear();
          }
        });

    updateEnablement(newProjectRadioButton);
  }

  @Override
  public void setLayout(Layout layout) {
    // NOP
  }

  public void addProjectOptionListener(ProjectOptionListener listener) {
    listeners.add(listener);
  }

  public void removeProjectOptionListener(ProjectOptionListener listener) {
    listeners.remove(listener);
  }

  /**
   * Returns the remote project ID that is associated with the project represented by this
   * composite.
   *
   * @return the remote project ID
   */
  public String getRemoteProjectID() {
    return remoteProjectID;
  }

  /**
   * Returns whether the remote project should merged into an already existing one.
   *
   * @return <code>true</code> if the remote project should be merged, <code>false</code> otherwise
   */
  public boolean useExistingProject() {
    return existingProjectRadioButton.getSelection();
  }

  /**
   * Returns the currently selected local project name for the remote project.
   *
   * @return the currently selected local project name
   */
  public String getProjectName() {
    if (newProjectRadioButton.getSelection()) return newProjectNameText.getText();

    return existingProjectNameText.getText();
  }

  /**
   * Sets the local project name for the remote project
   *
   * @param useExistingProject <code>true</code>, if the local project should point to an existing
   *     local project, <code>false</code> otherwise
   * @param name the local name of the project
   */
  public void setProjectName(boolean useExistingProject, String name) {
    if (useExistingProject) {
      newProjectRadioButton.setSelection(false);
      existingProjectRadioButton.setSelection(true);

      existingProjectNameText.setText(name);
    } else {
      newProjectRadioButton.setSelection(true);
      existingProjectRadioButton.setSelection(false);

      newProjectNameText.setText(name);
    }
  }

  /** Create components of "Create new project" area of EnterProjectNamePage */
  private void createNewProjectGroup() {
    GridData gridData;

    /* Radio button */
    newProjectRadioButton = new Button(this, SWT.RADIO);
    newProjectRadioButton.setText(Messages.EnterProjectNamePage_create_new_project);
    newProjectRadioButton.setSelection(true);

    gridData = new GridData();
    gridData.horizontalSpan = 3;

    newProjectRadioButton.setLayoutData(gridData);
    newProjectRadioButton.addSelectionListener(
        new SelectionListener() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            updateEnablement(newProjectRadioButton);
          }

          @Override
          public void widgetDefaultSelected(SelectionEvent e) {
            updateEnablement(newProjectRadioButton);
          }
        });

    /* Label */
    Label newProjectNameLabel = new Label(this, SWT.RIGHT);
    newProjectNameLabel.setText(Messages.EnterProjectNamePage_project_name);
    newProjectNameLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));

    /* Text box */
    newProjectNameText = new Text(this, SWT.BORDER);

    gridData = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
    gridData.horizontalSpan = 2;

    newProjectNameText.setLayoutData(gridData);
    newProjectNameText.addModifyListener(
        new ModifyListener() {
          @Override
          public void modifyText(ModifyEvent e) {
            for (ProjectOptionListener listener : listeners) {
              listener.projectNameChanged(
                  new ProjectNameChangedEvent(getRemoteProjectID(), getProjectName()));
            }
          }
        });

    newProjectNameText.setFocus();
  }

  /** Create components of "Use existing project" area of EnterProjectNamePage */
  private void createUpdateProjectGroup() {
    /* Radio Button */
    existingProjectRadioButton = new Button(this, SWT.RADIO);
    existingProjectRadioButton.setText(Messages.EnterProjectNamePage_use_existing_project);
    existingProjectRadioButton.setSelection(false);

    GridData gridData = new GridData();
    gridData.horizontalSpan = 3;

    existingProjectRadioButton.setLayoutData(gridData);
    existingProjectRadioButton.addSelectionListener(
        new SelectionListener() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            updateEnablement(existingProjectRadioButton);
          }

          @Override
          public void widgetDefaultSelected(SelectionEvent e) {
            updateEnablement(existingProjectRadioButton);
          }
        });

    /* Label */
    Label updateProjectNameLabel = new Label(this, SWT.RIGHT);
    updateProjectNameLabel.setText(Messages.EnterProjectNamePage_project_name);
    updateProjectNameLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));

    /* Text box */
    existingProjectNameText = new Text(this, SWT.BORDER);
    existingProjectNameText.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
    existingProjectNameText.setEnabled(false);
    existingProjectNameText.addModifyListener(
        new ModifyListener() {
          @Override
          public void modifyText(ModifyEvent e) {
            for (ProjectOptionListener listener : listeners)
              listener.projectNameChanged(
                  new ProjectNameChangedEvent(getRemoteProjectID(), getProjectName()));
          }
        });

    /* Button */
    browseProjectsButton = new Button(this, SWT.PUSH);
    browseProjectsButton.setText(Messages.EnterProjectNamePage_browse);
    browseProjectsButton.addSelectionListener(
        new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            String projectName =
                getProjectDialog(Messages.EnterProjectNamePage_select_project_for_update);
            if (projectName != null) existingProjectNameText.setText(projectName);
          }
        });
  }

  private String getProjectDialog(String title) {
    ContainerSelectionDialog dialog = new ContainerSelectionDialog(getShell(), null, false, title);

    dialog.open();

    Object[] result = dialog.getResult();

    if (result == null || result.length == 0) return null;

    return ResourcesPlugin.getWorkspace()
        .getRoot()
        .findMember((Path) result[0])
        .getProject()
        .getName();
  }

  /**
   * Enables or disables the widgets of this composite depending on the selection of the radio
   * buttons. Then, all listeners are informed because the current project name has changed.
   */
  private void updateEnablement(Button button) {
    boolean updateSelected = (button == existingProjectRadioButton);

    newProjectNameText.setEnabled(!updateSelected);

    existingProjectNameText.setEnabled(updateSelected);
    browseProjectsButton.setEnabled(updateSelected);

    for (ProjectOptionListener listener : listeners) {
      listener.projectNameChanged(
          new ProjectNameChangedEvent(getRemoteProjectID(), getProjectName()));
    }
  }
}
