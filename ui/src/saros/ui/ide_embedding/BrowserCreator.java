package saros.ui.ide_embedding;

import de.fu_berlin.inf.ag_se.browser.extensions.IJQueryBrowser;
import de.fu_berlin.inf.ag_se.browser.functions.JavascriptFunction;
import de.fu_berlin.inf.ag_se.browser.swt.SWTJQueryBrowser;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.swt.widgets.Composite;
import org.jivesoftware.smack.util.StringUtils;
import saros.HTMLUIContextFactory;
import saros.ui.manager.BrowserManager;
import saros.ui.pages.IBrowserPage;

/**
 * This class represents the IDE-independent part of the browser creation. It resorts to
 * IDE-specific resource location however by using the correct instance of {@link
 * IUIResourceLocator} which is injected by PicoContainer.
 *
 * <p>During the creation of a {@link IJQueryBrowser} all {@link JavascriptFunction}s that are
 * registered in the {@link HTMLUIContextFactory} are injected into this browser instance.
 */
public class BrowserCreator {

  private final BrowserManager browserManager;

  private final IUIResourceLocator resourceLocator;

  private final List<JavascriptFunction> browserFunctions;

  /**
   * Injected via PicoContainer
   *
   * @param browserManager
   * @param resourceLocator
   * @see HTMLUIContextFactory
   */
  public BrowserCreator(BrowserManager browserManager, IUIResourceLocator resourceLocator) {
    this.browserManager = browserManager;
    this.resourceLocator = resourceLocator;
    this.browserFunctions = new ArrayList<JavascriptFunction>();
  }

  /**
   * Adds a function to the set of {@link JavascriptFunction} that will be injected to any new
   * {@link IJQueryBrowser browser} instance created with {@link #createBrowser(Composite, int,
   * IBrowserPage) createBrowser()}. This does not affect already created browser instances.
   */
  public void addBrowserFunction(JavascriptFunction function) {
    browserFunctions.add(function);
  }

  /**
   * Creates a new browser instance.
   *
   * @param composite the composite enclosing the browser.
   * @param style the style of the browser instance.
   * @param page the page which should be displayed.
   * @return a browser instance which loads and renders the given {@link IBrowserPage BrowserPage}
   */
  public IJQueryBrowser createBrowser(Composite composite, int style, final IBrowserPage page) {

    final String resourceName = page.getRelativePath();
    assert resourceName != null;

    final IJQueryBrowser browser = SWTJQueryBrowser.createSWTBrowser(composite, style);

    String resourceLocation = resourceLocator.getResourceLocation(resourceName);

    if (resourceLocation == null) {
      browser.setText(
          "<html><body><pre>"
              + "Resource <b>"
              + StringUtils.escapeForXML(resourceName)
              + "</b> could not be found.</pre></body></html>");
      return browser;
    }

    browser.open(resourceLocation, 5000);

    for (JavascriptFunction function : browserFunctions) browser.createBrowserFunction(function);

    browserManager.setBrowser(page, browser);
    browser.runOnDisposal(
        new Runnable() {
          @Override
          public void run() {
            browserManager.removeBrowser(page);
          }
        });

    return browser;
  }
}
