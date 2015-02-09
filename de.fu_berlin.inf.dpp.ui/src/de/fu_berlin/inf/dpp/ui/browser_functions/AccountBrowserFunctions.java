package de.fu_berlin.inf.dpp.ui.browser_functions;

import de.fu_berlin.inf.ag_se.browser.IBrowserFunction;
import de.fu_berlin.inf.ag_se.browser.extensions.IJQueryBrowser;
import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.ui.core_services.AccountCoreService;
import de.fu_berlin.inf.dpp.ui.manager.IDialogManager;
import de.fu_berlin.inf.dpp.ui.view_parts.AddAccountWizard;
import de.fu_berlin.inf.dpp.util.ThreadUtils;
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
    private AccountCoreService accountCoreService;

    @Inject
    private AddAccountWizard addAccountWizard;

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
                    dialogManager.showDialogWindow(addAccountWizard);
                    return null;
                }
            });

        browser.createBrowserFunction(
            new IBrowserFunction("__java_cancelAddAccountWizard") {
                @Override
                public Object function(Object[] arguments) {
                    dialogManager.closeDialogWindow(addAccountWizard);
                    return null;
                }
            });

        browser
            .createBrowserFunction(new IBrowserFunction("__java_saveAccount") {
                @Override
                public Object function(final Object[] arguments) {
                    if (arguments.length <= 0) {
                        browser.run("alert('Please provide valid inputs'");
                        return null;
                    }

                    //TODO use JSON object as parameter
                    final String jid = (String) arguments[0];
                    if (jid.matches(".+@.+")) {
                        ThreadUtils.runSafeAsync(LOG, new Runnable() {
                                @Override
                                public void run() {
                                    accountCoreService.createAccount(jid, (String) arguments[1]); }
                            });
                        dialogManager.closeDialogWindow(addAccountWizard);
                        return true;
                    } else {
                        browser.run("alert('Invalid jid: \'" + jid + "\'')");
                        return false;
                    }
                }
            });
    }
}
