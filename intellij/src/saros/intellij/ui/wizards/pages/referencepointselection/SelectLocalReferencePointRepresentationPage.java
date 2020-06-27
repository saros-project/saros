package saros.intellij.ui.wizards.pages.referencepointselection;

import com.intellij.ui.components.JBTabbedPane;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JTabbedPane;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import saros.intellij.ui.wizards.Wizard;
import saros.intellij.ui.wizards.pages.AbstractWizardPage;
import saros.intellij.ui.wizards.pages.PageActionListener;
import saros.negotiation.ResourceNegotiationData;
import saros.negotiation.additional_resource_data.AbstractPossibleRepresentationProvider;

/**
 * Wizard page to choose how the shared reference points are represented locally. For each shared
 * reference point, a {@link ReferencePointTab} is created.
 */
public class SelectLocalReferencePointRepresentationPage extends AbstractWizardPage {

  private final JTabbedPane tabbedBasePane;
  private final Map<String, ReferencePointTab> referencePointTabs;
  private final ReferencePointTabStateListener referencePointTabStateListener;

  public SelectLocalReferencePointRepresentationPage(
      String id,
      PageActionListener pageActionListener,
      List<ResourceNegotiationData> resourceNegotiationData) {

    super(id, pageActionListener);

    tabbedBasePane = new JBTabbedPane();
    referencePointTabs = new HashMap<>();
    referencePointTabStateListener = new ReferencePointTabStateListener();

    resourceNegotiationData.forEach(this::addReferencePointTab);

    add(tabbedBasePane);
  }

  /**
   * Creates a reference point tab for the given resource negotiation data and adds it to the tabbed
   * reference point view.
   *
   * @param resourceNegotiationData the resource negotiation data of the shared reference point
   */
  private void addReferencePointTab(@NotNull ResourceNegotiationData resourceNegotiationData) {
    String referencePointName = resourceNegotiationData.getReferencePointName();
    List<Pair<String, String>> possibleRepresentations =
        AbstractPossibleRepresentationProvider.getPossibleRepresentations(resourceNegotiationData);

    ReferencePointTab referencePointTab =
        new ReferencePointTab(
            referencePointName, possibleRepresentations, referencePointTabStateListener);

    referencePointTabs.put(referencePointTab.getReferencePointName(), referencePointTab);

    tabbedBasePane.addTab(referencePointTab.getReferencePointName(), referencePointTab.getPanel());
  }

  /**
   * Checks whether all contained reference point tabs have a valid input.
   *
   * <p>Enables the 'Next' button if the input of <i>all</i> tabs is valid, disables the button
   * otherwise.
   */
  private void checkReferencePointTabValidityState() {
    if (wizard == null) {
      return;
    }

    for (ReferencePointTab referencePointTab : referencePointTabs.values()) {
      boolean tabInputIsValid = referencePointTab.hasValidInput();

      if (!tabInputIsValid) {
        wizard.disableNextButton();
        return;
      }
    }

    wizard.enableNextButton();
  }

  /**
   * Returns the reference point selection results of the tab for the given reference point name.
   *
   * @param referencePointName the name of the shared reference point contained in the resource
   *     negotiation data
   * @return the reference point selection results of the tab for the given reference point name or
   *     <code>null</code> if there is no reference point tab for the given name
   */
  @Nullable
  public ReferencePointSelectionResult getReferencePointSelectionResult(
      @NotNull String referencePointName) {

    ReferencePointTab referencePointTab = referencePointTabs.get(referencePointName);

    if (referencePointTab == null) {
      return null;
    }

    return referencePointTab.getReferencePointSelectionResult();
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

    checkReferencePointTabValidityState();
  }

  /**
   * A listener for changes in the validity state of input contained in the given reference point
   * tab.
   *
   * <p>The method {@link #validityStateChanged()} is called by the registered reference point tab
   * when its input validity state changes.
   */
  class ReferencePointTabStateListener {
    void validityStateChanged() {
      checkReferencePointTabValidityState();
    }
  }
}
