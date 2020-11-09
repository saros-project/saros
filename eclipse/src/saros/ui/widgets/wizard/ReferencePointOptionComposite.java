package saros.ui.widgets.wizard;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;
import saros.ui.ImageManager;
import saros.ui.Messages;
import saros.ui.widgets.wizard.events.ReferencePointOptionListener;

/**
 * Composite allowing the user to choose how to represent a shared reference point in the local
 * workspace.
 */
// TODO fix scaling; with addition of new row, windows is to short vertically
public class ReferencePointOptionComposite extends Composite {

  /** The possible ways to represent the shared reference point that can be chosen by the user. */
  public enum LocalRepresentationOption {
    NEW_PROJECT,
    NEW_DIRECTORY,
    EXISTING_DIRECTORY
  }

  private static final Image BROWSE_BUTTON_ICON =
      ImageManager.getImage("icons/obj16/fldr_obj.png"); // $NON-NLS-1$

  private final List<ReferencePointOptionListener> listeners = new ArrayList<>();

  private final String remoteReferencePointId;

  /*
   * Fields for the option to create a new project to represent the reference point.
   */
  private Button newProjectRadioButton;
  private Text newProjectNameText;

  /*
   * Fields for the option to create a new directory to represent the reference point.
   */
  private Button newDirectoryRadioButton;
  private Text newDirectoryNameText;
  private Text newDirectoryBasePathText;
  private Button newDirectoryBasePathBrowseButton;

  /*
   * Fields for the option to use an existing directory to represent the reference point.
   */
  private Button existingDirectoryRadioButton;
  private Text existingDirectoryPathText;
  private Button existingDirectoryBrowseButton;

  /**
   * Instantiates a new reference point option composite. Selects the option to create a new project
   * by default.
   *
   * @param parent the parent composite
   * @param remoteReferencePointId the ID of the reference point for which this composite chooses a
   *     local representation
   */
  public ReferencePointOptionComposite(
      final Composite parent, final String remoteReferencePointId) {
    super(parent, SWT.BORDER);

    this.remoteReferencePointId = remoteReferencePointId;

    GridLayout layout = new GridLayout();
    layout.numColumns = 3;
    super.setLayout(layout);

    createNewProjectGroup();
    createNewDirectoryGroup();
    createExistingDirectoryGroup();

    addDisposeListener(e -> listeners.clear());

    setRadioButtonSelection(LocalRepresentationOption.NEW_PROJECT);
  }

  @Override
  public void setLayout(Layout layout) {
    // NOP
  }

  /**
   * Adds a {@link ReferencePointOptionListener} to this composite.
   *
   * @param listener the listener to add
   */
  public void addReferencePointOptionListener(ReferencePointOptionListener listener) {
    listeners.add(listener);
  }

  /**
   * Returns the remote reference point ID that is associated with the reference point represented
   * by this composite.
   *
   * @return the remote reference point ID
   */
  public String getRemoteReferencePointId() {
    return remoteReferencePointId;
  }

  /**
   * Returns the option on how to represent the shared reference point selected by the user. The
   * selected option determines which content requests return valid values.
   *
   * @return the option on how to represent the shared reference point selected by the user
   * @see #getResult()
   */
  private LocalRepresentationOption getSelectedOption() {
    if (newProjectRadioButton.getSelection()) {
      return LocalRepresentationOption.NEW_PROJECT;
    }

    if (newDirectoryRadioButton.getSelection()) {
      return LocalRepresentationOption.NEW_DIRECTORY;
    }

    if (existingDirectoryRadioButton.getSelection()) {
      return LocalRepresentationOption.EXISTING_DIRECTORY;
    }

    throw new IllegalStateException("Encountered unknown selection state");
  }

  /**
   * Returns a {@link ReferencePointOptionResult} representing the state of the reference point
   * option composite.
   *
   * @return a {@link ReferencePointOptionResult} representing the state of the reference point
   *     option composite
   */
  public ReferencePointOptionResult getResult() {
    LocalRepresentationOption localRepresentationOption = getSelectedOption();

    String newProjectName = null;
    String newDirectoryName = null;
    String newDirectoryBase = null;
    String existingDirectory = null;

    switch (localRepresentationOption) {
      case NEW_PROJECT:
        newProjectName = newProjectNameText.getText();

        break;

      case NEW_DIRECTORY:
        newDirectoryName = newDirectoryNameText.getText();
        newDirectoryBase = newDirectoryBasePathText.getText();

        break;

      case EXISTING_DIRECTORY:
        existingDirectory = existingDirectoryPathText.getText();

        break;

      default:
        throw new IllegalStateException(
            "Encountered unknown local representation option " + localRepresentationOption);
    }

    return new ReferencePointOptionResult(
        localRepresentationOption,
        newProjectName,
        newDirectoryName,
        newDirectoryBase,
        existingDirectory);
  }

  /**
   * Sets the option to create a new project to represent the reference point as selected.
   *
   * <p>If a project name is given, it is set as the new value of the new project name field.
   *
   * @param projectName the value to set in the project name text field or <code>null</code>
   */
  public void setNewProjectOptionSelected(String projectName) {
    setRadioButtonSelection(LocalRepresentationOption.NEW_PROJECT);

    newProjectNameText.setFocus();

    if (projectName != null) {
      newProjectNameText.setText(projectName);
    }
  }

  /**
   * Sets the option to create a new directory to represent the reference point as selected.
   *
   * <p>If a directory name is given, it is set as the new value of the new directory name field. If
   * a directory base path is given, it is set as the new value of the new directory base path
   * field.
   *
   * @param directoryName the value to set in the directory name text field or <code>null</code>
   * @param directoryBasePath the value to set in the directory base path text field or <code>null
   *     </code>
   */
  public void setNewDirectoryOptionSelected(String directoryName, String directoryBasePath) {
    setRadioButtonSelection(LocalRepresentationOption.NEW_DIRECTORY);

    newDirectoryNameText.setFocus();

    if (directoryBasePath != null) {
      newDirectoryBasePathText.setText(directoryBasePath);
    }

    if (directoryName != null) {
      newDirectoryNameText.setText(directoryName);
    }
  }

  /**
   * Sets the option to use an existing directory to represent the reference point as selected.
   *
   * <p>If a directory path is given, it is set as the new value of the existing directory field.
   *
   * @param directoryPath the value to set in the directory path text field or <code>null</code>
   */
  public void setExistingDirectoryOptionSelected(String directoryPath) {
    setRadioButtonSelection(LocalRepresentationOption.EXISTING_DIRECTORY);

    existingDirectoryPathText.setFocus();

    if (directoryPath != null) {
      existingDirectoryPathText.setText(directoryPath);
    }
  }

  /** Create components for the option to create a new project to represent the reference point. */
  private void createNewProjectGroup() {
    GridData gridData;

    /* Radio button */
    newProjectRadioButton = new Button(this, SWT.RADIO);
    newProjectRadioButton.setText(Messages.ReferencePointOptionComposite_create_new_project);
    newProjectRadioButton.setToolTipText(
        Messages.ReferencePointOptionComposite_tooltip_new_project);
    newProjectRadioButton.setSelection(false);

    gridData = new GridData();
    gridData.horizontalSpan = 3;

    newProjectRadioButton.setLayoutData(gridData);
    newProjectRadioButton.addSelectionListener(
        new SelectionListener() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            updateEnablement(LocalRepresentationOption.NEW_PROJECT);
            newProjectNameText.setFocus();
            fireValueChanged();
            fireSelectedOptionChanged();
          }

          @Override
          public void widgetDefaultSelected(SelectionEvent e) {
            widgetSelected(e);
          }
        });

    /* Label */
    Label newProjectNameLabel = new Label(this, SWT.RIGHT);
    newProjectNameLabel.setText(Messages.ReferencePointOptionComposite_project_name);
    newProjectNameLabel.setToolTipText(
        Messages.ReferencePointOptionComposite_tooltip_new_project_name);
    newProjectNameLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));

    /* Text box */
    newProjectNameText = new Text(this, SWT.BORDER);
    newProjectNameText.setToolTipText(
        Messages.ReferencePointOptionComposite_tooltip_new_project_name);

    gridData = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
    gridData.horizontalSpan = 2;

    newProjectNameText.setLayoutData(gridData);
    newProjectNameText.addModifyListener(e -> fireValueChanged());
  }

  /**
   * Create components for the option to create a new directory to represent the reference point.
   */
  private void createNewDirectoryGroup() {
    GridData gridData;

    /* Radio button */
    newDirectoryRadioButton = new Button(this, SWT.RADIO);
    newDirectoryRadioButton.setText(Messages.ReferencePointOptionComposite_create_new_directory);
    newDirectoryRadioButton.setToolTipText(
        Messages.ReferencePointOptionComposite_tooltip_new_directory);
    newDirectoryRadioButton.setSelection(false);

    gridData = new GridData();
    gridData.horizontalSpan = 3;

    newDirectoryRadioButton.setLayoutData(gridData);
    newDirectoryRadioButton.addSelectionListener(
        new SelectionListener() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            updateEnablement(LocalRepresentationOption.NEW_DIRECTORY);
            newDirectoryNameText.setFocus();
            fireValueChanged();
            fireSelectedOptionChanged();
          }

          @Override
          public void widgetDefaultSelected(SelectionEvent e) {
            widgetSelected(e);
          }
        });

    /* Label */
    Label newDirectoryNameLabel = new Label(this, SWT.RIGHT);
    newDirectoryNameLabel.setText(Messages.ReferencePointOptionComposite_directory_name);
    newDirectoryNameLabel.setToolTipText(
        Messages.ReferencePointOptionComposite_tooltip_new_directory_name);
    newDirectoryNameLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));

    /* Text box */
    newDirectoryNameText = new Text(this, SWT.BORDER);
    newDirectoryNameText.setToolTipText(
        Messages.ReferencePointOptionComposite_tooltip_new_directory_name);

    gridData = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
    gridData.horizontalSpan = 2;

    newDirectoryNameText.setLayoutData(gridData);
    newDirectoryNameText.addModifyListener(e -> fireValueChanged());

    /* Label */
    Label newDirectoryBasePathLabel = new Label(this, SWT.RIGHT);
    newDirectoryBasePathLabel.setText(Messages.ReferencePointOptionComposite_directory_base_path);
    newDirectoryBasePathLabel.setToolTipText(
        Messages.ReferencePointOptionComposite_tooltip_new_directory_base_path);
    newDirectoryBasePathLabel.setLayoutData(
        new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));

    /* Text box */
    newDirectoryBasePathText = new Text(this, SWT.BORDER);
    newDirectoryBasePathText.setToolTipText(
        Messages.ReferencePointOptionComposite_tooltip_new_directory_base_path);

    gridData = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
    gridData.horizontalSpan = 1;

    newDirectoryBasePathText.setLayoutData(gridData);
    newDirectoryBasePathText.addModifyListener(e -> fireValueChanged());

    /* Button */
    newDirectoryBasePathBrowseButton = new Button(this, SWT.PUSH);
    newDirectoryBasePathBrowseButton.setToolTipText(
        Messages.ReferencePointOptionComposite_tooltip_browse_button);
    newDirectoryBasePathBrowseButton.setImage(BROWSE_BUTTON_ICON);
    newDirectoryBasePathBrowseButton.addSelectionListener(
        new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            String baseDirectoryPath =
                showBrowseDialog(Messages.ReferencePointOptionComposite_select_base_directory);

            if (baseDirectoryPath != null) {
              newDirectoryBasePathText.setText(baseDirectoryPath);
            }
          }
        });
  }

  /**
   * Create components for the option to use an existing directory to represent the reference point.
   */
  private void createExistingDirectoryGroup() {
    /* Radio Button */
    existingDirectoryRadioButton = new Button(this, SWT.RADIO);
    existingDirectoryRadioButton.setText(
        Messages.ReferencePointOptionComposite_use_existing_directory);
    existingDirectoryRadioButton.setToolTipText(
        Messages.ReferencePointOptionComposite_tooltip_existing_directory);
    existingDirectoryRadioButton.setSelection(false);

    GridData gridData = new GridData();
    gridData.horizontalSpan = 3;

    existingDirectoryRadioButton.setLayoutData(gridData);
    existingDirectoryRadioButton.addSelectionListener(
        new SelectionListener() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            updateEnablement(LocalRepresentationOption.EXISTING_DIRECTORY);
            existingDirectoryPathText.setFocus();
            fireValueChanged();
            fireSelectedOptionChanged();
          }

          @Override
          public void widgetDefaultSelected(SelectionEvent e) {
            widgetSelected(e);
          }
        });

    /* Label */
    Label existingDirectoryLabel = new Label(this, SWT.RIGHT);
    existingDirectoryLabel.setText(Messages.ReferencePointOptionComposite_existing_directory_path);
    existingDirectoryLabel.setToolTipText(
        Messages.ReferencePointOptionComposite_tooltip_existing_directory_path);
    existingDirectoryLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));

    /* Text box */
    existingDirectoryPathText = new Text(this, SWT.BORDER);
    existingDirectoryPathText.setToolTipText(
        Messages.ReferencePointOptionComposite_tooltip_existing_directory_path);

    existingDirectoryPathText.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
    existingDirectoryPathText.setEnabled(false);
    existingDirectoryPathText.addModifyListener(e -> fireValueChanged());

    /* Button */
    existingDirectoryBrowseButton = new Button(this, SWT.PUSH);
    existingDirectoryBrowseButton.setToolTipText(
        Messages.ReferencePointOptionComposite_tooltip_browse_button);
    existingDirectoryBrowseButton.setImage(BROWSE_BUTTON_ICON);
    existingDirectoryBrowseButton.addSelectionListener(
        new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            String existingDirectoryPath =
                showBrowseDialog(Messages.ReferencePointOptionComposite_select_existing_directory);

            if (existingDirectoryPath != null) {
              existingDirectoryPathText.setText(existingDirectoryPath);
            }
          }
        });
  }

  /**
   * Opens a browse dialog allowing the user to chose a container in the current workspace. Returns
   * the path of the container chosen by the user.
   *
   * @param title the title to display in the browse dialog
   * @return the path of the container chosen by the user or <code>null</code> if the user did not
   *     make a selection or canceled the dialog
   */
  private String showBrowseDialog(String title) {
    ContainerSelectionDialog dialog = new ContainerSelectionDialog(getShell(), null, false, title);

    dialog.open();

    Object[] result = dialog.getResult();

    if (result == null || result.length == 0) return null;

    // TODO drop leading delimiter?
    return ((Path) result[0]).toOSString();
  }

  /**
   * Selects the radio button of the given option and enables all of its fields. Deselects all other
   * radio buttons and disables all other fields.
   *
   * @param selectedOption the selected option
   * @see #setNewProjectFieldsEnabled(boolean)
   * @see #setNewDirectoryFieldsEnabled(boolean)
   * @see #setExistingDirectoryFieldsEnabled(boolean)
   */
  private void setRadioButtonSelection(LocalRepresentationOption selectedOption) {
    boolean newProjectOptionsSelected = false;
    boolean newDirectoryOptionsSelected = false;
    boolean existingDirectoryOptionsSelected = false;

    switch (selectedOption) {
      case NEW_PROJECT:
        newProjectOptionsSelected = true;
        break;

      case NEW_DIRECTORY:
        newDirectoryOptionsSelected = true;
        break;

      case EXISTING_DIRECTORY:
        existingDirectoryOptionsSelected = true;
        break;

      default:
        throw new IllegalStateException(
            "Encountered unsupported selection option " + selectedOption);
    }

    newProjectRadioButton.setSelection(newProjectOptionsSelected);
    newDirectoryRadioButton.setSelection(newDirectoryOptionsSelected);
    existingDirectoryRadioButton.setSelection(existingDirectoryOptionsSelected);

    setNewProjectFieldsEnabled(newProjectOptionsSelected);
    setNewDirectoryFieldsEnabled(newDirectoryOptionsSelected);
    setExistingDirectoryFieldsEnabled(existingDirectoryOptionsSelected);
  }

  /**
   * Enables or disables the widgets of this composite depending on the selected option.
   *
   * <p><b>NOTE:</b> This does not select the radio button for the given option. As a result, it
   * should only be used if the radio button is already selected. To also select the radio button,
   * use {@link #setRadioButtonSelection(LocalRepresentationOption)}.
   *
   * @param selectedOption the selected option
   * @see #setNewProjectFieldsEnabled(boolean)
   * @see #setNewDirectoryFieldsEnabled(boolean)
   * @see #setExistingDirectoryFieldsEnabled(boolean)
   */
  private void updateEnablement(LocalRepresentationOption selectedOption) {
    boolean newProjectOptionsEnabled = false;
    boolean newDirectoryOptionsEnabled = false;
    boolean existingDirectoryOptionsEnabled = false;

    switch (selectedOption) {
      case NEW_PROJECT:
        newProjectOptionsEnabled = true;
        break;

      case NEW_DIRECTORY:
        newDirectoryOptionsEnabled = true;
        break;

      case EXISTING_DIRECTORY:
        existingDirectoryOptionsEnabled = true;
        break;

      default:
        throw new IllegalStateException(
            "Encountered unsupported selection option " + selectedOption);
    }

    setNewProjectFieldsEnabled(newProjectOptionsEnabled);
    setNewDirectoryFieldsEnabled(newDirectoryOptionsEnabled);
    setExistingDirectoryFieldsEnabled(existingDirectoryOptionsEnabled);
  }

  /**
   * Sets the enabled state of all fields of the option to create a new project to represent the
   * reference point to the given value.
   *
   * @param enabled whether to enable the fields
   */
  private void setNewProjectFieldsEnabled(boolean enabled) {
    newProjectNameText.setEnabled(enabled);
  }

  /**
   * Sets the enabled state of all fields of the option to create a new directory to represent the
   * reference point to the given value.
   *
   * @param enabled whether to enable the fields
   */
  private void setNewDirectoryFieldsEnabled(boolean enabled) {
    newDirectoryNameText.setEnabled(enabled);
    newDirectoryBasePathText.setEnabled(enabled);
    newDirectoryBasePathBrowseButton.setEnabled(enabled);
  }

  /**
   * Sets the enabled state of all fields of the option to use an existing directory to represent
   * the reference point to the given value.
   *
   * @param enabled whether to enable the fields
   */
  private void setExistingDirectoryFieldsEnabled(boolean enabled) {
    existingDirectoryPathText.setEnabled(enabled);
    existingDirectoryBrowseButton.setEnabled(enabled);
  }

  private void fireValueChanged() {
    for (ReferencePointOptionListener listener : listeners) listener.valueChanged(this);
  }

  private void fireSelectedOptionChanged() {
    for (ReferencePointOptionListener listener : listeners) listener.selectedOptionChanged(this);
  }
}
