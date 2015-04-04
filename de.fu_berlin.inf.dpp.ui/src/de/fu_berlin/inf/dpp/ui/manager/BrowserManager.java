package de.fu_berlin.inf.dpp.ui.manager;

import de.fu_berlin.inf.ag_se.browser.extensions.IJQueryBrowser;
import de.fu_berlin.inf.dpp.ui.renderer.Renderer;
import de.fu_berlin.inf.dpp.ui.webpages.BrowserPage;
import de.fu_berlin.inf.dpp.ui.webpages.SarosMainPage;

import java.util.HashMap;
import java.util.Map;

/**
 * This class manages the different browser instances for the dialogs and
 * the main window.
 * As the browser instance for one window may change, this class allows the
 * replacement of each browser.
 */
public class BrowserManager {

    private Map<Class<? extends BrowserPage>, IJQueryBrowser> browsers = new HashMap<Class<? extends BrowserPage>, IJQueryBrowser>();

    /**
     * Sets or replaces the browser for the given page.
     *
     * @param page    the webpage
     * @param browser the browser to be set for the given page
     */
    public synchronized void setBrowser(BrowserPage page,
        IJQueryBrowser browser) {
        browsers.put(page.getClass(), browser);
        for (Renderer renderer : page.getRenderer()) {
            renderer.setBrowser(browser);
        }
        notifyAll();
    }

    /**
     * Removes the browser for the given page
     *
     * @param page the page whose browser should be removed
     */
    public synchronized void removeBrowser(BrowserPage page) {
        browsers.remove(page.getClass());
        for (Renderer renderer : page.getRenderer()) {
            renderer.removeBrowser();
        }
    }

    /**
     * Gets the browser for the main view or null if the view is closed.
     *
     * @return the browser currently displaying the main view or null if not present
     */
    public IJQueryBrowser getMainViewBrowser() {
        return browsers.get(SarosMainPage.class);
    }

    /**
     * Returns the browser for the given page.
     * It waits a certain amount of time if the browser is not already present as
     * it may instantiate at the moment.
     *
     * @param browserPageClass the class of the page whose browser is requested
     * @return the browser displaying the given page
     * @throws RuntimeException if the time out is over
     */
    public synchronized IJQueryBrowser getBrowser(Class<? extends BrowserPage> browserPageClass) {
        long current = System.currentTimeMillis();
        while (!browsers.containsKey(browserPageClass)
            && System.currentTimeMillis() - current < 3000) {
            try {
                wait(3000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        if (browsers.containsKey(browserPageClass)) {
            return browsers.get(browserPageClass);
        } else {
            throw new RuntimeException(
                "Timeout while waiting for the browser to be instatiated.");
        }
    }
}
