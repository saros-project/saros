package saros.intellij.ui.wizards.pages.referencepointselection;

import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.JBColor;
import com.intellij.ui.SimpleListCellRenderer;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBRadioButton;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.JBInsets;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import saros.intellij.editor.ProjectAPI;
import saros.intellij.runtime.FilesystemRunner;
import saros.intellij.ui.Messages;
import saros.intellij.ui.wizards.pages.referencepointselection.SelectLocalReferencePointRepresentationPage.ReferencePointTabStateListener;
import saros.util.PathUtils;

/**
 * Panel to specify how a shared reference point is represented locally. The available options are
 * to create a new directory or to use an already existing directory. The options specified by the
 * user can be requested from the tab using {@link #getReferencePointSelectionResult()}.
 */
class ReferencePointTab {

  private final String referencePointName;
  private final List<Pair<String, String>> possibleRepresentations;
  private final ReferencePointTabStateListener referencePointTabStateListener;

  private final JPanel referencePointTabPanel;

  private final JComboBox<Project> projectComboBox;
  private final JRadioButton createNewDirectoryRadioButton;
  private final JTextField newDirectoryNameTextField;
  private final TextFieldWithBrowseButton newDirectoryBasePathTextField;
  private final JRadioButton useExistingDirectoryRadioButton;
  private final TextFieldWithBrowseButton existingDirectoryPathTextField;

  private boolean newDirectoryNameTextFieldShownAsValid;
  private final Border newDirectoryNameTextFieldDefaultBorder;
  private final Border newDirectoryNameTextFieldErrorBorder;

  private boolean newDirectoryBasePathTextFieldShownAsValid;
  private final Border newDirectoryBasePathTextFieldDefaultBorder;
  private final Border newDirectoryBasePathTextFieldErrorBorder;

  private boolean existingDirectoryPathTextFieldShownAsValid;
  private final Border existingDirectoryPathTextFieldDefaultBorder;
  private final Border existingDirectoryPathTextFieldErrorBorder;

  private boolean hasValidInput;

  /**
   * Creates a panel to specify how a shared reference point is represented locally.
   *
   * @param referencePointName the name of the shared reference point contained in the resource
   *     negotiation data
   * @param possibleRepresentations the possible representation suggestions for the reference point
   * @param referencePointTabStateListener the reference point tab state listener to connect to
   */
  ReferencePointTab(
      @NotNull String referencePointName,
      @NotNull List<Pair<String, String>> possibleRepresentations,
      @NotNull ReferencePointTabStateListener referencePointTabStateListener) {

    this.referencePointName = referencePointName;
    this.possibleRepresentations = possibleRepresentations;
    this.referencePointTabStateListener = referencePointTabStateListener;

    this.referencePointTabPanel = new JPanel();
    this.projectComboBox = new ComboBox<>();
    this.createNewDirectoryRadioButton = new JBRadioButton();
    this.useExistingDirectoryRadioButton = new JBRadioButton();

    this.newDirectoryNameTextField = new JBTextField();
    this.newDirectoryBasePathTextField = new TextFieldWithBrowseButton();
    this.existingDirectoryPathTextField = new TextFieldWithBrowseButton();

    this.newDirectoryNameTextFieldShownAsValid = true;
    this.newDirectoryNameTextFieldDefaultBorder = newDirectoryNameTextField.getBorder();
    this.newDirectoryNameTextFieldErrorBorder =
        BorderFactory.createCompoundBorder(
            newDirectoryNameTextFieldDefaultBorder, BorderFactory.createLineBorder(JBColor.RED));

    this.newDirectoryBasePathTextFieldShownAsValid = true;
    this.newDirectoryBasePathTextFieldDefaultBorder =
        newDirectoryBasePathTextField.getTextField().getBorder();
    this.newDirectoryBasePathTextFieldErrorBorder =
        BorderFactory.createCompoundBorder(
            newDirectoryBasePathTextFieldDefaultBorder,
            BorderFactory.createLineBorder(JBColor.RED));

    this.existingDirectoryPathTextFieldShownAsValid = true;
    this.existingDirectoryPathTextFieldDefaultBorder =
        existingDirectoryPathTextField.getTextField().getBorder();
    this.existingDirectoryPathTextFieldErrorBorder =
        BorderFactory.createCompoundBorder(
            existingDirectoryPathTextFieldDefaultBorder,
            BorderFactory.createLineBorder(JBColor.RED));

    this.hasValidInput = false;

    initPanel();

    fillProjectComboBox();
    setUpRadioButtons();
    setUpFolderChooser();

    setInitialInput();

    addProjectComboBoxListener();
    addCreateNewDirectoryFieldListeners();
    addUseExistingDirectoryFieldListeners();
  }

  /**
   * Fills the project combo box with all open projects of the current Intellij application and
   * clears the default selection. The projects are sorted alphabetically.
   *
   * @see ProjectManager#getOpenProjects()
   */
  private void fillProjectComboBox() {
    Project[] projects = ProjectManager.getInstance().getOpenProjects();

    Arrays.sort(projects, Comparator.comparing(Project::getName));

    for (Project project : projects) {
      projectComboBox.addItem(project);
    }

    projectComboBox.setSelectedIndex(-1);
  }

  /**
   * Sets up the radio buttons used to choose whether to create a new directory or use and existing
   * directory for the resource negotiation.
   */
  private void setUpRadioButtons() {
    final String CREATE_NEW_DIRECTORY_ACTION_COMMAND = "create new";
    final String USE_EXISTING_DIRECTORY_ACTION_COMMAND = "use existing";

    createNewDirectoryRadioButton.setActionCommand(CREATE_NEW_DIRECTORY_ACTION_COMMAND);
    useExistingDirectoryRadioButton.setActionCommand(USE_EXISTING_DIRECTORY_ACTION_COMMAND);

    ButtonGroup buttonGroup = new ButtonGroup();
    buttonGroup.add(createNewDirectoryRadioButton);
    buttonGroup.add(useExistingDirectoryRadioButton);

    ActionListener radioButtonActionListener =
        actionEvent -> {
          switch (actionEvent.getActionCommand()) {
            case CREATE_NEW_DIRECTORY_ACTION_COMMAND:
              setCreateNewDirectoryFieldsEnabled(true);
              setUseExistingDirectoryFieldsEnabled(false);
              break;

            case USE_EXISTING_DIRECTORY_ACTION_COMMAND:
              setCreateNewDirectoryFieldsEnabled(false);
              setUseExistingDirectoryFieldsEnabled(true);
              break;

            default:
              throw new IllegalStateException("Encountered unknown radio button selection.");
          }

          updateNewDirectoryNameValidityIndicator();
          updateNewBaseDirectoryValidityIndicator();
          updateExistingDirectoryValidityIndicator();

          updateInputValidity();
        };

    createNewDirectoryRadioButton.addActionListener(radioButtonActionListener);
    useExistingDirectoryRadioButton.addActionListener(radioButtonActionListener);
  }

  /**
   * Enables or disables all fields belonging to the option to create a new directory as part of the
   * resource negotiation.
   *
   * @param enabled whether ot not the fields should be set to enabled
   */
  private void setCreateNewDirectoryFieldsEnabled(boolean enabled) {
    newDirectoryNameTextField.setEnabled(enabled);
    newDirectoryBasePathTextField.setEnabled(enabled);
  }

  /**
   * Enables or disables all fields belonging to the option to use an existing directory as part of
   * the resource negotiation.
   *
   * @param enabled whether ot not the fields should be set to enabled
   */
  private void setUseExistingDirectoryFieldsEnabled(boolean enabled) {
    existingDirectoryPathTextField.setEnabled(enabled);
  }

  /**
   * Sets up the directory choosers used as part of the dialog.
   *
   * <p>Adds a directory chooser to the displayed text fields. The current path entered in the text
   * field is used as the default selection when the folder chooser is opened by the user.
   *
   * <p>Sets the text fields to not be editable to avoid issues with {@link
   * #representsValidVirtualFile(File)}. Adds a mouse listener to ensure that the directory chooser
   * is opened instead when the text field is clicked.
   */
  private void setUpFolderChooser() {
    newDirectoryBasePathTextField.addBrowseFolderListener(
        Messages.ReferencePointTab_directory_base_path_file_chooser_title,
        Messages.ReferencePointTab_directory_base_path_file_chooser_description,
        null,
        FileChooserDescriptorFactory.createSingleFolderDescriptor());

    newDirectoryBasePathTextField.setEditable(false);
    newDirectoryBasePathTextField
        .getTextField()
        .addMouseListener(
            (MouseClickedListener)
                e -> {
                  // only act on left click and filter out events where browse button was clicked
                  if (e.getButton() == MouseEvent.BUTTON1 && e.getComponent().hasFocus()) {
                    newDirectoryBasePathTextField.getButton().doClick();
                  }
                });

    existingDirectoryPathTextField.addBrowseFolderListener(
        Messages.ReferencePointTab_existing_directory_path_file_chooser_title,
        Messages.ReferencePointTab_existing_directory_path_file_chooser_description,
        null,
        FileChooserDescriptorFactory.createSingleFolderDescriptor());

    existingDirectoryPathTextField.setEditable(false);
    existingDirectoryPathTextField
        .getTextField()
        .addMouseListener(
            (MouseClickedListener)
                e -> {
                  // only act on left click and filter out events where browse button was clicked
                  if (e.getButton() == MouseEvent.BUTTON1 && e.getComponent().hasFocus()) {
                    existingDirectoryPathTextField.getButton().doClick();
                  }
                });
  }

  /**
   * Checks all open projects if one of the suggested representations applies. If such a proposed
   * representation is found, the corresponding project is chosen as the default selection. If
   * multiple of such projects exist, the first one found by the search is used. If no such project
   * is found, the first one of the list is selected by default instead.
   *
   * <p>Calls {@link #setInitialInputForProject(Project, String)} with the selected project and the
   * proposed representation to determine the default values and selection for the other fields.
   *
   * @see #findMatchingProposedRepresentation(Project)
   */
  private void setInitialInput() {
    int projectCount = projectComboBox.getItemCount();

    for (int i = 0; i < projectCount; i++) {
      Project project = projectComboBox.getItemAt(i);

      String proposedRepresentation = findMatchingProposedRepresentation(project);

      if (proposedRepresentation != null) {
        setInitialInputForProject(project, proposedRepresentation);

        return;
      }
    }

    if (projectCount > 0) {
      setInitialInputForProject(projectComboBox.getItemAt(0), null);
    }
  }

  /**
   * Returns the path of a proposed representation. This proposal is generated by trying to apply
   * the possible representations contained in the resource negotiation data to the given project.
   *
   * @param project the project to check
   * @return the path of a proposed representation or <code>null</code> if no such valid proposal
   *     could be found
   */
  @Nullable
  private String findMatchingProposedRepresentation(Project project) {
    Map<String, Module> modules =
        Arrays.stream(ModuleManager.getInstance(project).getModules())
            .collect(Collectors.toMap(Module::getName, Function.identity()));

    for (Pair<String, String> possibleRepresentation : possibleRepresentations) {
      String moduleName = possibleRepresentation.getLeft();
      String contentRootRelativePath = possibleRepresentation.getRight();

      Module module = modules.get(moduleName);

      if (module == null) {
        continue;
      }

      VirtualFile[] contentRoots = ModuleRootManager.getInstance(module).getContentRoots();

      for (VirtualFile contentRoot : contentRoots) {
        if (contentRootRelativePath.isEmpty()) {
          if (contentRoot.getName().equals(referencePointName)) {
            return contentRoot.getPath();

          } else {
            continue;
          }
        }

        VirtualFile representation = contentRoot.findFileByRelativePath(contentRootRelativePath);

        if (representation != null) {
          return representation.getPath();
        }
      }
    }

    return null;
  }

  /**
   * Selects the given project in the project combo-box, updates the other fields of the dialogs
   * accordingly and updates the validity state of the reference point tab.
   *
   * @param project the newly selected project
   * @param proposedRepresentation the proposed local representation suggestion
   * @see #updateFieldsForProjectChange(Project, String)
   * @see #updateInputValidity()
   */
  private void setInitialInputForProject(
      @NotNull Project project, @Nullable String proposedRepresentation) {

    projectComboBox.setSelectedItem(project);
    updateFieldsForProjectChange(project, proposedRepresentation);

    updateNewDirectoryNameValidityIndicator();
    updateNewBaseDirectoryValidityIndicator();
    updateExistingDirectoryValidityIndicator();

    updateInputValidity();
  }

  /**
   * Updates the contained fields for the given project. Sets the given reference point name as the
   * new directory name. Sets the base path of the chosen project as the base directory for the new
   * directory.
   *
   * <p>Also sets the default option for the selected project. If a proposed local representation is
   * given, the option to use it for the resource negotiation is selected by default. Otherwise, the
   * option to create a new directory is selected by default.
   *
   * @param project the newly selected project to set the default values and selection for
   * @param proposedRepresentation the proposed local representation suggestion
   */
  private void updateFieldsForProjectChange(
      @NotNull Project project, @Nullable String proposedRepresentation) {

    newDirectoryNameTextField.setText(referencePointName);

    VirtualFile projectBaseDir = ProjectUtil.guessProjectDir(project);
    if (projectBaseDir != null) {
      newDirectoryBasePathTextField.setText(projectBaseDir.getPath());
    }

    if (proposedRepresentation != null) {
      useExistingDirectoryRadioButton.doClick();

      existingDirectoryPathTextField.setText(proposedRepresentation);

      return;
    }

    createNewDirectoryRadioButton.doClick();
  }

  /**
   * Registers a listener with the project combo box that sets default values for all other fields
   * when a new project is selected. After the new values are set, the validity state of the
   * reference point tab input is updated.
   *
   * @see #updateFieldsForProjectChange(Project, String)
   * @see #updateInputValidity()
   */
  private void addProjectComboBoxListener() {
    projectComboBox.addItemListener(
        itemEvent -> {
          if (itemEvent.getStateChange() == ItemEvent.SELECTED) {
            Project newSelectedProject = (Project) projectComboBox.getSelectedItem();

            String proposedRepresentation = findMatchingProposedRepresentation(newSelectedProject);

            if (newSelectedProject != null) {
              updateFieldsForProjectChange(newSelectedProject, proposedRepresentation);
            }

            updateInputValidity();
          }
        });
  }

  /**
   * Adds listeners which update the reference point tab validity state on input changes to the
   * fields used when creating a new directory as part of the resource negotiation.
   */
  private void addCreateNewDirectoryFieldListeners() {
    DocumentListener newDirectoryNameDocumentListener =
        new DocumentListener() {
          @Override
          public void insertUpdate(DocumentEvent e) {
            updateValidity();
          }

          @Override
          public void removeUpdate(DocumentEvent e) {
            updateValidity();
          }

          @Override
          public void changedUpdate(DocumentEvent e) {
            updateValidity();
          }

          private void updateValidity() {
            updateNewDirectoryNameValidityIndicator();
            updateInputValidity();
          }
        };
    newDirectoryNameTextField.getDocument().addDocumentListener(newDirectoryNameDocumentListener);

    DocumentListener newDirectoryBasePathDocumentListener =
        new DocumentListener() {
          @Override
          public void insertUpdate(DocumentEvent e) {
            updateValidity();
          }

          @Override
          public void removeUpdate(DocumentEvent e) {
            updateValidity();
          }

          @Override
          public void changedUpdate(DocumentEvent e) {
            updateValidity();
          }

          private void updateValidity() {
            updateNewBaseDirectoryValidityIndicator();
            updateInputValidity();
          }
        };
    newDirectoryBasePathTextField
        .getTextField()
        .getDocument()
        .addDocumentListener(newDirectoryBasePathDocumentListener);
  }

  /**
   * Adds listeners which update the reference point tab validity state on input changes to the
   * fields used when using an existing directory as part of the resource negotiation.
   */
  private void addUseExistingDirectoryFieldListeners() {
    DocumentListener existingDirectoryPathDocumentListener =
        new DocumentListener() {
          @Override
          public void insertUpdate(DocumentEvent e) {
            updateValidity();
          }

          @Override
          public void removeUpdate(DocumentEvent e) {
            updateValidity();
          }

          @Override
          public void changedUpdate(DocumentEvent e) {
            updateValidity();
          }

          private void updateValidity() {
            updateExistingDirectoryValidityIndicator();
            updateInputValidity();
          }
        };

    existingDirectoryPathTextField
        .getTextField()
        .getDocument()
        .addDocumentListener(existingDirectoryPathDocumentListener);
  }

  /**
   * Updates the held flag of whether the current input of the reference point tab is valid.
   *
   * <p>This method should be called by all change listeners of the panel components.
   */
  private void updateInputValidity() {
    boolean newInputValidityState;

    if (projectComboBox.getSelectedItem() == null) {
      newInputValidityState = false;

    } else if (createNewDirectoryRadioButton.isSelected()) {
      newInputValidityState = hasValidNewDirectoryName() && hasValidNewBasePath();

    } else if (useExistingDirectoryRadioButton.isSelected()) {

      newInputValidityState = hasValidExistingDirectory();

    } else {
      newInputValidityState = false;
    }

    if (newInputValidityState != hasValidInput) {
      hasValidInput = newInputValidityState;

      referencePointTabStateListener.validityStateChanged();
    }
  }

  /**
   * Returns whether the entered directory name is valid for the selected project.
   *
   * <p>A reference point name is seen as valid if it
   *
   * <ul>
   *   <li>is not empty,
   *   <li>is a valid path, and
   *   <li>only contains a single path element.
   * </ul>
   *
   * @return whether the entered directory name is valid for the selected project
   * @see Paths#get(String, String...)
   */
  /*
   * TODO check for other separators? Intellij also sees '\' as a separator on unix but this is not
   *  detected by the Java Unix path implementation
   */
  private boolean hasValidNewDirectoryName() {
    String enteredName = newDirectoryNameTextField.getText().trim();

    if (enteredName.isEmpty() || enteredName.contains(File.pathSeparator)) {
      return false;
    }

    try {
      Path enteredNamePath = Paths.get(enteredName);

      if (PathUtils.isEmpty(enteredNamePath)
          || enteredNamePath.getNameCount() != 1
          || enteredNamePath.isAbsolute()) {

        return false;
      }

      Path enteredBasePath = Paths.get(newDirectoryBasePathTextField.getText());

      return !enteredBasePath.resolve(enteredName).toFile().exists();

    } catch (InvalidPathException e) {
      return false;
    }
  }

  /**
   * Returns whether the entered path points to a valid (existing) directory that has a valid
   * matching reference point in the local VFS.
   *
   * @return whether the entered path points to a valid (existing) directory that has a valid
   *     matching reference point in the local VFS
   * @see #representsValidVirtualFile(File)
   */
  private boolean hasValidNewBasePath() {
    File newBasePathFile = new File(newDirectoryBasePathTextField.getText());

    if (!newBasePathFile.exists() || !newBasePathFile.isDirectory()) {
      return false;
    }

    return representsValidVirtualFile(newBasePathFile);
  }

  /**
   * Returns whether the entered path points to a valid (existing) directory that has a valid
   * matching reference point in the local VFS.
   *
   * @return whether the entered path points to a valid (existing) directory that has a valid
   *     matching reference point in the local VFS
   * @see #representsValidVirtualFile(File)
   */
  private boolean hasValidExistingDirectory() {
    File directoryFile = new File(existingDirectoryPathTextField.getText());

    if (!directoryFile.exists() || !directoryFile.isDirectory()) {
      return false;
    }

    return representsValidVirtualFile(directoryFile);
  }

  /**
   * Returns whether the given file represents a valid virtual file in the local VFS.
   *
   * <p>A valid virtual file
   *
   * <ul>
   *   <li>exists in the local VFS,
   *   <li>is part of the content of the selected project, and
   *   <li>is not excluded from the project content.
   * </ul>
   *
   * <b>NOTE:</b> As this is a long-running operation that queries the local VFS snapshot, it should
   * not be called too frequently.
   *
   * @param file the file to check
   * @return whether the given file represents a valid virtual file in the local VFS
   */
  private boolean representsValidVirtualFile(File file) {
    Project selectedProject = (Project) projectComboBox.getSelectedItem();

    if (selectedProject == null) {
      return false;
    }

    VirtualFile baseFile =
        FilesystemRunner.runWriteAction(
            () -> LocalFileSystem.getInstance().findFileByIoFile(file),
            ModalityState.defaultModalityState());

    return baseFile != null
        && ProjectAPI.isInProjectContent(selectedProject, baseFile)
        && !ProjectAPI.isExcluded(selectedProject, baseFile);
  }

  /**
   * Updates whether the field for the new directory name is marked as invalid. The new state is
   * based on the returned value of {@link #hasValidNewDirectoryName()}.
   */
  private void updateNewDirectoryNameValidityIndicator() {
    boolean showFieldAsValid =
        hasValidNewDirectoryName() || !createNewDirectoryRadioButton.isSelected();

    if (showFieldAsValid == newDirectoryNameTextFieldShownAsValid) {
      return;
    }

    newDirectoryNameTextFieldShownAsValid = showFieldAsValid;

    Border border;
    String toolTip;

    if (showFieldAsValid) {
      border = newDirectoryNameTextFieldDefaultBorder;
      toolTip = "";

    } else {
      border = newDirectoryNameTextFieldErrorBorder;
      toolTip = Messages.ReferencePointTab_create_new_directory_name_invalid_tooltip;
    }

    newDirectoryNameTextField.setBorder(border);
    newDirectoryNameTextField.setToolTipText(toolTip);
  }

  /**
   * Updates whether the field for the new directory base path is marked as invalid. The new state
   * is based on the returned value of {@link #hasValidNewBasePath()}.
   */
  private void updateNewBaseDirectoryValidityIndicator() {
    boolean showFieldAsValid = hasValidNewBasePath() || !createNewDirectoryRadioButton.isSelected();

    if (showFieldAsValid == newDirectoryBasePathTextFieldShownAsValid) {
      return;
    }

    newDirectoryBasePathTextFieldShownAsValid = showFieldAsValid;

    Border border;
    String toolTip;

    if (showFieldAsValid) {
      border = newDirectoryBasePathTextFieldDefaultBorder;
      toolTip = "";

    } else {
      border = newDirectoryBasePathTextFieldErrorBorder;
      toolTip = Messages.ReferencePointTab_create_new_directory_base_path_invalid_tooltip;
    }

    newDirectoryBasePathTextField.getTextField().setBorder(border);
    newDirectoryBasePathTextField.getTextField().setToolTipText(toolTip);
  }

  /**
   * Updates whether the field to select an existing directory is marked as invalid. The new state
   * is based on the returned value of {@link #hasValidExistingDirectory()}.
   */
  private void updateExistingDirectoryValidityIndicator() {
    boolean showFieldAsValid =
        hasValidExistingDirectory() || !useExistingDirectoryRadioButton.isSelected();

    if (showFieldAsValid == existingDirectoryPathTextFieldShownAsValid) {
      return;
    }

    existingDirectoryPathTextFieldShownAsValid = showFieldAsValid;

    Border border;
    String toolTip;

    if (showFieldAsValid) {
      border = existingDirectoryPathTextFieldDefaultBorder;
      toolTip = "";

    } else {
      border = existingDirectoryPathTextFieldErrorBorder;
      toolTip = Messages.ReferencePointTab_use_existing_directory_local_directory_invalid_tooltip;
    }

    existingDirectoryPathTextField.getTextField().setBorder(border);
    existingDirectoryPathTextField.getTextField().setToolTipText(toolTip);
  }

  /**
   * Returns the current input of the reference point tab.
   *
   * @return the current input of the reference point tab
   * @throws IllegalStateException if neither of the two radio buttons is selected or there is no
   *     project selected
   * @see ReferencePointSelectionResult
   */
  @NotNull
  ReferencePointSelectionResult getReferencePointSelectionResult() {
    LocalRepresentationOption chosenLocalRepresentationOption;

    if (createNewDirectoryRadioButton.isSelected()) {
      chosenLocalRepresentationOption = LocalRepresentationOption.CREATE_NEW_DIRECTORY;
    } else if (useExistingDirectoryRadioButton.isSelected()) {
      chosenLocalRepresentationOption = LocalRepresentationOption.USE_EXISTING_DIRECTORY;
    } else {
      throw new IllegalStateException(
          "Encountered a state where neither of the two radio buttons was selected.");
    }

    Project project = (Project) projectComboBox.getSelectedItem();

    if (project == null) {
      throw new IllegalStateException("Encountered a state where no project was selected.");
    }

    String newDirectoryName = newDirectoryNameTextField.getText();
    VirtualFile newDirectoryBaseDirectory = getVirtualFile(newDirectoryBasePathTextField.getText());
    VirtualFile existingDirectory = getVirtualFile(existingDirectoryPathTextField.getText());

    return new ReferencePointSelectionResult(
        chosenLocalRepresentationOption,
        project,
        newDirectoryName,
        newDirectoryBaseDirectory,
        existingDirectory);
  }

  /**
   * Returns the virtual file for the given path.
   *
   * @param path the path whose virtual file to get
   * @return the virtual file for the given path or <code>null</code> if no such virtual file could
   *     be found
   */
  @Nullable
  private VirtualFile getVirtualFile(String path) {
    File file = new File(path);

    return FilesystemRunner.runWriteAction(
        () -> LocalFileSystem.getInstance().findFileByIoFile(file),
        ModalityState.defaultModalityState());
  }

  /**
   * Returns the panel representing by the reference point tab.
   *
   * @return the panel representing by the reference point tab
   */
  @NotNull
  JPanel getPanel() {
    return referencePointTabPanel;
  }

  /**
   * Returns the name of the shared reference point contained in the resource negotiation data.
   *
   * @return the name of the shared reference point contained in the resource negotiation data
   */
  @NotNull
  String getReferencePointName() {
    return referencePointName;
  }

  /**
   * Returns whether the current input of the reference point tab is valid.
   *
   * @return whether the current input of the reference point tab is valid
   */
  boolean hasValidInput() {
    return hasValidInput;
  }

  /* Layout initialization */

  /** Creates and adds all necessary fields to the panel. */
  private void initPanel() {
    GridBagConstraints gbc = new GridBagConstraints();

    referencePointTabPanel.setLayout(new GridBagLayout());
    gbc.gridy = 0;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.insets = new JBInsets(0, 8, 0, 8);
    gbc.anchor = GridBagConstraints.LINE_START;

    initProjectComboBox(gbc);
    initCreateNewDirectoryFields(gbc);
    initUseExistingDirectoryFields(gbc);
    initSpacers(gbc);
  }

  /**
   * Initializes the project combo box.
   *
   * @param gbc the basic grid bag constraints object used to define the layout
   */
  private void initProjectComboBox(@NotNull GridBagConstraints gbc) {
    gbc.gridwidth = 2;

    JLabel projectLabel = new JBLabel();
    projectLabel.setText(Messages.ReferencePointTab_project_label);

    referencePointTabPanel.add(projectLabel, gbc);

    gbc.gridwidth = 1;

    projectComboBox.setRenderer(
        new SimpleListCellRenderer<Project>() {
          @Override
          public void customize(
              @NotNull JList list, Project value, int index, boolean selected, boolean hasFocus) {

            if (value != null) {
              setText(value.getName());
            }
          }
        });

    referencePointTabPanel.add(projectComboBox, gbc);

    gbc.gridy++;

    referencePointTabPanel.add(Box.createVerticalStrut(5), gbc);

    gbc.gridy++;
  }

  /**
   * Initializes the fields to create a new directory as part of the resource negotiation.
   *
   * @param gbc the basic grid bag constraints object used to define the layout
   */
  private void initCreateNewDirectoryFields(@NotNull GridBagConstraints gbc) {
    gbc.gridwidth = 3;

    createNewDirectoryRadioButton.setText(Messages.ReferencePointTab_create_new_directory);

    referencePointTabPanel.add(createNewDirectoryRadioButton, gbc);

    gbc.gridwidth = 1;
    gbc.gridy++;

    referencePointTabPanel.add(Box.createHorizontalStrut(5), gbc);

    JLabel directoryNameLabel = new JBLabel();
    directoryNameLabel.setText(Messages.ReferencePointTab_create_new_directory_name);

    referencePointTabPanel.add(directoryNameLabel, gbc);

    referencePointTabPanel.add(newDirectoryNameTextField, gbc);

    gbc.gridy++;

    referencePointTabPanel.add(Box.createHorizontalStrut(5), gbc);

    JLabel directoryBasePathLabel = new JBLabel();
    directoryBasePathLabel.setText(Messages.ReferencePointTab_create_new_directory_base_path);

    referencePointTabPanel.add(directoryBasePathLabel, gbc);

    referencePointTabPanel.add(newDirectoryBasePathTextField, gbc);

    gbc.gridy++;
  }

  /**
   * Initializes the fields to chose an existing directory to use for the resource negotiation.
   *
   * @param gbc the basic grid bag constraints object used to define the layout
   */
  private void initUseExistingDirectoryFields(@NotNull GridBagConstraints gbc) {
    gbc.gridwidth = 3;

    useExistingDirectoryRadioButton.setText(Messages.ReferencePointTab_use_existing_directory);

    referencePointTabPanel.add(useExistingDirectoryRadioButton, gbc);

    gbc.gridwidth = 1;
    gbc.gridy++;

    referencePointTabPanel.add(Box.createHorizontalStrut(5), gbc);

    JLabel localDirectoryLabel = new JBLabel();
    localDirectoryLabel.setText(Messages.ReferencePointTab_use_existing_directory_local_directory);

    referencePointTabPanel.add(localDirectoryLabel, gbc);

    referencePointTabPanel.add(existingDirectoryPathTextField, gbc);

    gbc.gridy++;
  }

  /**
   * Initializes the spacers used to define the sizes of the columns of the layout.
   *
   * @param gbc the basic grid bag constraints object used to define the layout
   */
  private void initSpacers(@NotNull GridBagConstraints gbc) {
    gbc.gridwidth = 3;
    referencePointTabPanel.add(Box.createHorizontalStrut(600), gbc);
  }

  /** Interface extension adding stubs for all methods besides {@link #mouseClicked(MouseEvent)}. */
  private interface MouseClickedListener extends MouseListener {
    @Override
    default void mousePressed(MouseEvent e) {
      // NOP
    }

    @Override
    default void mouseReleased(MouseEvent e) {
      // NOP
    }

    @Override
    default void mouseEntered(MouseEvent e) {
      // NOP
    }

    @Override
    default void mouseExited(MouseEvent e) {
      // NOP
    }
  }
}
