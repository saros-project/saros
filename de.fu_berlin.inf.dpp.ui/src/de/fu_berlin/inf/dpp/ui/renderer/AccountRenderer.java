package de.fu_berlin.inf.dpp.ui.renderer;

import com.google.gson.Gson;
import de.fu_berlin.inf.ag_se.browser.extensions.IJQueryBrowser;
import de.fu_berlin.inf.dpp.account.XMPPAccount;
import de.fu_berlin.inf.dpp.account.XMPPAccountStore;
import de.fu_berlin.inf.dpp.ui.manager.BrowserManager;
import de.fu_berlin.inf.dpp.ui.model.Account;
import net.jcip.annotations.GuardedBy;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class is responsible for sending the account list to the HTML UI.
 * It saves the current account list to be able to re-render it when the browser
 * changes.
 */
public class AccountRenderer {

    private static final Logger LOG = Logger.getLogger(AccountRenderer.class);

    private final BrowserManager browserManager;

    private final XMPPAccountStore accountStore;

    public AccountRenderer(BrowserManager browserManager,
        XMPPAccountStore accountStore) {
        this.browserManager = browserManager;
        this.accountStore = accountStore;
    }

    /**
     * Displays the current state in the browser.
     */
    public synchronized void render() {
        Gson gson = new Gson();
        String accountString = gson.toJson(getAccountList());
        LOG.debug("sending json: " + accountString);
        IJQueryBrowser browser = browserManager.getMainViewBrowser();
        if (browser != null) {
            browser.run("__angular_setAccountList(" + accountString + ")");
        }
    }

    /**
     * May be called from both UI and non-UI thread.
     */
    private List<Account> getAccountList() {
        ArrayList<Account> res = new ArrayList<Account>();
        for (XMPPAccount xmppAccount : accountStore.getAllAccounts()) {
            res.add(new Account(xmppAccount.getUsername(),
                xmppAccount.getDomain()));
        }
        return res;
    }
}
