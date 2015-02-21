package de.fu_berlin.inf.dpp.ui.view_parts;

import de.fu_berlin.inf.ag_se.browser.extensions.IJQueryBrowser;

/**
 * A browser page encapsulates the location of the HTML page as well as
 * logic to connect the needed Java and Javascript functions.
 */
public interface BrowserPage {

    /**
     * Returns the resource name of this <code>BowserPage</code> or <code>null</code> if there is no resource associated with this page.
     * <p/>
     * E.g: html/index.html
     * <p/>
     * It is up to the caller to resolve the absolute physical location.
     *
     * @return the resource name or <code>null</code>
     * @see ClassLoader#getResource(String name)
     */
    String getWebpage();

    /**
     * Creates the neeeded {@link org.eclipse.swt.browser.BrowserFunction}s for
     * the webpage.
     *
     * @param browser the SWT browser instance
     */
    void createBrowserFunctions(IJQueryBrowser browser);

    /**
     * Displays the current state of the page in the browser.
     * TODO: maybe it makes sense to implement a Renderer#render(Page) method instead
     */
    void render();
}
