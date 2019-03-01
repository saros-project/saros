package saros.ui.browser_functions;

import saros.HTMLUIContextFactory;
import saros.ui.ide_embedding.DialogManager;
import saros.ui.pages.SessionWizardPage;

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
