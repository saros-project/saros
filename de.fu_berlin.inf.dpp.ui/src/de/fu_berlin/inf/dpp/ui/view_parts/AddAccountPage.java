package de.fu_berlin.inf.dpp.ui.view_parts;

import de.fu_berlin.inf.ag_se.browser.extensions.IJQueryBrowser;
import de.fu_berlin.inf.dpp.ui.browser_functions.AccountBrowserFunctions;

/**
 * Represents the wizard to add new accounts.
 */
public class AddAccountPage implements BrowserPage {

    @Override
    public String getWebpage() {
        return "html/add-account-page.html";
    }

    @Override
    public void createBrowserFunctions(IJQueryBrowser browser) {
        new AccountBrowserFunctions(browser).createJavascriptFunctions();
    }

    @Override
    public void render() {
    }
}
