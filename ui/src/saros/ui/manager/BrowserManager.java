package saros.ui.manager;

import de.fu_berlin.inf.ag_se.browser.extensions.IJQueryBrowser;
import java.util.HashMap;
import java.util.Map;
import saros.ui.pages.IBrowserPage;
import saros.ui.renderer.Renderer;

/**
 * This class manages the different browser instances for the dialogs and the main window. As the
 * browser instance for one window may change, this class allows the replacement of each browser.
 */
public class BrowserManager {

  private Map<Class<? extends IBrowserPage>, IJQueryBrowser> browsers =
      new HashMap<Class<? extends IBrowserPage>, IJQueryBrowser>();

  /**
   * Sets or replaces the browser for the given page and all corresponding renderers.
   *
   * @param page the page
   * @param browser the browser to be set for the given page
   */
  public synchronized void setBrowser(IBrowserPage page, IJQueryBrowser browser) {
    browsers.put(page.getClass(), browser);
    for (Renderer renderer : page.getRenderers()) {
      renderer.addBrowser(browser);
    }
    notifyAll();
  }

  /**
   * Removes the browser for the given page and all corresponding renderers
   *
   * @param page the page whose browser should be removed
   */
  public synchronized void removeBrowser(IBrowserPage page) {

    for (Renderer renderer : page.getRenderers()) {
      renderer.removeBrowser(browsers.get(page.getClass()));
    }
    browsers.remove(page.getClass());
  }

  /**
   * Returns the browser for the given page. It waits a certain amount of time if the browser is not
   * already present as it may instantiate at the moment.
   *
   * @param browserPageClass the class of the page whose browser is requested
   * @return the browser displaying the given page
   * @throws RuntimeException if the time out is over
   */
  public synchronized IJQueryBrowser getBrowser(Class<? extends IBrowserPage> browserPageClass) {
    long current = System.currentTimeMillis();
    while (!browsers.containsKey(browserPageClass) && System.currentTimeMillis() - current < 3000) {
      try {
        wait(3000);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }

    if (browsers.containsKey(browserPageClass)) {
      return browsers.get(browserPageClass);
    } else {
      throw new RuntimeException("Timeout while waiting for the browser to be instatiated.");
    }
  }
}
