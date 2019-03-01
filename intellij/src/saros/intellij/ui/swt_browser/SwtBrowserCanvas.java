package saros.intellij.ui.swt_browser;

import de.fu_berlin.inf.ag_se.browser.extensions.IJQueryBrowser;
import java.awt.Canvas;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.picocontainer.annotations.Inject;
import saros.SarosPluginContext;
import saros.ui.ide_embedding.BrowserCreator;
import saros.ui.pages.IBrowserPage;

/**
 * This class is an AWT canvas and is responsible for launching the browser. It represents the AWT
 * part of the AWT-SWT bridge. The creation of the browser itself is done by the {@link
 * BrowserCreator}.
 */
class SwtBrowserCanvas extends Canvas {

  private final IBrowserPage startPage;
  private IJQueryBrowser browser;

  @Inject private BrowserCreator browserCreator;

  /** @param startPage the BrowserPage object containing the page to be displayed */
  SwtBrowserCanvas(IBrowserPage startPage) {
    SarosPluginContext.initComponent(this);
    this.startPage = startPage;
  }

  /**
   * Creates and displays the SWT browser.
   *
   * <p>This method must be called *after* the enclosing frame has been made visible. Otherwise the
   * SWT AWT bridge will throw a {@link org.eclipse.swt.SWT#ERROR_INVALID_ARGUMENT}
   */
  void launchBrowser() {
    final Display display = Display.getDefault();
    display.syncExec(
        new Runnable() {
          @Override
          public void run() {
            browser = createBrowser();
            addResizeListener();
          }
        });
  }

  /** This methods creates a SWT shell and browser in this Canvas. */
  private IJQueryBrowser createBrowser() {
    Shell shell = SWT_AWT.new_Shell(Display.getCurrent(), this);

    IJQueryBrowser browser = browserCreator.createBrowser(shell, SWT.NONE, startPage);

    /* Ideally the size of browser and shell gets set via a resize listener.
     * This does not work when the tool window is re-openend as no size
     * change event is fired. The if clause below sets the size for this case */
    if (getHeight() > 0 && getWidth() > 0) {
      shell.setSize(getWidth(), getHeight());
      browser.setSize(getWidth(), getHeight());
    }

    return browser;
  }

  private void addResizeListener() {
    final ComponentAdapter resizeListener =
        new ComponentAdapter() {
          @Override
          public void componentResized(final ComponentEvent e) {
            Display.getDefault()
                .asyncExec(
                    new Runnable() {
                      @Override
                      public void run() {
                        browser.setSize(e.getComponent().getWidth(), e.getComponent().getHeight());
                      }
                    });
          }
        };

    addComponentListener(resizeListener);
    browser.runOnDisposal(
        new Runnable() {
          @Override
          public void run() {
            removeComponentListener(resizeListener);
          }
        });
  }
}
