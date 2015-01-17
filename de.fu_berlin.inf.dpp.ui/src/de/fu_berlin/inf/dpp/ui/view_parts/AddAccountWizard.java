package de.fu_berlin.inf.dpp.ui.view_parts;

import de.fu_berlin.inf.dpp.ui.browser_functions.AccountBrowserFunctions;
import de.fu_berlin.inf.dpp.util.BrowserUtils;
import org.eclipse.swt.browser.Browser;

/**
 * Represents the wizard to add new accounts.
 */
public class AddAccountWizard implements BrowserPage {

    @Override
    public String getWebpage() {
        return BrowserUtils
            .getUrlForClasspathFile("/html/add-user-wizard.html");
    }

    @Override
    public void createRenderer(Browser browser) {

    }

    @Override
    public void createBrowserFunctions(Browser browser) {
        new AccountBrowserFunctions(browser).createJavascriptFunctions();
    }
}
