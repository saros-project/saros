package saros.intellij.ui.wizards.pages.referencepointselection;

import com.intellij.ui.components.JBTabbedPane;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.swing.JTabbedPane;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import saros.intellij.ui.wizards.Wizard;
import saros.intellij.ui.wizards.pages.AbstractWizardPage;
import saros.intellij.ui.wizards.pages.PageActionListener;

/**
 * Wizard page to choose how the shared modules are represented locally. For each shared module, a
 * {@link ReferencePointTab} is created.
 */
public class SelectLocalReferencePointRepresentationPage extends AbstractWizardPage {

  private final JTabbedPane tabbedBasePane;
  private final Map<String, ReferencePointTab> moduleTabs;
  private final ModuleTabStateListener moduleTabStateListener;

  public SelectLocalReferencePointRepresentationPage(
      String id, PageActionListener pageActionListener, Set<String> moduleNames) {
    super(id, pageActionListener);

    tabbedBasePane = new JBTabbedPane();
    moduleTabs = new HashMap<>();
    moduleTabStateListener = new ModuleTabStateListener();

    moduleNames.forEach(this::addModuleTab);

    add(tabbedBasePane);
  }

  /**
   * Creates a module tab for the given module name and adds it to the tabbed module view.
   *
   * @param moduleName the name of a shared module contained in the project negotiation data
   */
  private void addModuleTab(@NotNull String moduleName) {
    ReferencePointTab referencePointTab = new ReferencePointTab(moduleName, moduleTabStateListener);

    moduleTabs.put(referencePointTab.getModuleName(), referencePointTab);

    tabbedBasePane.addTab(referencePointTab.getModuleName(), referencePointTab.getPanel());
  }

  /**
   * Checks whether all contained module tabs have a valid input.
   *
   * <p>Enables the 'Next' button if the input of <i>all</i> module tabs is valid, disables the
   * button otherwise.
   */
  private void checkModuleTabValidityState() {
    if (wizard == null) {
      return;
    }

    for (ReferencePointTab referencePointTab : moduleTabs.values()) {
      boolean tabInputIsValid = referencePointTab.hasValidInput();

      if (!tabInputIsValid) {
        wizard.disableNextButton();
        return;
      }
    }

    wizard.enableNextButton();
  }

  /**
   * Returns the module selection results of the module tab for the given module name.
   *
   * @param moduleName the name of the shared module contained in the project negotiation data
   * @return the module selection results of the module tab for the given module name or <code>null
   *     </code> if there is no module tab for the given name
   */
  @Nullable
  public ReferencePointSelectionResult getModuleSelectionResult(@NotNull String moduleName) {
    ReferencePointTab referencePointTab = moduleTabs.get(moduleName);

    if (referencePointTab == null) {
      return null;
    }

    return referencePointTab.getModuleSelectionResult();
  }

  @Override
  public boolean isBackButtonEnabled() {
    return false;
  }

  @Override
  public boolean isNextButtonEnabled() {
    return true;
  }

  @Override
  public void setWizard(@NotNull Wizard wizard) {
    super.setWizard(wizard);

    checkModuleTabValidityState();
  }

  /**
   * A listener for changes in the validity state of input contained in the given module tab.
   *
   * <p>The method {@link #moduleStateChanged()} is called by the registered module tab when its
   * input validity state changes.
   */
  class ModuleTabStateListener {
    void moduleStateChanged() {
      checkModuleTabValidityState();
    }
  }
}
