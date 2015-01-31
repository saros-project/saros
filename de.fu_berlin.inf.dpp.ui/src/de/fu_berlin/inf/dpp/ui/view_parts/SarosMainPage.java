package de.fu_berlin.inf.dpp.ui.view_parts;

import de.fu_berlin.inf.ag_se.browser.extensions.IJQueryBrowser;
import de.fu_berlin.inf.dpp.ui.browser_functions.AccountBrowserFunctions;
import de.fu_berlin.inf.dpp.ui.browser_functions.ContactListBrowserFunctions;
import de.fu_berlin.inf.dpp.ui.browser_functions.ContactListCoreService;
import de.fu_berlin.inf.dpp.ui.browser_functions.ContactListRenderer;
import de.fu_berlin.inf.dpp.ui.manager.ContactListManager;

/**
 * Represents the Saros main view.
 */
public class SarosMainPage implements BrowserPage {

    private final ContactListManager contactListManager;

    private final ContactListCoreService contactListCoreService;

    /**
     * Parameters are injected by pico container.
     *
     * @param contactListManager     the ContactListManager instance
     * @param contactListCoreService the ContactListCoreService instance
     */
    public SarosMainPage(ContactListManager contactListManager,
        ContactListCoreService contactListCoreService) {
        this.contactListManager = contactListManager;
        this.contactListCoreService = contactListCoreService;
    }

    @Override
    public String getWebpage() {
        return "html/saros-angular.html";
    }

    @Override
    public void createRenderer(IJQueryBrowser browser) {
        contactListManager
            .setContactListRenderer(new ContactListRenderer(browser));
        browser.runOnDisposal(new Runnable() {
            @Override
            public void run() {
                contactListManager.removeContactListRenderer();
            }
        });
    }

    @Override
    public void createBrowserFunctions(IJQueryBrowser browser) {
        new AccountBrowserFunctions(browser).createJavascriptFunctions();
        new ContactListBrowserFunctions(browser, contactListCoreService)
            .createJavascriptFunctions();
    }
}
