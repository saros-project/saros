/*
 *
 *  DPP - Serious Distributed Pair Programming
 *  (c) Freie Universität Berlin - Fachbereich Mathematik und Informatik - 2010
 *  (c) NFQ (www.nfq.com) - 2014
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 1, or (at your option)
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 * /
 */

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

    private final IDialogManager dialogCreator;

    private final XMPPAccountStore accountStore;

    private final Browser browser;
    private String addAccountPage = "/html/add-user-wizard.html";

    public AccountBrowserFunctions(Browser browser) {
        dialogCreator = ComponentLookup.getDialogManager();
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
                dialogCreator.showDialogWindow(addAccountPage);
                return null;
            }
        };

        new BrowserFunction(browser, "__java_cancelAddAccountWizard") {
            @Override
            public Object function(Object[] arguments) {
                dialogCreator.closeDialogWindow(addAccountPage);
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
                dialogCreator.closeDialogWindow(addAccountPage);
                return null;
            }
        };

        //TODO should be executed after site is loaded
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                LOG.debug("sending json: " + allAcountsToJson());
                browser.execute(
                    "__angular_setAccountList(" + allAcountsToJson() + ")");
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
