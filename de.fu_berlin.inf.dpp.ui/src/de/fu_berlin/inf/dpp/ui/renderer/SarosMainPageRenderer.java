package de.fu_berlin.inf.dpp.ui.renderer;

/**
 * Encapsulates the different renderer for the Saros main page.
 */
public class SarosMainPageRenderer implements Renderer{

    private final AccountRenderer accountRenderer;
    private final ContactListRenderer contactListRenderer;

    public SarosMainPageRenderer(AccountRenderer accountRenderer,
        ContactListRenderer contactListRenderer) {
        this.accountRenderer = accountRenderer;
        this.contactListRenderer = contactListRenderer;
    }

    @Override
    public void render() {
        accountRenderer.render();
        contactListRenderer.render();
    }
}