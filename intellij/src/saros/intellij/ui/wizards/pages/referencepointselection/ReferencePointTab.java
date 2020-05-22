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
import org.jetbrains.annotations.NotNull;
import saros.intellij.editor.ProjectAPI;
import saros.intellij.runtime.FilesystemRunner;
import saros.intellij.ui.Messages;
import saros.intellij.ui.wizards.pages.referencepointselection.SelectLocalReferencePointRepresentationPage.ModuleTabStateListener;

/**
 * Panel to specify how a shared module is represented locally. The available options are to create
 * a new module or to use an already existing module. The options specified by the user can be
 * requested from the module tab through {@link #getModuleSelectionResult()}.
 */
// TODO adjust module references in javadoc, variable and method names, etc.
class ReferencePointTab {

  private final String moduleName;
  private final ModuleTabStateListener moduleTabStateListener;

  private final JPanel moduleTabPanel;

  private final JComboBox<Project> projectComboBox;
  private final JRadioButton createNewModuleRadioButton;
  private final JTextField newModuleNameTextField;
  private final TextFieldWithBrowseButton newModuleBasePathTextField;
  private final JRadioButton useExistingModuleRadioButton;
  private final TextFieldWithBrowseButton existingDirectoryPathTextField;

  private boolean moduleNameTextFieldShownAsValid;
  private final Border moduleNameTextFieldDefaultBorder;
  private final Border moduleNameTextFieldErrorBorder;

  private boolean moduleBasePathTextFieldShownAsValid;
  private final Border moduleBasePathTextFieldDefaultBorder;
  private final Border moduleBasePathTextFieldErrorBorder;

  private boolean existingDirectoryPathTextFieldShownAsValid;
  private final Border existingDirectoryPathTextFieldDefaultBorder;
  private final Border existingDirectoryPathTextFieldErrorBorder;

  private boolean hasValidInput;

  /**
   * Creates a panel to specify how a shared module is represented locally.
   *
   * @param moduleName the name of the shared module contained in the project negotiation data
   */
  ReferencePointTab(
      @NotNull String moduleName, @NotNull ModuleTabStateListener moduleTabStateListener) {
    this.moduleName = moduleName;
    this.moduleTabStateListener = moduleTabStateListener;

    this.moduleTabPanel = new JPanel();
    this.projectComboBox = new ComboBox<>();
    this.createNewModuleRadioButton = new JBRadioButton();
    this.useExistingModuleRadioButton = new JBRadioButton();

    this.newModuleNameTextField = new JBTextField();
    this.newModuleBasePathTextField = new TextFieldWithBrowseButton();
    this.existingDirectoryPathTextField = new TextFieldWithBrowseButton();

    this.moduleNameTextFieldShownAsValid = true;
    this.moduleNameTextFieldDefaultBorder = newModuleNameTextField.getBorder();
    this.moduleNameTextFieldErrorBorder =
        BorderFactory.createCompoundBorder(
            moduleNameTextFieldDefaultBorder, BorderFactory.createLineBorder(JBColor.RED));

    this.moduleBasePathTextFieldShownAsValid = true;
    this.moduleBasePathTextFieldDefaultBorder =
        newModuleBasePathTextField.getTextField().getBorder();
    this.moduleBasePathTextFieldErrorBorder =
        BorderFactory.createCompoundBorder(
            moduleBasePathTextFieldDefaultBorder, BorderFactory.createLineBorder(JBColor.RED));

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
    addCreateNewModuleFieldListeners();
    addUseExistingModuleFieldListeners();
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
   * Sets up the radio buttons used to choose whether to create a new module or use and existing
   * module for the project negotiation.
   */
  private void setUpRadioButtons() {
    final String CREATE_NEW_MODULE_ACTION_COMMAND = "create new";
    final String USE_EXISTING_MODULE_ACTION_COMMAND = "use existing";

    createNewModuleRadioButton.setActionCommand(CREATE_NEW_MODULE_ACTION_COMMAND);
    useExistingModuleRadioButton.setActionCommand(USE_EXISTING_MODULE_ACTION_COMMAND);

    ButtonGroup buttonGroup = new ButtonGroup();
    buttonGroup.add(createNewModuleRadioButton);
    buttonGroup.add(useExistingModuleRadioButton);

    ActionListener radioButtonActionListener =
        actionEvent -> {
          switch (actionEvent.getActionCommand()) {
            case CREATE_NEW_MODULE_ACTION_COMMAND:
              setCreateNewModuleFieldsEnabled(true);
              setUseExistingDirectoryFieldsEnabled(false);
              break;

            case USE_EXISTING_MODULE_ACTION_COMMAND:
              setCreateNewModuleFieldsEnabled(false);
              setUseExistingDirectoryFieldsEnabled(true);
              break;

            default:
              throw new IllegalStateException("Encountered unknown radio button selection.");
          }

          updateNewModuleNameValidityIndicator();
          updateNewBasePathValidityIndicator();
          updateExistingModuleValidityIndicator();

          updateInputValidity();
        };

    createNewModuleRadioButton.addActionListener(radioButtonActionListener);
    useExistingModuleRadioButton.addActionListener(radioButtonActionListener);
  }

  /**
   * Enables or disables all fields belonging to the option to create a new module as part of the
   * project negotiation.
   *
   * @param enabled whether ot not the fields should be set to enabled
   */
  private void setCreateNewModuleFieldsEnabled(boolean enabled) {
    newModuleNameTextField.setEnabled(enabled);
    newModuleBasePathTextField.setEnabled(enabled);
  }

  /**
   * Enables or disables all fields belonging to the option to use an existing module as part of the
   * project negotiation.
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
    newModuleBasePathTextField.addBrowseFolderListener(
        Messages.ModuleTab_module_base_path_file_chooser_title,
        Messages.ModuleTab_module_base_path_file_chooser_description,
        null,
        FileChooserDescriptorFactory.createSingleFolderDescriptor());

    newModuleBasePathTextField.setEditable(false);
    newModuleBasePathTextField
        .getTextField()
        .addMouseListener(
            (MouseClickedListener)
                e -> {
                  // only act on left click and filter out events where browse button was clicked
                  if (e.getButton() == MouseEvent.BUTTON1 && e.getComponent().hasFocus()) {
                    newModuleBasePathTextField.getButton().doClick();
                  }
                });

    existingDirectoryPathTextField.addBrowseFolderListener(
        Messages.ModuleTab_existing_directory_path_file_chooser_title,
        Messages.ModuleTab_existing_directory_path_file_chooser_description,
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
   * Checks all open projects if the name of its base directory or the name of one of the content
   * roots of one of its modules matches the name of the shared reference point. If such a directory
   * is found, the corresponding project is chosen as the default selection. If multiple of such
   * projects exist, the first one found by the search is used. If no project is found, the first
   * one of the list is selected instead.
   *
   * <p>Calls {@link #setInitialInputForProject(Project)} with the selected project to determine the
   * default values and selection for the other fields.
   */
  private void setInitialInput() {
    int projectCount = projectComboBox.getItemCount();

    for (int i = 0; i < projectCount; i++) {
      Project project = projectComboBox.getItemAt(i);

      VirtualFile projectBaseDir = ProjectUtil.guessProjectDir(project);
      if (projectBaseDir != null && projectBaseDir.getName().equals(moduleName)) {
        setInitialInputForProject(project);

        return;
      }

      Module[] modules = ModuleManager.getInstance(project).getModules();
      for (Module module : modules) {
        for (VirtualFile contentRoot : ModuleRootManager.getInstance(module).getContentRoots()) {
          if (contentRoot.getName().equals(moduleName)) {
            setInitialInputForProject(project);

            return;
          }
        }
      }
    }

    if (projectCount > 0) {
      setInitialInputForProject(projectComboBox.getItemAt(0));
    }
  }

  /**
   * Selects the given project in the project combo-box, updates the other fields of the dialogs
   * accordingly and updates the validity state of the module tab.
   *
   * @param project the newly selected project
   * @see #updateFieldsForProjectChange(Project)
   * @see #updateInputValidity()
   */
  private void setInitialInputForProject(@NotNull Project project) {
    projectComboBox.setSelectedItem(project);
    updateFieldsForProjectChange(project);

    updateNewModuleNameValidityIndicator();
    updateNewBasePathValidityIndicator();
    updateExistingModuleValidityIndicator();

    updateInputValidity();
  }

  /**
   * Updates the contained fields for the given project. Sets the given reference point name as the
   * new directory name. Sets the base path of the chosen project as the base path for the new
   * directory.
   *
   * <p>Also sets the default option for the selected project. If the name of the project root
   * directory matches the name of the shared reference point or the project contains a module with
   * a content root that matches the given reference point name, the option to use it for the
   * project negotiation is selected by default. Otherwise, the option to create a new directory is
   * selected by default.
   *
   * @param project the newly selected project to set the default values and selection for
   */
  private void updateFieldsForProjectChange(@NotNull Project project) {
    newModuleNameTextField.setText(moduleName);

    VirtualFile projectBaseDir = ProjectUtil.guessProjectDir(project);
    if (projectBaseDir != null) {
      newModuleBasePathTextField.setText(projectBaseDir.getPath());

      if (projectBaseDir.getName().equals(moduleName)) {
        useExistingModuleRadioButton.doClick();

        existingDirectoryPathTextField.setText(projectBaseDir.getPath());

        return;
      }
    }

    Module[] modules = ModuleManager.getInstance(project).getModules();

    for (Module module : modules) {
      for (VirtualFile contentRoot : ModuleRootManager.getInstance(module).getContentRoots()) {
        if (contentRoot.getName().equals(moduleName)) {
          useExistingModuleRadioButton.doClick();

          existingDirectoryPathTextField.setText(contentRoot.getPath());

          return;
        }
      }
    }

    createNewModuleRadioButton.doClick();
  }

  /**
   * Registers a listener with the project combo box that sets default values for all other fields
   * when a new project is selected. After the new values are set, the validity state of the module
   * tab input is updated.
   *
   * @see #updateFieldsForProjectChange(Project)
   * @see #updateInputValidity()
   */
  private void addProjectComboBoxListener() {
    projectComboBox.addItemListener(
        itemEvent -> {
          if (itemEvent.getStateChange() == ItemEvent.SELECTED) {
            Project newSelectedProject = (Project) projectComboBox.getSelectedItem();

            if (newSelectedProject != null) {
              updateFieldsForProjectChange(newSelectedProject);
            }

            updateInputValidity();
          }
        });
  }

  /**
   * Adds listeners which update the module tab validity state on input changes to the fields used
   * when creating a new module as part of the project negotiation.
   */
  private void addCreateNewModuleFieldListeners() {
    DocumentListener moduleNameDocumentListener =
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
            updateNewModuleNameValidityIndicator();
            updateInputValidity();
          }
        };

    newModuleNameTextField.getDocument().addDocumentListener(moduleNameDocumentListener);

    DocumentListener moduleBasePathDocumentListener =
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
            updateNewBasePathValidityIndicator();
            updateInputValidity();
          }
        };

    newModuleBasePathTextField
        .getTextField()
        .getDocument()
        .addDocumentListener(moduleBasePathDocumentListener);
  }

  /**
   * Adds listeners which update the module tab validity state on input changes to the fields used
   * when using an existing directory as part of the project negotiation.
   */
  private void addUseExistingModuleFieldListeners() {
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
            updateExistingModuleValidityIndicator();
            updateInputValidity();
          }
        };

    existingDirectoryPathTextField
        .getTextField()
        .getDocument()
        .addDocumentListener(existingDirectoryPathDocumentListener);
  }

  /**
   * Updates the held flag of whether the current input of the module tab is valid.
   *
   * <p>This method should be called by all change listeners of the panel components.
   */
  private void updateInputValidity() {
    boolean newInputValidityState;

    if (projectComboBox.getSelectedItem() == null) {
      newInputValidityState = false;

    } else if (createNewModuleRadioButton.isSelected()) {
      newInputValidityState = hasValidNewModuleName() && hasValidNewBasePath();

    } else if (useExistingModuleRadioButton.isSelected()) {

      newInputValidityState = hasValidExistingModule();

    } else {
      newInputValidityState = false;
    }

    if (newInputValidityState != hasValidInput) {
      hasValidInput = newInputValidityState;

      moduleTabStateListener.moduleStateChanged();
    }
  }

  /**
   * Returns whether the entered module name is valid for the selected project.
   *
   * <p>A module name is seen as valid if it
   *
   * <ul>
   *   <li>is not empty,
   *   <li>is a valid path,
   *   <li>only contains a single path element, and
   *   <li>does not match any existing module name in the chosen project.
   * </ul>
   *
   * @return whether the entered module name is valid for the selected project
   * @see Paths#get(String, String...)
   */
  /*
   * TODO check for other separators? Intellij also sees '\' as a separator on unix but this is not
   *  detected by the Java Unix path implementation
   */
  private boolean hasValidNewModuleName() {
    String enteredName = newModuleNameTextField.getText();
    if (enteredName.isEmpty()) {
      return false;
    }

    try {
      Path path = Paths.get(enteredName);
      if (path.getNameCount() != 1) {
        return false;
      }
    } catch (InvalidPathException e) {
      return false;
    }

    // TODO check whether resource with same name already exists in chosen base directory
    return true;
  }

  /**
   * Returns whether the entered path points to a valid (existing) directory.
   *
   * @return whether the entered path points to a valid (existing) directory
   */
  private boolean hasValidNewBasePath() {
    File newBasePathFile = new File(newModuleBasePathTextField.getText());

    if (!newBasePathFile.exists() || !newBasePathFile.isDirectory()) {
      return false;
    }

    return representsValidVirtualFile(newBasePathFile);
  }

  /**
   * Returns whether a valid existing module is chosen.
   *
   * @return whether a valid existing module is chosen
   * @see Module#isDisposed()
   */
  private boolean hasValidExistingModule() {
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
   * Updates whether the field for the new module name is marked as invalid. The new state is based
   * on the returned value of {@link #hasValidNewModuleName()}.
   */
  private void updateNewModuleNameValidityIndicator() {
    boolean showFieldAsValid = hasValidNewModuleName() || !createNewModuleRadioButton.isSelected();

    if (showFieldAsValid == moduleNameTextFieldShownAsValid) {
      return;
    }

    moduleNameTextFieldShownAsValid = showFieldAsValid;

    Border border;
    String toolTip;

    if (showFieldAsValid) {
      border = moduleNameTextFieldDefaultBorder;
      toolTip = "";

    } else {
      border = moduleNameTextFieldErrorBorder;
      toolTip = Messages.ModuleTab_create_new_module_name_invalid_tooltip;
    }

    newModuleNameTextField.setBorder(border);
    newModuleNameTextField.setToolTipText(toolTip);
  }

  /**
   * Updates whether the field for the new module base path is marked as invalid. The new state is
   * based on the returned value of {@link #hasValidNewBasePath()}.
   */
  private void updateNewBasePathValidityIndicator() {
    boolean showFieldAsValid = hasValidNewBasePath() || !createNewModuleRadioButton.isSelected();

    if (showFieldAsValid == moduleBasePathTextFieldShownAsValid) {
      return;
    }

    moduleBasePathTextFieldShownAsValid = showFieldAsValid;

    Border border;
    String toolTip;

    if (showFieldAsValid) {
      border = moduleBasePathTextFieldDefaultBorder;
      toolTip = "";

    } else {
      border = moduleBasePathTextFieldErrorBorder;
      toolTip = Messages.ModuleTab_create_new_module_base_path_invalid_tooltip;
    }

    newModuleBasePathTextField.getTextField().setBorder(border);
    newModuleBasePathTextField.getTextField().setToolTipText(toolTip);
  }

  /**
   * Updates whether the field to select an existing module is marked as invalid. The new state is
   * based on the returned value of {@link #hasValidExistingModule()}.
   */
  private void updateExistingModuleValidityIndicator() {
    boolean showFieldAsValid =
        hasValidExistingModule() || !useExistingModuleRadioButton.isSelected();

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
      toolTip = Messages.ModuleTab_use_existing_module_local_module_invalid_tooltip;
    }

    existingDirectoryPathTextField.getTextField().setBorder(border);
    existingDirectoryPathTextField.getTextField().setToolTipText(toolTip);
  }

  /**
   * Returns the current input of the module tab.
   *
   * @return the current input of the module tab
   * @throws IllegalStateException if neither of the two radio buttons is selected or if there is no
   *     project selected
   * @see ReferencePointSelectionResult
   */
  @NotNull
  ReferencePointSelectionResult getModuleSelectionResult() {
    LocalRepresentationOption chosenLocalRepresentationOption;

    if (createNewModuleRadioButton.isSelected()) {
      chosenLocalRepresentationOption = LocalRepresentationOption.CREATE_NEW_DIRECTORY;
    } else if (useExistingModuleRadioButton.isSelected()) {
      chosenLocalRepresentationOption = LocalRepresentationOption.USE_EXISTING_DIRECTORY;
    } else {
      throw new IllegalStateException(
          "Encountered a state where neither of the two radio buttons was selected.");
    }

    Project project = (Project) projectComboBox.getSelectedItem();

    if (project == null) {
      throw new IllegalStateException("Encountered a state where no project was selected.");
    }

    String newModuleName = newModuleNameTextField.getText();
    VirtualFile newDirectoryBaseDirectory = getVirtualFile(newModuleBasePathTextField.getText());
    VirtualFile existingDirectory = getVirtualFile(existingDirectoryPathTextField.getText());

    return new ReferencePointSelectionResult(
        chosenLocalRepresentationOption,
        project,
        newModuleName,
        newDirectoryBaseDirectory,
        existingDirectory);
  }

  private VirtualFile getVirtualFile(String path) {
    File file = new File(path);

    return FilesystemRunner.runWriteAction(
        () -> LocalFileSystem.getInstance().findFileByIoFile(file),
        ModalityState.defaultModalityState());
  }

  /**
   * Returns the panel representing by the module tab.
   *
   * @return the panel representing by the module tab
   */
  @NotNull
  JPanel getPanel() {
    return moduleTabPanel;
  }

  /**
   * Returns the name of the shared module contained in the project negotiation data.
   *
   * @return the name of the shared module contained in the project negotiation data
   */
  @NotNull
  String getModuleName() {
    return moduleName;
  }

  /**
   * Returns whether the current input of the module tab is valid.
   *
   * @return whether the current input of the module tab is valid
   */
  boolean hasValidInput() {
    return hasValidInput;
  }

  /* Layout initialization */

  /** Creates and adds all necessary fields to the panel. */
  private void initPanel() {
    GridBagConstraints gbc = new GridBagConstraints();

    moduleTabPanel.setLayout(new GridBagLayout());
    gbc.gridy = 0;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.insets = new JBInsets(0, 8, 0, 8);
    gbc.anchor = GridBagConstraints.LINE_START;

    initProjectComboBox(gbc);
    initCreateNewModuleFields(gbc);
    initUseExistingModuleFields(gbc);
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
    projectLabel.setText(Messages.ModuleTab_project_label);

    moduleTabPanel.add(projectLabel, gbc);

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

    moduleTabPanel.add(projectComboBox, gbc);

    gbc.gridy++;

    moduleTabPanel.add(Box.createVerticalStrut(5), gbc);

    gbc.gridy++;
  }

  /**
   * Initializes the fields to create a new module as part of the project negotiation.
   *
   * @param gbc the basic grid bag constraints object used to define the layout
   */
  private void initCreateNewModuleFields(@NotNull GridBagConstraints gbc) {
    gbc.gridwidth = 3;

    createNewModuleRadioButton.setText(Messages.ModuleTab_create_new_module);

    moduleTabPanel.add(createNewModuleRadioButton, gbc);

    gbc.gridwidth = 1;
    gbc.gridy++;

    moduleTabPanel.add(Box.createHorizontalStrut(5), gbc);

    JLabel moduleNameLabel = new JBLabel();
    moduleNameLabel.setText(Messages.ModuleTab_create_new_module_name);

    moduleTabPanel.add(moduleNameLabel, gbc);

    moduleTabPanel.add(newModuleNameTextField, gbc);

    gbc.gridy++;

    moduleTabPanel.add(Box.createHorizontalStrut(5), gbc);

    JLabel moduleBasePathLabel = new JBLabel();
    moduleBasePathLabel.setText(Messages.ModuleTab_create_new_module_base_path);

    moduleTabPanel.add(moduleBasePathLabel, gbc);

    moduleTabPanel.add(newModuleBasePathTextField, gbc);

    gbc.gridy++;
  }

  /**
   * Initializes the fields to chose an existing module to use for the project negotiation.
   *
   * @param gbc the basic grid bag constraints object used to define the layout
   */
  private void initUseExistingModuleFields(@NotNull GridBagConstraints gbc) {
    gbc.gridwidth = 3;

    useExistingModuleRadioButton.setText(Messages.ModuleTab_use_existing_module);

    moduleTabPanel.add(useExistingModuleRadioButton, gbc);

    gbc.gridwidth = 1;
    gbc.gridy++;

    moduleTabPanel.add(Box.createHorizontalStrut(5), gbc);

    JLabel localModuleLabel = new JBLabel();
    localModuleLabel.setText(Messages.ModuleTab_use_existing_module_local_module);

    moduleTabPanel.add(localModuleLabel, gbc);

    moduleTabPanel.add(existingDirectoryPathTextField, gbc);

    gbc.gridy++;
  }

  /**
   * Initializes the spacers used to define the sizes of the columns of the layout.
   *
   * @param gbc the basic grid bag constraints object used to define the layout
   */
  private void initSpacers(@NotNull GridBagConstraints gbc) {
    gbc.gridwidth = 3;
    moduleTabPanel.add(Box.createHorizontalStrut(600), gbc);
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
