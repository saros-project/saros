package de.fu_berlin.inf.dpp.whiteboard.ui.browser;

import de.fu_berlin.inf.dpp.ui.util.SWTUtils;
import de.fu_berlin.inf.dpp.whiteboard.ui.HTMLWhiteboardView;
import org.apache.log4j.Logger;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.BrowserFunction;

/**
 * the eclipse implementation of the IWhiteboardBrowser, this implementation uses the SWT browser to
 * transfer events and commands between java and javascript
 */
public class EclipseWhiteboardBrowser implements IWhiteboardBrowser {

  private static final Logger LOG = Logger.getLogger(EclipseWhiteboardBrowser.class);

  private final Browser browser;

  /**
   * this browser will be initialised in the {@link HTMLWhiteboardView}
   *
   * @param browser a SWT browser instance which displays the whiteboard
   */
  public EclipseWhiteboardBrowser(Browser browser) {
    this.browser = browser;
  }

  @Override
  public void asyncRun(final String script) {
    SWTUtils.runSafeSWTAsync(
        LOG,
        new Runnable() {
          @Override
          public void run() {
            browser.evaluate(script);
          }
        });
  }

  @Override
  public void createBrowserFunction(String functionName, final BrowserRunnable runnable) {
    new BrowserFunction(browser, functionName) {
      @Override
      public Object function(Object[] arguments) {
        return runnable.run(arguments);
      }
    };
  }
}
