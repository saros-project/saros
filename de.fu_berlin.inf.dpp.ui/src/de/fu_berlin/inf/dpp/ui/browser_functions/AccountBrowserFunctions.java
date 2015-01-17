package de.fu_berlin.inf.dpp.ui.browser_functions;

import com.google.gson.Gson;
import de.fu_berlin.inf.dpp.account.XMPPAccount;
import de.fu_berlin.inf.dpp.account.XMPPAccountStore;
import de.fu_berlin.inf.dpp.ui.manager.IDialogManager;
import de.fu_berlin.inf.dpp.ui.model.Account;
import de.fu_berlin.inf.dpp.util.ComponentLookup;
import org.apache.log4j.Logger;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.BrowserFunction;
import org.eclipse.swt.widgets.Display;

import java.util.ArrayList;
import java.util.List;

/**
 * This class contains functions to be called from Javascript.
 * It contains only the so called browser functions concerning the account
 * management.
 */
public class AccountBrowserFunctions {

    private static final Logger LOG = Logger
        .getLogger(AccountBrowserFunctions.class);

    private final IDialogManager dialogManager;

    private final XMPPAccountStore accountStore;

    private final Browser browser;

    public AccountBrowserFunctions(Browser browser) {
        dialogManager = ComponentLookup.getDialogManager();
        accountStore = ComponentLookup.getAccountStore();
        this.browser = browser;
    }

    /**
     * Injects Javascript functions into the HTML page. These functions
     * call Java code below when invoked.
     */
    public void createJavascriptFunctions() {
        new BrowserFunction(browser, "__java_showAddAccountWizard") {
            @Override
            public Object function(Object[] arguments) {
                dialogManager.showDialogWindow(
                    ComponentLookup.getAddAccountWizard());
                return null;
            }
        };

        new BrowserFunction(browser, "__java_cancelAddAccountWizard") {
            @Override
            public Object function(Object[] arguments) {
                dialogManager.closeDialogWindow(
                    ComponentLookup.getAddAccountWizard());
                return null;
            }
        };

        new BrowserFunction(browser, "__java_saveAccount") {
            @Override
            public Object function(Object[] arguments) {
                if (arguments.length > 0) {
                    //TODO use JSON object as parameter
                    String jid = (String) arguments[0];
                    if (jid.matches(".+@.+")) {
                        String[] pair = jid.split("@");
                        accountStore
                            .createAccount(pair[0], (String) arguments[1],
                                pair[1], "", 0, true, true);
                    } else {
                        //TODO notify user
                    }
                }
                dialogManager.closeDialogWindow(
                    ComponentLookup.getAddAccountWizard());
                return null;
            }
        };

        //TODO should be executed after site is loaded
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                if (!browser.isDisposed()) {
                    LOG.debug("sending json: " + allAcountsToJson());
                    browser.execute(
                        "__angular_setAccountList(" + allAcountsToJson() + ")");
                }
            }
        });
    }

    private String allAcountsToJson() {
        List<XMPPAccount> allAccounts = accountStore.getAllAccounts();
        ArrayList<Account> accounts = new ArrayList<Account>();
        for (XMPPAccount account : allAccounts) {
            accounts.add(
                new Account(account.getUsername(), account.getDomain()));
        }
        Gson gson = new Gson();
        return gson.toJson(accounts);
    }
}
