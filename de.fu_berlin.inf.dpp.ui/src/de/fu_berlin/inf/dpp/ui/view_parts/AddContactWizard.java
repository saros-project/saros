package de.fu_berlin.inf.dpp.ui.view_parts;

import org.eclipse.swt.browser.Browser;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.ui.browser_functions.ContactListBrowserFunctions;
import de.fu_berlin.inf.dpp.ui.browser_functions.ContactListCoreService;

public class AddContactWizard implements BrowserPage {

    @Inject
    private ContactListCoreService contactListCoreService;

    @Override
    public String getWebpage() {
        return "html/add-contact-wizard.html";
    }

    @Override
    public void createRenderer(Browser browser) {
    }

    @Override
    public void createBrowserFunctions(Browser browser) {
        new ContactListBrowserFunctions(browser, contactListCoreService)
            .createJavascriptFunctions();
    }
}
