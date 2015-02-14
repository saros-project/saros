package de.fu_berlin.inf.dpp.ui.view_parts;

import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;

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
     * @param contactListManager
     *            the ContactListManager instance
     * @param contactListCoreService
     *            the ContactListCoreService instance
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
    public void createRenderer(Browser browser) {
        contactListManager.setContactListRenderer(new ContactListRenderer(
            browser));
        browser.addDisposeListener(new DisposeListener() {
            @Override
            public void widgetDisposed(DisposeEvent e) {
                contactListManager.removeContactListRenderer();
            }
        });
    }

    @Override
    public void createBrowserFunctions(Browser browser) {
        new AccountBrowserFunctions(browser).createJavascriptFunctions();
        new ContactListBrowserFunctions(browser, contactListCoreService)
            .createJavascriptFunctions();
    }
}
