package saros.ui.browser_functions;

import saros.HTMLUIContextFactory;
import saros.ui.ide_embedding.DialogManager;
import saros.ui.pages.AccountPage;

/** Close an open {@link AccountPage} dialog. */
public class CloseAccountWizard extends TypedJavascriptFunction {

  public static final String JS_NAME = "closeAddAccountWizard";

  private final DialogManager dialogManager;

  /**
   * Created by PicoContainer
   *
   * @param dialogManager
   * @see HTMLUIContextFactory
   */
  public CloseAccountWizard(DialogManager dialogManager) {
    super(JS_NAME);
    this.dialogManager = dialogManager;
  }

  /** Close an open {@link AccountPage} dialog. */
  @BrowserFunction
  public void closeAddAccountWizard() {
    dialogManager.closeDialogWindow(AccountPage.HTML_DOC_NAME);
  }
}
