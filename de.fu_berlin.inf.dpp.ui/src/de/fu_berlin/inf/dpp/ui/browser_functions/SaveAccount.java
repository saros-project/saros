package de.fu_berlin.inf.dpp.ui.browser_functions;

import org.apache.log4j.Logger;

import de.fu_berlin.inf.ag_se.browser.functions.JavascriptFunction;
import de.fu_berlin.inf.dpp.account.XMPPAccountStore;
import de.fu_berlin.inf.dpp.ui.core_facades.AccountStoreFacade;
import de.fu_berlin.inf.dpp.ui.model.Account;
import de.fu_berlin.inf.dpp.util.ThreadUtils;

/**
 * Offers a via Javascript invokable method to save a given JID/Password
 * combination in the {@link XMPPAccountStore}. Note that this action will
 * trigger an re-rendering for the {@link Account} model to reflect the changes
 * immediately.
 * <p>
 * JS-signature: "void __java_saveAccount(String JID, String password)".
 */
public class SaveAccount extends JavascriptFunction {
    private static final Logger LOG = Logger.getLogger(SaveAccount.class);

    private AccountStoreFacade accountStoreFacade;
    public static final String JS_NAME = "saveAccount";

    public SaveAccount(AccountStoreFacade accountStoreFacade) {
        super(NameCreator.getConventionName(JS_NAME));
        this.accountStoreFacade = accountStoreFacade;
    }

    @Override
    public Object function(Object[] arguments) {
        if (arguments.length != 2 || !(arguments[0] instanceof String)
            || !(arguments[1] instanceof String)) {
            // TODO: handle exception, signal that operation have been failed
            // to the user. Use HTMLUIStrings for all MSG, and
            // JavaScriptAPI.showMSG() in all Browserfunctions
            browser.run("alert('Please provide valid inputs');");
            return false;
        }

        final String jid = (String) arguments[0];
        final String password = (String) arguments[1];

        ThreadUtils.runSafeAsync(LOG, new Runnable() {

            @Override
            public void run() {
                try {
                    accountStoreFacade.createAccount(jid, password);
                } catch (RuntimeException e) {
                    LOG.error(
                        "Unexpected exception while creating account. As the input has been validate, this should not happen.",
                        e);
                    // TODO: handle exception, signal that operation have been
                    // failed to the user. Use HTMLUIStrings for all MSG, and
                    // JavaScriptAPI.showMSG() in all Browserfunctions
                }
            }
        });
        return null;
    }
}
