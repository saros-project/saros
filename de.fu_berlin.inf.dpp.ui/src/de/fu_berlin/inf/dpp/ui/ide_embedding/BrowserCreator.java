package de.fu_berlin.inf.dpp.ui.ide_embedding;

import de.fu_berlin.inf.ag_se.browser.extensions.IJQueryBrowser;
import de.fu_berlin.inf.ag_se.browser.functions.JavascriptFunction;
import de.fu_berlin.inf.ag_se.browser.swt.SWTJQueryBrowser;
import de.fu_berlin.inf.dpp.ui.manager.BrowserManager;
import de.fu_berlin.inf.dpp.ui.webpages.BrowserPage;
import org.eclipse.swt.widgets.Composite;
import org.jivesoftware.smack.util.StringUtils;

/**
 * This class represents the IDE-independent part of the browser creation.
 * It resorts to IDE-specific resource location however by using the correct
 * instance of {@link IWebResourceLocator} which is injected by PicoContainer.
 */
public class BrowserCreator {

    private final BrowserManager browserManager;

    private final IWebResourceLocator resourceLocator;

    public BrowserCreator(BrowserManager browserManager,
        IWebResourceLocator resourceLocator) {
        this.browserManager = browserManager;
        this.resourceLocator = resourceLocator;
    }

    /**
     * Creates a new browser instance.
     *
     * @param composite the composite enclosing the browser.
     * @param style     the style of the browser instance.
     * @param page      the page which should be displayed.
     * @return a browser instance which loads and renders the given
     * {@link BrowserPage BrowserPage}
     */
    public IJQueryBrowser createBrowser(Composite composite, int style,
        final BrowserPage page) {

        final String resourceName = page.getWebpage();
        assert resourceName != null;

        final IJQueryBrowser browser = SWTJQueryBrowser
            .createSWTBrowser(composite, style);

        String resourceLocation = resourceLocator
            .getResourceLocation(resourceName);

        if (resourceLocation == null) {
            browser.setText("<html><body><pre>" + "Resource <b>" + StringUtils
                .escapeForXML(resourceName)
                + "</b> could not be found.</pre></body></html>");
            return browser;
        }

        browser.open(resourceLocation, 5000);

        for (JavascriptFunction function : page.getJavascriptFunctions()) {
            browser.createBrowserFunction(function);
        }

        browserManager.setBrowser(page, browser);
        browser.runOnDisposal(new Runnable() {
            @Override
            public void run() {
                browserManager.removeBrowser(page);
            }
        });

        return browser;
    }
}
