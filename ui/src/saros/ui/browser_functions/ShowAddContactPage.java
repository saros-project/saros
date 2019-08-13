package saros.ui.browser_functions;

import saros.HTMLUIContextFactory;
import saros.ui.ide_embedding.DialogManager;
import saros.ui.pages.AddAccountPage;
import saros.ui.pages.AddContactPage;

/** Open and display {@link AddAccountPage} dialog. */
public class ShowAddContactPage extends TypedJavascriptFunction {

  public static final String JS_NAME = "showAddContactPage";

  private final DialogManager dialogManager;
  private final AddContactPage addContactPage;

  /**
   * Created by PicoContainer
   *
   * @param dialogManager
   * @param addContactPage
   * @see HTMLUIContextFactory
   */
  public ShowAddContactPage(DialogManager dialogManager, AddContactPage addContactPage) {
    super(JS_NAME);
    this.dialogManager = dialogManager;
    this.addContactPage = addContactPage;
  }

  /** Open and display {@link AddAccountPage} dialog. */
  @BrowserFunction
  public void showAddContactPage() {
    dialogManager.showDialogWindow(addContactPage);
  }
}
