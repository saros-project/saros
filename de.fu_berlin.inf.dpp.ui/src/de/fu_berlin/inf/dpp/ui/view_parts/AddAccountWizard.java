package de.fu_berlin.inf.dpp.ui.view_parts;

import org.eclipse.swt.browser.Browser;

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
    public void createRenderer(Browser browser) {

    }

    @Override
    public void createBrowserFunctions(Browser browser) {
        new AccountBrowserFunctions(browser).createJavascriptFunctions();
    }
}
