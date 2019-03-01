package de.fu_berlin.inf.dpp.intellij.ui.wizards.pages;

import de.fu_berlin.inf.dpp.intellij.ui.wizards.Wizard;
import javax.swing.JPanel;

/**
 * Abstract base class for wizard pages.
 *
 * <p>FIXME: Make layouts resizable
 */
public abstract class AbstractWizardPage extends JPanel {
  protected Wizard wizard;
  private String id;
  private PageActionListener pageListener;

  public AbstractWizardPage(String id) {
    super();
    this.id = id;
  }

  public AbstractWizardPage(String id, PageActionListener pageListener) {
    this(id);
    this.pageListener = pageListener;
  }

  public String getId() {
    return id;
  }

  /** Method called before hiding panel */
  public void aboutToHidePanel() {
    // Do nothing by default
  }

  /**
   * Title of the next button for this page (e.g. next or accept).
   *
   * @return by default, {@link NavigationPanel#TITLE_NEXT}.
   */
  public String getNextButtonTitle() {
    return NavigationPanel.TITLE_NEXT;
  }

  public void setWizard(Wizard wizard) {
    this.wizard = wizard;
  }

  /** Called when user clicks on the back button. Calls pageListener.back(). */
  public void actionBack() {
    if (pageListener != null) {
      pageListener.back();
    }
  }

  /** Action performed when user clicks Cancel button. Calls pageListener.cancel(). */
  public void actionCancel() {
    if (pageListener != null) {
      pageListener.cancel();
    }
  }

  /** Action performed when user clicks on Next button. Calls pageListener.next(). */
  public void actionNext() {
    if (pageListener != null) {
      pageListener.next();
    }
  }

  public boolean isBackButtonEnabled() {
    return true;
  }

  public boolean isNextButtonEnabled() {
    return true;
  }
}
