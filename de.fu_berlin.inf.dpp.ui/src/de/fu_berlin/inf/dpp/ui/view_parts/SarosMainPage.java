package de.fu_berlin.inf.dpp.ui.view_parts;

import de.fu_berlin.inf.ag_se.browser.extensions.IJQueryBrowser;
import de.fu_berlin.inf.dpp.ui.browser_functions.AccountBrowserFunctions;
import de.fu_berlin.inf.dpp.ui.core_services.AccountCoreService;
import de.fu_berlin.inf.dpp.ui.renderer.AccountRenderer;
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

    private final AccountCoreService accountCoreService;

    public SarosMainPage(ContactListManager contactListManager,
        ContactListCoreService contactListCoreService,
        AccountCoreService accountCoreService) {
        this.contactListManager = contactListManager;
        this.contactListCoreService = contactListCoreService;
        this.accountCoreService = accountCoreService;
    }

    @Override
    public String getWebpage() {
        return "html/saros-angular.html";
    }

    @Override
    public void createRenderer(IJQueryBrowser browser) {
        contactListManager
            .setContactListRenderer(new ContactListRenderer(browser));
        accountCoreService.setRenderer(new AccountRenderer(browser));
        browser.runOnDisposal(new Runnable() {
            @Override
            public void run() {
                contactListManager.removeContactListRenderer();
                accountCoreService.removeRenderer();
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
