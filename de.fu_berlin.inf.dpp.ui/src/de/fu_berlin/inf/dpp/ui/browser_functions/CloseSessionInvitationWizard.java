package de.fu_berlin.inf.dpp.ui.browser_functions;

import de.fu_berlin.inf.dpp.HTMLUIContextFactory;
import de.fu_berlin.inf.dpp.ui.ide_embedding.DialogManager;
import de.fu_berlin.inf.dpp.ui.pages.SessionWizardPage;

/** Close an open {@link SessionWizardPage} dialog. */
public class CloseSessionInvitationWizard extends TypedJavascriptFunction {

  public static final String JS_NAME = "closeStartSessionWizard";

  private final DialogManager dialogManager;

  /**
   * Created by PicoContainer
   *
   * @param dialogManager
   * @see HTMLUIContextFactory
   */
  public CloseSessionInvitationWizard(DialogManager dialogManager) {
    super(JS_NAME);
    this.dialogManager = dialogManager;
  }

  /** Close an open {@link SessionWizardPage} dialog. */
  @BrowserFunction
  public void closeStartSessionWizard() {
    dialogManager.closeDialogWindow(SessionWizardPage.HTML_DOC_NAME);
  }
}
