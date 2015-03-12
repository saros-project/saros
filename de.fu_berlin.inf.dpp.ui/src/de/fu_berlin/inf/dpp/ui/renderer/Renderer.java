package de.fu_berlin.inf.dpp.ui.renderer;

import de.fu_berlin.inf.ag_se.browser.extensions.IJQueryBrowser;

/**
 * Implementations of this interface can transfer their current state to
 * a browser by calling Javascript functions.
 *
 * As the responsible browser instance may change, all renderers manage a reference
 * to the current browser and provide methods for its replacement.
 */
public abstract class Renderer {

    protected IJQueryBrowser browser;

    /**
     * Displays the current state managed by the renderer in the browser.
     */
    public abstract void render();

    /**
     * Sets the current browser instance, any reference to a previous
     * browser is replaced.
     *
     * @param browser the browser to be set
     */
    public synchronized void setBrowser(IJQueryBrowser browser) {
        this.browser = browser;
        render();
    }

    /**
     * Removes the set browser. This method should be called every time the browser
     * is disposed.
     */
    public synchronized void removeBrowser() {
        browser = null;
    }
}
