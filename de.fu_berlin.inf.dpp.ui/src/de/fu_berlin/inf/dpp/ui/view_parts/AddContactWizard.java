package de.fu_berlin.inf.dpp.ui.view_parts;

import de.fu_berlin.inf.ag_se.browser.extensions.IJQueryBrowser;
import de.fu_berlin.inf.dpp.ui.browser_functions.ContactListBrowserFunctions;
import de.fu_berlin.inf.dpp.ui.browser_functions.ContactListCoreService;

/**
 * Represents the wizard to add new contacts.
 */
public class AddContactWizard implements BrowserPage{

    private final ContactListCoreService contactListCoreService;

    public AddContactWizard(ContactListCoreService contactListCoreService) {
        this.contactListCoreService = contactListCoreService;
    }

    @Override
    public String getWebpage() {
        return "html/add-contact-wizard.html";
    }

    @Override
    public void createBrowserFunctions(IJQueryBrowser browser) {
        new ContactListBrowserFunctions(browser, contactListCoreService).createJavascriptFunctions();
    }

    @Override
    public void render() {
    }
}
