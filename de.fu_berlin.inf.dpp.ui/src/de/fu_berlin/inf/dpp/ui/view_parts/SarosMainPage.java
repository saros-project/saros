package de.fu_berlin.inf.dpp.ui.view_parts;

import de.fu_berlin.inf.ag_se.browser.extensions.IJQueryBrowser;
import de.fu_berlin.inf.dpp.ui.browser_functions.AccountBrowserFunctions;
import de.fu_berlin.inf.dpp.ui.browser_functions.ContactListBrowserFunctions;
import de.fu_berlin.inf.dpp.ui.core_services.ContactListCoreService;
import de.fu_berlin.inf.dpp.ui.renderer.SarosMainPageRenderer;

/**
 * Represents the Saros main view.
 */
public class SarosMainPage implements BrowserPage {

    private final ContactListCoreService contactListCoreService;

    private final SarosMainPageRenderer sarosMainPageRenderer;

    public SarosMainPage(ContactListCoreService contactListCoreService,
        SarosMainPageRenderer sarosMainPageRenderer) {
        this.contactListCoreService = contactListCoreService;
        this.sarosMainPageRenderer = sarosMainPageRenderer;
    }

    @Override
    public String getWebpage() {
        return "html/saros-angular.html";
    }

    @Override
    public void createBrowserFunctions(IJQueryBrowser browser) {
        new AccountBrowserFunctions(browser).createJavascriptFunctions();
        new ContactListBrowserFunctions(browser, contactListCoreService)
            .createJavascriptFunctions();
    }
    @Override
    public void render() {
        sarosMainPageRenderer.render();
    }
}
