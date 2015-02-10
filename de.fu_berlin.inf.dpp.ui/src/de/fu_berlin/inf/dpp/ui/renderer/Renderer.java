package de.fu_berlin.inf.dpp.ui.renderer;

/**
 * Implementations of this interface can transfer their current state to
 * a browser by calling Javascript functions.
 */
public interface Renderer {

    /**
     * Displays the current state managed by the renderer in the browser.
     */
    void render();
}
