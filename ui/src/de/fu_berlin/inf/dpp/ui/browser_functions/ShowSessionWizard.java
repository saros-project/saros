package de.fu_berlin.inf.dpp.ui.browser_functions;

import de.fu_berlin.inf.dpp.HTMLUIContextFactory;
import de.fu_berlin.inf.dpp.ui.ide_embedding.DialogManager;
import de.fu_berlin.inf.dpp.ui.pages.SessionWizardPage;

/** Open and display the {@link SessionWizardPage} dialog. */
public class ShowSessionWizard extends TypedJavascriptFunction {

  // TODO: Rename to openXYZ for more convenient naming
  public static final String JS_NAME = "showSessionWizard";

  private final DialogManager dialogManager;
  private final SessionWizardPage sessionWizardPage;

  /**
   * Created by PicoContainer
   *
   * @param dialogManager
   * @param sessionWizardPage
   * @see HTMLUIContextFactory
   */
  public ShowSessionWizard(DialogManager dialogManager, SessionWizardPage sessionWizardPage) {

    super(JS_NAME);
    this.dialogManager = dialogManager;
    this.sessionWizardPage = sessionWizardPage;
  }

  /** Open and display the {@link SessionWizardPage} dialog. */
  @BrowserFunction
  public void showSessionWizard() {
    dialogManager.showDialogWindow(sessionWizardPage);
  }
}
