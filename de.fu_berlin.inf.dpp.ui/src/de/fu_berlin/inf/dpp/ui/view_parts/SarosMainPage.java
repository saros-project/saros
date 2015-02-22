package de.fu_berlin.inf.dpp.ui.view_parts;

import de.fu_berlin.inf.ag_se.browser.extensions.IJQueryBrowser;
import de.fu_berlin.inf.dpp.ui.browser_functions.SarosMainPageBrowserFunctions;
import de.fu_berlin.inf.dpp.ui.renderer.SarosMainPageRenderer;

/**
 * Represents the Saros main view.
 */
public class SarosMainPage implements BrowserPage {

    private final SarosMainPageRenderer sarosMainPageRenderer;

    public SarosMainPage(SarosMainPageRenderer sarosMainPageRenderer) {
        this.sarosMainPageRenderer = sarosMainPageRenderer;
    }

    @Override
    public String getWebpage() {
        return "html/saros-angular.html";
    }

    @Override
    public void createBrowserFunctions(IJQueryBrowser browser) {
        new SarosMainPageBrowserFunctions(browser).createJavascriptFunctions();
    }
    @Override
    public void render() {
        sarosMainPageRenderer.render();
    }
}
