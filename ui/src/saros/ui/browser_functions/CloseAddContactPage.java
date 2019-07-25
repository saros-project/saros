package saros.ui.browser_functions;

import saros.HTMLUIContextFactory;
import saros.ui.ide_embedding.DialogManager;
import saros.ui.pages.AddAccountPage;
import saros.ui.pages.AddContactPage;

/** Close an open {@link AddAccountPage} dialog. */
public class CloseAddContactPage extends TypedJavascriptFunction {

  public static final String JS_NAME = "closeAddContactPage";

  private final DialogManager dialogManager;

  /**
   * Created by PicoContainer
   *
   * @param dialogManager
   * @see HTMLUIContextFactory
   */
  public CloseAddContactPage(DialogManager dialogManager) {
    super(JS_NAME);
    this.dialogManager = dialogManager;
  }

  /** Close an open {@link AddAccountPage} dialog. */
  @BrowserFunction
  public void closeAddContactPage() {
    dialogManager.closeDialogWindow(AddContactPage.HTML_DOC_NAME);
  }
}
