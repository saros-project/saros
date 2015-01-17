package de.fu_berlin.inf.dpp.ui.manager;

import de.fu_berlin.inf.dpp.ui.browser_functions.AccountBrowserFunctions;
import de.fu_berlin.inf.dpp.ui.browser_functions.ContactListBrowserFunctions;
import de.fu_berlin.inf.dpp.ui.browser_functions.ContactListCoreService;
import de.fu_berlin.inf.dpp.ui.browser_functions.ContactListRenderer;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;

/**
 * Encloses the different managers for the UI.
 * Up till now there only exists the {@link de.fu_berlin.inf.dpp.ui.manager.ContactListManager}.
 * Other managers for managing the chat, various user activities etc. will follow.
 */
public class HTMLUIManager {

    private final ContactListManager contactListManager;

    private final ContactListCoreService contactListCoreService;

    public HTMLUIManager(ContactListManager contactListManager,
        ContactListCoreService contactListCoreService) {
        this.contactListManager = contactListManager;
        this.contactListCoreService = contactListCoreService;
    }

    /**
     * Creates the appropriate renderer classes for a given SWT browser
     * and sets the created renderers in its managed classes.
     * Up till now this is onlye the contact list manager.
     *
     * @param browser the SWT browser
     */
    public void createRenderer(Browser browser) {
        ContactListRenderer contactListRenderer = new ContactListRenderer(
            browser);
        contactListManager.setContactListRenderer(contactListRenderer);
        browser.addDisposeListener(new DisposeListener() {
            @Override
            public void widgetDisposed(DisposeEvent e) {
                contactListManager.removeContactListRenderer();
            }
        });
    }

    /**
     * Creates the appropriate browser functions for a given SWT browser.
     *
     * @param browser the SWT browser
     */
    public void createBrowserFunctions(Browser browser) {
        ContactListBrowserFunctions contactListBrowserFunctions = new ContactListBrowserFunctions(
            browser, contactListCoreService);
        contactListBrowserFunctions.createJavascriptFunctions();
        new AccountBrowserFunctions(browser).createJavascriptFunctions();
    }
}
