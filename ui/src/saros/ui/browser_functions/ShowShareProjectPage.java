package saros.ui.browser_functions;

import saros.HTMLUIContextFactory;
import saros.ui.ide_embedding.DialogManager;
import saros.ui.pages.ShareProjectPage;

/** Open and display the {@link ShareProjectPage} dialog. */
public class ShowShareProjectPage extends TypedJavascriptFunction {

  // TODO: Rename to openXYZ for more convenient naming
  public static final String JS_NAME = "showShareProjectPage";

  private final DialogManager dialogManager;
  private final ShareProjectPage shareProjectPage;

  /**
   * Created by PicoContainer
   *
   * @param dialogManager
   * @param shareProjectPage
   * @see HTMLUIContextFactory
   */
  public ShowShareProjectPage(DialogManager dialogManager, ShareProjectPage shareProjectPage) {
    super(JS_NAME);
    this.dialogManager = dialogManager;
    this.shareProjectPage = shareProjectPage;
  }

  /** Open and display the {@link ShareProjectPage} dialog. */
  @BrowserFunction
  public void showShareProjectPage() {
    dialogManager.showDialogWindow(shareProjectPage);
  }
}
