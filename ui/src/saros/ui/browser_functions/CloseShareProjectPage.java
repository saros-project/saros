package saros.ui.browser_functions;

import saros.HTMLUIContextFactory;
import saros.ui.ide_embedding.DialogManager;
import saros.ui.pages.ShareProjectPage;

/** Close an open {@link ShareProjectPage} dialog. */
public class CloseShareProjectPage extends TypedJavascriptFunction {

  public static final String JS_NAME = "closeShareProjectPage";

  private final DialogManager dialogManager;

  /**
   * Created by PicoContainer
   *
   * @param dialogManager
   * @see HTMLUIContextFactory
   */
  public CloseShareProjectPage(DialogManager dialogManager) {
    super(JS_NAME);
    this.dialogManager = dialogManager;
  }

  /** Close an open {@link ShareProjectPage} dialog. */
  @BrowserFunction
  public void closeShareProjectPage() {
    dialogManager.closeDialogWindow(ShareProjectPage.HTML_DOC_NAME);
  }
}
