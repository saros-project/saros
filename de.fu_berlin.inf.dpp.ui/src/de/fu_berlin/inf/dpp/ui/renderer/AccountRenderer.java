package de.fu_berlin.inf.dpp.ui.renderer;

import com.google.gson.Gson;
import de.fu_berlin.inf.ag_se.browser.extensions.IJQueryBrowser;
import de.fu_berlin.inf.dpp.ui.model.Account;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * This class is responsible for sending the account list to the HTML UI.
 * It saves the current account list to be able to re-render it when the browser
 * changes.
 */
public class AccountRenderer {

    private static final Logger LOG = Logger.getLogger(AccountRenderer.class);

    private IJQueryBrowser browser;

    /**
     * @param browser the browser used for the rendering
     */
    public AccountRenderer(IJQueryBrowser browser) {
        this.browser = browser;
    }

    /**
     * Displays the given account list in the browser.
     * May be called from both UI and non-UI thread
     *
     * @param accountList the list of accounts to display
     */
    public void renderAccountList(List<Account> accountList) {
        String accountString = allAcountsToJson(accountList);
        LOG.debug("sending json: " + accountString);
        browser.run("__angular_setAccountList(" + accountString + ")");
    }

    private String allAcountsToJson(List<Account> accountList) {
        Gson gson = new Gson();
        return gson.toJson(accountList);
    }
}
