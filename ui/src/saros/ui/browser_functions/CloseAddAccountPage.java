package saros.ui.browser_functions;

import saros.HTMLUIContextFactory;
import saros.ui.ide_embedding.DialogManager;
import saros.ui.pages.AddAccountPage;

/** Close an open {@link AddAccountPage} dialog. */
public class CloseAddAccountPage extends TypedJavascriptFunction {

  public static final String JS_NAME = "closeAddAccountPage";

  private final DialogManager dialogManager;

  /**
   * Created by PicoContainer
   *
   * @param dialogManager
   * @see HTMLUIContextFactory
   */
  public CloseAddAccountPage(DialogManager dialogManager) {
    super(JS_NAME);
    this.dialogManager = dialogManager;
  }

  /** Close an open {@link AddAccountPage} dialog. */
  @BrowserFunction
  public void closeAddAccountPage() {
    dialogManager.closeDialogWindow(AddAccountPage.HTML_DOC_NAME);
  }
}
