package de.fu_berlin.inf.dpp.ui.browser_functions;

import de.fu_berlin.inf.ag_se.browser.IBrowserFunction;
import de.fu_berlin.inf.ag_se.browser.extensions.IJQueryBrowser;
import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.ui.manager.IDialogManager;
import de.fu_berlin.inf.dpp.ui.view_parts.AddAccountPage;
import org.apache.log4j.Logger;
import org.picocontainer.annotations.Inject;

/**
 * This class contains functions to be called from Javascript.
 * It contains only the so called browser functions concerning the account
 * management.
 */
public class AccountBrowserFunctions {

    private static final Logger LOG = Logger
        .getLogger(AccountBrowserFunctions.class);

    @Inject
    private IDialogManager dialogManager;

    @Inject
    private AddAccountPage addAccountPage;

    private final IJQueryBrowser browser;

    public AccountBrowserFunctions(IJQueryBrowser browser) {
        SarosPluginContext.initComponent(this);
        this.browser = browser;
    }

    /**
     * Injects Javascript functions into the HTML page. These functions
     * call Java code below when invoked.
     */
    public void createJavascriptFunctions() {
        browser.createBrowserFunction(
            new IBrowserFunction("__java_showAddAccountWizard") {
                @Override
                public Object function(Object[] arguments) {
                    dialogManager.showDialogWindow(addAccountPage);
                    return true;
                }
            });
    }
}
