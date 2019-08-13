package saros.ui.ide_embedding;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.swt.widgets.Composite;
import org.jivesoftware.smack.util.StringUtils;
import saros.HTMLUIContextFactory;
import saros.ui.browser.AbstractJavascriptFunction;
import saros.ui.browser.IBrowser;
import saros.ui.browser.SWTBrowser;
import saros.ui.manager.BrowserManager;
import saros.ui.pages.IBrowserPage;

/**
 * This class represents the IDE-independent part of the browser creation. It resorts to
 * IDE-specific resource location however by using the correct instance of {@link
 * IUIResourceLocator} which is injected by PicoContainer.
 *
 * <p>During the creation of a {@link SWTBrowser} all {@link AbstractJavascriptFunction}s that are
 * registered in the {@link HTMLUIContextFactory} are injected into this browser instance.
 */
public class BrowserCreator {

  private final BrowserManager browserManager;

  private final IUIResourceLocator resourceLocator;

  private final List<AbstractJavascriptFunction> browserFunctions;

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
    this.browserFunctions = new ArrayList<AbstractJavascriptFunction>();
  }

  /**
   * Adds a function to the set of {@link AbstractJavascriptFunction} that will be injected to any
   * new {@link SWTBrowser browser} instance created with {@link #createBrowser(Composite, int,
   * IBrowserPage) createBrowser()}. This does not affect already created browser instances.
   */
  public void addBrowserFunction(AbstractJavascriptFunction function) {
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
  public IBrowser createBrowser(Composite composite, int style, final IBrowserPage page) {

    final String resourceName = page.getRelativePath();
    assert resourceName != null;

    final IBrowser browser = new SWTBrowser(composite, style);

    String resourceLocation = resourceLocator.getResourceLocation(resourceName);

    if (resourceLocation == null) {
      browser.loadHtml(
          "<html><body><pre>"
              + "Resource <b>"
              + StringUtils.escapeForXML(resourceName)
              + "</b> could not be found.</pre></body></html>");
      return browser;
    }

    browser.loadUrl(resourceLocation, 5000);

    for (AbstractJavascriptFunction function : browserFunctions)
      browser.addBrowserFunction(function);

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
