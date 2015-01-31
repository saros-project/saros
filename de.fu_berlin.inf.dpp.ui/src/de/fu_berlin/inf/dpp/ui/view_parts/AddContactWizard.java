package de.fu_berlin.inf.dpp.ui.view_parts;

import de.fu_berlin.inf.ag_se.browser.extensions.IJQueryBrowser;
import de.fu_berlin.inf.dpp.ui.browser_functions.ContactListBrowserFunctions;
import de.fu_berlin.inf.dpp.ui.browser_functions.ContactListCoreService;
import org.picocontainer.annotations.Inject;

public class AddContactWizard implements BrowserPage{

    @Inject
    private ContactListCoreService contactListCoreService;

    @Override
    public String getWebpage() {
        return "html/add-contact-wizard.html";
    }

    @Override
    public void createRenderer(IJQueryBrowser browser) {
    }

    @Override
    public void createBrowserFunctions(IJQueryBrowser browser) {
        new ContactListBrowserFunctions(browser, contactListCoreService).createJavascriptFunctions();
    }
}
