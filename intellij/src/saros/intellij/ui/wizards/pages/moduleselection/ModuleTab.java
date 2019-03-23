package saros.intellij.ui.wizards.pages.moduleselection;

import javax.swing.JPanel;
import org.jetbrains.annotations.NotNull;
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

    this.hasValidInput = false;

    fillPanel();
  }

  /** Creates and adds all necessary fields to the panel and sets up the necessary UI logic. */
  private void fillPanel() {
    // TODO create UI components and logic
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
}
