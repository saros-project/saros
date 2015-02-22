package de.fu_berlin.inf.dpp.ui.view_parts;

import de.fu_berlin.inf.ag_se.browser.extensions.IJQueryBrowser;
import de.fu_berlin.inf.dpp.ui.browser_functions.AddContactBrowserFunctions;

/**
 * Represents the wizard to add new contacts.
 */
public class AddContactPage implements BrowserPage{

    @Override
    public String getWebpage() {
        return "html/add-contact-page.html";
    }

    @Override
    public void createBrowserFunctions(IJQueryBrowser browser) {
        new AddContactBrowserFunctions(browser).createJavascriptFunctions();
    }

    @Override
    public void render() {
    }
}
