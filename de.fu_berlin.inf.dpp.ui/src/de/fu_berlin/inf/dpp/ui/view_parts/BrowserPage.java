package de.fu_berlin.inf.dpp.ui.view_parts;

import org.eclipse.swt.browser.Browser;

/**
 * A browser page encapsulates the location of the HTML page as well as
 * logic to connect the needed Java and Javascript functions.
 */
public interface BrowserPage {

    /**
     * @return a file URL to the website which is bundled in the resource folder
     */
    String getWebpage();

    /**
     * Creates the needed renderer classes and sets them in the manager classes.
     * @param browser the SWT browser instance
     */
    void createRenderer(Browser browser);

    /**
     * Creates the neeeded {@link org.eclipse.swt.browser.BrowserFunction}s for the
     * webpage.
     *
     * @param browser the SWT browser instance
     */
    void createBrowserFunctions(Browser browser);
}
