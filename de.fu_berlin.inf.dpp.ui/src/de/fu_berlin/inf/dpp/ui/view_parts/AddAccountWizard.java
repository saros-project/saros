package de.fu_berlin.inf.dpp.ui.view_parts;

import de.fu_berlin.inf.ag_se.browser.extensions.IJQueryBrowser;
import de.fu_berlin.inf.dpp.ui.browser_functions.AccountBrowserFunctions;

/**
 * Represents the wizard to add new accounts.
 */
public class AddAccountWizard implements BrowserPage {

    @Override
    public String getWebpage() {
        return "html/add-user-wizard.html";
    }

    @Override
    public void createBrowserFunctions(IJQueryBrowser browser) {
        new AccountBrowserFunctions(browser).createJavascriptFunctions();
    }

    @Override
    public void render() {
    }
}
