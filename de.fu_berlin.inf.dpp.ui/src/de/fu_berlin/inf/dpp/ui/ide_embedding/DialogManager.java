package de.fu_berlin.inf.dpp.ui.ide_embedding;

import de.fu_berlin.inf.ag_se.browser.IBrowser;
import de.fu_berlin.inf.dpp.synchronize.UISynchronizer;
import de.fu_berlin.inf.dpp.ui.pages.AbstractBrowserPage;
import de.fu_berlin.inf.dpp.ui.pages.IBrowserPage;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;

/**
 * IDE-independent base class for managing HTML dialogs.
 *
 * <p>Those dialogs are displayed in a new window inside a browser. The simultaneous display of
 * multiple dialogs is supported. However, there may only be one dialog open for each {@link
 * IBrowserPage IBrowserPage} at the same time.
 */
public abstract class DialogManager {

  private static final Logger LOG = Logger.getLogger(DialogManager.class);

  private Map<String, IBrowserDialog> openDialogs = new HashMap<String, IBrowserDialog>();

  private final UISynchronizer uiSynchronizer;

  public DialogManager(UISynchronizer uiSynchronizer) {
    this.uiSynchronizer = uiSynchronizer;
  }

  /**
   * Shows a dialog displaying an HTML page inside a {@link IBrowser}. For each {@link IBrowserPage}
   * there may only be one open dialog window. If this method is called when the dialog is already
   * displayed, nothing happens.
   *
   * <p>May be called from any thread.
   *
   * @param browserPage the BrowserPage object to be displayed in the dialog
   */
  public void showDialogWindow(final IBrowserPage browserPage) {
    uiSynchronizer.asyncExec(
        new Runnable() {
          @Override
          public void run() {
            String resource = browserPage.getRelativePath();

            if (dialogIsOpen(resource)) {
              // If the user try to open a dialog that is already open,
              // the dialog should get active and in the foreground to
              // help the user find it.

              reopenDialogWindow(resource);
              return;
            }

            IBrowserDialog dialog = createDialog(browserPage);

            openDialogs.put(resource, dialog);
          }
        });
  }

  /**
   * This method is to overwritten to implement the IDE-specific opening of the dialog. This method
   * must only be called on the UI thread.
   *
   * @param browserPage the BrowserPage object to be displayed in the dialog
   * @return an IDE-independent representation of the dialog
   */
  protected abstract IBrowserDialog createDialog(IBrowserPage browserPage);

  /**
   * Closes the dialog displaying the given page.
   *
   * <p>May be called from any thread.
   *
   * @param pageId a String representing the page that should be closed. Since we use the
   *     pageResource as an identifier, this string can be obtained via {@link
   *     IBrowserPage#getRelativePath()}
   */
  public void closeDialogWindow(final String pageId) {
    uiSynchronizer.asyncExec(
        new Runnable() {
          @Override
          public void run() {
            if (!dialogIsOpen(AbstractBrowserPage.PATH + pageId)) {
              LOG.warn(AbstractBrowserPage.PATH + pageId + "could not be found");
              return;
            }

            // shell is removed in the ShellLister
            openDialogs.get(AbstractBrowserPage.PATH + pageId).close();
          }
        });
  }

  /**
   * Set the location of a given dialog to the center of the eclipse instance. If the given
   * browserPage is not currently displayed in a shell/dialog this does nothing.
   *
   * @param pageId a String representing the HTML page. Since we use the pageResource as an
   *     identifier, this string can be obtained via {@link IBrowserPage#getRelativePath()}
   */
  private void reopenDialogWindow(String pageId) {
    if (!dialogIsOpen(pageId)) {
      LOG.warn(pageId + "could not be found");
      return;
    }

    IBrowserDialog dialog = openDialogs.get(pageId);
    dialog.reopen();
  }

  /**
   * @param pageId a String representing the page. Since we use the pageResource as an identifier,
   *     this string can be obtained via {@link IBrowserPage#getRelativePath()}
   * @return true if the browserPage is currently displayed in a shell/dialog
   */
  private boolean dialogIsOpen(String pageId) {
    return openDialogs.containsKey(pageId);
  }

  /**
   * This method should be called in the IDE-specific close listeners to remove the entry for the
   * dialog.
   *
   * @param pageId a String representing the page. Since we use the pageResource as an identifier,
   *     this string can be obtained via {@link IBrowserPage#getRelativePath()}
   */
  protected void removeDialogEntry(String pageId) {
    LOG.debug(pageId + " is closed");
    openDialogs.remove(pageId);
  }
}
