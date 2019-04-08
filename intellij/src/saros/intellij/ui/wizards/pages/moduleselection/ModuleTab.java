package saros.intellij.ui.wizards.pages.moduleselection;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.ListCellRendererWrapper;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBRadioButton;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.JBInsets;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.Box;
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

    /*
     * TODO set up default selection
     *  - fill project combo box
     *  - check if project module combination is found; select it if present
     */

    /*
     * TODO set up project combo box logic
     *  - add listener to update other fields + set new default selection for chosen project
     */

    /*
     * TODO set up radio-buttons
     *  - make radio-buttons exclusive (selecting one deselects other)
     *  - add logic to enable/disable fields according to chosen radio-button
     */

    /*
     * TODO set up new module fields logic
     *  - add file chooser and register it with the browse button
     */

    /*
     * TODO set up logic to determine whether the current input is valid
     *  - add logic to updateInputValidity
     *  - call updateValidity whenever a field changes to inform the wizard page of potential state
     *    changes
     */
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
