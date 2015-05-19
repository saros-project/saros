package de.fu_berlin.inf.dpp.ui.renderer;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.google.gson.Gson;

import de.fu_berlin.inf.ag_se.browser.extensions.IJQueryBrowser;
import de.fu_berlin.inf.dpp.account.XMPPAccount;
import de.fu_berlin.inf.dpp.account.XMPPAccountStore;
import de.fu_berlin.inf.dpp.ui.model.Account;

/**
 * This class is responsible for sending the account list to the HTML UI.
 * 
 * As changes to the state of the account store are currenty not pushed via
 * listeners, this class actively queries the state on each render request from
 * the {@link XMPPAccountStore}.
 * 
 * As changes to the state of the account store are currenty not pushed via
 * listeners, this class actively queries the state on each render request from
 * the {@link XMPPAccountStore}.
 */
public class AccountRenderer extends Renderer {

    private static final Logger LOG = Logger.getLogger(AccountRenderer.class);

    private final XMPPAccountStore accountStore;

    public AccountRenderer(XMPPAccountStore accountStore) {
        this.accountStore = accountStore;
    }

    @Override
    public synchronized void render(IJQueryBrowser browser) {
        Gson gson = new Gson();
        String accountString = gson.toJson(getAccountList());
        LOG.debug("sending json: " + accountString);
        browser.run("Saros.setAccountList(" + accountString + ")");

    }

    private List<Account> getAccountList() {
        ArrayList<Account> res = new ArrayList<Account>();
        for (XMPPAccount xmppAccount : accountStore.getAllAccounts()) {
            res.add(new Account(xmppAccount.getUsername(), xmppAccount
                .getDomain()));
        }
        return res;
    }
}
