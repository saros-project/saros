package de.fu_berlin.inf.dpp.ui.webpages;

import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.HTMLUIStrings;
import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.ui.browser_functions.CloseAccountWizard;
import de.fu_berlin.inf.dpp.ui.browser_functions.SaveAccount;

/**
 * Represents the wizard to manage accounts.
 */
public class AccountPage extends AbstractBrowserPage {
    // TODO: NOT USED AT THE MOMENT! Create HTML page and open it in the
    // main-page.html by calling "__java_showAccountPage();".

    public static final String HTML_DOC_NAME = "account-page.html";
    // Injection of provided BrowserFunctions
    @Inject
    private SaveAccount saveAccount;
    @Inject
    private CloseAccountWizard closeAccountWizard;

    public AccountPage() {
        super(HTML_DOC_NAME, HTMLUIStrings.ADD_ACCOUNT_PAGE_TITLE);
        SarosPluginContext.initComponent(this);

        this.addBrowserFunctions(closeAccountWizard, saveAccount);
        // No renderer used, so let renderers list be empty
    }
}
