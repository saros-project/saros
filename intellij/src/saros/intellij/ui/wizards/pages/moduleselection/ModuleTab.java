package saros.intellij.ui.wizards.pages.moduleselection;

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.ListCellRendererWrapper;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBRadioButton;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.JBInsets;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.util.Arrays;
import java.util.Comparator;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import org.jetbrains.annotations.NotNull;
import saros.intellij.ui.Messages;
import saros.intellij.ui.wizards.pages.moduleselection.SelectLocalModuleRepresentationPage.ModuleTabStateListener;

/**
 * Panel to specify how a shared module is represented locally. The available options are to create
 * a new module or to use an already existing module. The options specified by the user can be
 * requested from the module tab through {@link #getModuleSelectionResult()}.
 */
class ModuleTab {

  private final String moduleName;
  private final ModuleTabStateListener moduleTabStateListener;

  private final JPanel moduleTabPanel;

  private final JComboBox<Project> projectComboBox;
  private final JRadioButton createNewModuleRadioButton;
  private final JTextField newModuleNameTextField;
  private final TextFieldWithBrowseButton newModuleBasePathTextField;
  private final JRadioButton useExistingModuleRadioButton;
  private final JComboBox<Module> existingModuleComboBox;

  private boolean hasValidInput;

  /**
   * Creates a panel to specify how a shared module is represented locally.
   *
   * @param moduleName the name of the shared module contained in the project negotiation data
   */
  ModuleTab(@NotNull String moduleName, @NotNull ModuleTabStateListener moduleTabStateListener) {
    this.moduleName = moduleName;
    this.moduleTabStateListener = moduleTabStateListener;

    this.moduleTabPanel = new JPanel();
    this.projectComboBox = new ComboBox<>();
    this.createNewModuleRadioButton = new JBRadioButton();
    this.newModuleNameTextField = new JBTextField();
    this.newModuleBasePathTextField = new TextFieldWithBrowseButton();
    this.useExistingModuleRadioButton = new JBRadioButton();
    this.existingModuleComboBox = new ComboBox<>();

    this.hasValidInput = false;

    initPanel();

    fillProjectComboBox();
    setUpRadioButtons();
    setUpFolderChooser();

    setInitialInput();

    addProjectComboBoxListener();

    /*
     * TODO set up logic to determine whether the current input is valid
     *  - add logic to updateInputValidity
     *  - call updateValidity whenever a field changes to inform the wizard page of potential state
     *    changes
     */
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
              setUseExistingModuleFieldsEnabled(false);
              break;

            case USE_EXISTING_MODULE_ACTION_COMMAND:
              setCreateNewModuleFieldsEnabled(false);
              setUseExistingModuleFieldsEnabled(true);
              break;

            default:
              throw new IllegalStateException("Encountered unknown radio button selection.");
          }

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
  private void setUseExistingModuleFieldsEnabled(boolean enabled) {
    existingModuleComboBox.setEnabled(enabled);
  }

  /**
   * Adds a directory chooser to the base path text field. The current path entered in the text
   * field is used as the default selection when the folder chooser is opened by the user.
   */
  private void setUpFolderChooser() {
    newModuleBasePathTextField.addBrowseFolderListener(
        Messages.ModuleTab_module_base_path_file_chooser_title,
        Messages.ModuleTab_module_base_path_file_chooser_description,
        null,
        FileChooserDescriptorFactory.createSingleFolderDescriptor());
  }

  /**
   * Checks all open projects if they contain a module with the given name. If such a module is
   * found, the project containing the module is chosen as the default selection. If multiple of
   * such projects exist, the first one found by the search is used. If not project is found, the
   * first one of the list is selected instead.
   *
   * <p>Calls {@link #setInitialInputForProject(Project)} with the selected project to determine the
   * default values and selection for the other fields.
   */
  private void setInitialInput() {
    int projectCount = projectComboBox.getItemCount();

    for (int i = 0; i < projectCount; i++) {
      Project project = projectComboBox.getItemAt(i);

      Module[] modules = ModuleManager.getInstance(project).getModules();

      for (Module module : modules) {
        if (module.getName().equals(moduleName)) {
          setInitialInputForProject(project);

          return;
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

    updateInputValidity();
  }

  /**
   * Updates the contained fields for the given project. Sets the given module name as the new
   * module name. Sets the base path of the chosen project as the base path for the new module. Adds
   * all modules of the project to the combo-box for existing modules.
   *
   * <p>Also sets the default selection for the selected project. If the project contains a module
   * with the given module name, the option to use it for the project negotiation is selected by
   * default. Otherwise, the option to create a new module is selected by default and the selection
   * in the existing module combo-box is cleared.
   *
   * @param project the newly selected project to set the default values and selection for
   */
  private void updateFieldsForProjectChange(@NotNull Project project) {
    newModuleNameTextField.setText(moduleName);

    VirtualFile projectBaseDir = ProjectUtil.guessProjectDir(project);
    if (projectBaseDir != null) {
      newModuleBasePathTextField.setText(projectBaseDir.getPath());
    }

    Module[] modules = ModuleManager.getInstance(project).getModules();
    Arrays.sort(modules, Comparator.comparing(Module::getName));

    existingModuleComboBox.removeAllItems();

    Module preselectedModule = null;

    for (Module module : modules) {
      existingModuleComboBox.addItem(module);

      if (module.getName().equals(moduleName)) {
        preselectedModule = module;
      }
    }

    if (preselectedModule != null) {
      useExistingModuleRadioButton.doClick();

      existingModuleComboBox.setSelectedItem(preselectedModule);

      return;
    }

    createNewModuleRadioButton.doClick();
    existingModuleComboBox.setSelectedIndex(-1);
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
   * Updates the held flag of whether the current input of the module tab is valid.
   *
   * <p>This method should be called by all change listeners of the panel components.
   */
  private void updateInputValidity() {
    // TODO check input validity and set correct value
    boolean newInputValidityState = false;

    if (newInputValidityState != hasValidInput) {
      hasValidInput = newInputValidityState;

      moduleTabStateListener.moduleStateChanged();
    }
  }

  /**
   * Returns the current input of the module tab.
   *
   * @return the current input of the module tab
   * @see ModuleSelectionResult
   */
  @NotNull
  ModuleSelectionResult getModuleSelectionResult() {
    // TODO create moduleSelectionResult from current state
    return null;
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

    /*
     * TODO replace with SimpleListCellRenderer once it is released and our the backwards
     *  compatibility allows it
     */
    projectComboBox.setRenderer(
        new ListCellRendererWrapper<Project>() {
          @Override
          public void customize(
              JList list, Project value, int index, boolean selected, boolean hasFocus) {

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

    /*
     * TODO replace with SimpleListCellRenderer once it is released and our the backwards
     *  compatibility allows it
     */
    existingModuleComboBox.setRenderer(
        new ListCellRendererWrapper<Module>() {
          @Override
          public void customize(
              JList list, Module value, int index, boolean selected, boolean hasFocus) {

            if (value != null) {
              setText(value.getName());
            }
          }
        });

    moduleTabPanel.add(existingModuleComboBox, gbc);

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
}
