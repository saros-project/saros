package de.fu_berlin.inf.dpp.ui.renderer;

import de.fu_berlin.inf.ag_se.browser.extensions.IJQueryBrowser;
import de.fu_berlin.inf.dpp.ui.manager.ContactListManager;
import de.fu_berlin.inf.dpp.ui.model.ContactList;

/**
 * Implementations of this interface can transfer their current state to
 * a browser by calling Javascript functions.
 *
 * As the responsible browser instance may change, all renderers manage a reference
 * to the current browser and provide methods for its replacement.
 *
 * As those browsers may be temporary removed and re-created, the renderers
 * have to deal with null values for the browser property.
 *
 * @JTourBusStop 3, Extending the HTML GUI, Calling Javascript from Java:
 *
 *              Each renderer class is used to transfer parts of the application
 *              state to the browser for rendering.
 *
 *              They may store state themselves, as {@link ContactListRenderer}
 *              does.
 *
 *              The renderer classes call directly into the browser so that
 *              they have to know the currently active browser.
 *
 *              There are two ways to get the state from the Saros:
 *              1. use a listener if supported. See {@link ContactListManager}
 *                 for an example.
 *              2. query the state from the core directly; the {@link AccountRenderer}
 *                 does that.
 *
 *              For the management of GUI state create custom GUI model class
 *              in the model package, like {@link ContactList}.
 *              Those classes should be converted to JSON strings with the GSON
 *              library in the renderer classes.
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
