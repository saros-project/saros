package de.fu_berlin.inf.dpp.ui.browser_functions;

import de.fu_berlin.inf.dpp.HTMLUIContextFactory;
import de.fu_berlin.inf.dpp.ui.ide_embedding.DialogManager;
import de.fu_berlin.inf.dpp.ui.pages.AccountPage;

/** Open and display {@link AccountPage} dialog. */
public class ShowAccountPage extends TypedJavascriptFunction {

  public static final String JS_NAME = "showAccountPage";

  private final DialogManager dialogManager;
  private final AccountPage accountPage;

  /**
   * Created by PicoContainer
   *
   * @param dialogManager
   * @param accountPage
   * @see HTMLUIContextFactory
   */
  public ShowAccountPage(DialogManager dialogManager, AccountPage accountPage) {
    super(JS_NAME);
    this.dialogManager = dialogManager;
    this.accountPage = accountPage;
  }

  /** Open and display {@link AccountPage} dialog. */
  @BrowserFunction
  public void showAccountPage() {
    dialogManager.showDialogWindow(accountPage);
  }
}
