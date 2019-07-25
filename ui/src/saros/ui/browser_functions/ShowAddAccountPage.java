package saros.ui.browser_functions;

import saros.HTMLUIContextFactory;
import saros.ui.ide_embedding.DialogManager;
import saros.ui.pages.AddAccountPage;

/** Open and display {@link AddAccountPage} dialog. */
public class ShowAddAccountPage extends TypedJavascriptFunction {

  public static final String JS_NAME = "showAddAccountPage";

  private final DialogManager dialogManager;
  private final AddAccountPage addAccountPage;

  /**
   * Created by PicoContainer
   *
   * @param dialogManager
   * @param addAccountPage
   * @see HTMLUIContextFactory
   */
  public ShowAddAccountPage(DialogManager dialogManager, AddAccountPage addAccountPage) {
    super(JS_NAME);
    this.dialogManager = dialogManager;
    this.addAccountPage = addAccountPage;
  }

  /** Open and display {@link AddAccountPage} dialog. */
  @BrowserFunction
  public void showAddAccountPage() {
    dialogManager.showDialogWindow(addAccountPage);
  }
}
