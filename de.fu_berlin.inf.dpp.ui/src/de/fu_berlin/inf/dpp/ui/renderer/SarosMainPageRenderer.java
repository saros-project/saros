package de.fu_berlin.inf.dpp.ui.renderer;

import de.fu_berlin.inf.dpp.ui.browser_functions.ContactListRenderer;

/**
 * Encapsulates the different renderer for the Saros main page.
 */
public class SarosMainPageRenderer {

    private final AccountRenderer accountRenderer;
    private final ContactListRenderer contactListRenderer;

    public SarosMainPageRenderer(AccountRenderer accountRenderer,
        ContactListRenderer contactListRenderer) {
        this.accountRenderer = accountRenderer;
        this.contactListRenderer = contactListRenderer;
    }

    /**
     * Displays the current state in the browser.
     */
    public void render() {
        accountRenderer.render();
        contactListRenderer.render();
    }
}