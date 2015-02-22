package de.fu_berlin.inf.dpp.ui.browser_functions;

import com.google.gson.Gson;
import de.fu_berlin.inf.ag_se.browser.IBrowserFunction;
import de.fu_berlin.inf.ag_se.browser.extensions.IJQueryBrowser;
import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.ui.core_services.AccountCoreService;
import de.fu_berlin.inf.dpp.ui.manager.IDialogManager;
import de.fu_berlin.inf.dpp.ui.model.ValidationResult;
import de.fu_berlin.inf.dpp.ui.view_parts.AddAccountWizard;
import de.fu_berlin.inf.dpp.util.ThreadUtils;
import org.apache.commons.lang.StringUtils;
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
                    return true;
                }
            });

        browser.createBrowserFunction(
            new IBrowserFunction("__java_cancelAddAccountWizard") {
                @Override
                public Object function(Object[] arguments) {
                    dialogManager.closeDialogWindow(addAccountWizard);
                    return true;
                }
            });

        browser
            .createBrowserFunction(new IBrowserFunction("__java_validateJID") {
                @Override
                public Object function(Object[] arguments) {
                    ValidationResult validationResult;
                    if (arguments.length != 1
                        || !(arguments[0] instanceof String)) {
                        validationResult = new ValidationResult(false,
                            "JID must not be empty");
                    } else {

                        final String jid = (String) arguments[0];
                        JID jid1 = new JID(jid);
                        String username = jid1.getName();
                        String domain = jid1.getDomain();

                        if (StringUtils.isBlank(username)) {
                            validationResult = new ValidationResult(false,
                                "Invalid JID: username must not be empty.");
                        } else if (StringUtils.isBlank(domain)) {
                            validationResult = new ValidationResult(false,
                                "Invalid JID: domain must not be empty.");
                        } else if (!domain.toLowerCase().equals(domain)) {
                            validationResult = new ValidationResult(false,
                                "Invalid JID: domain must not contain upper case letters.");
                        } else if (accountCoreService.existsAccount(jid)) {
                            validationResult = new ValidationResult(false,
                                "Account already exists.");
                        } else {
                            validationResult = new ValidationResult(true, "");
                        }
                    }

                    Gson gson = new Gson();
                    return gson.toJson(validationResult);
                }
            });

        browser
            .createBrowserFunction(new IBrowserFunction("__java_saveAccount") {
                @Override
                public Object function(Object[] arguments) {
                    if (arguments.length != 2
                        || !(arguments[0] instanceof String)
                        || !(arguments[1] instanceof String)) {
                        browser.run("alert('Please provide valid inputs');");
                        return false;
                    }

                    final String jid = (String) arguments[0];
                    final String password = (String) arguments[1];

                    ThreadUtils.runSafeAsync(LOG, new Runnable() {
                        @Override
                        public void run() {
                            try {
                                accountCoreService.createAccount(jid, password);
                                dialogManager.closeDialogWindow(addAccountWizard);
                            } catch (RuntimeException e) {
                                LOG.error(
                                    "Unexpected exception while creating account. As the input has been validate, this should not happen.",
                                    e);
                                browser.run(
                                    "alert('An error occurred while saving the account.');");
                            }
                        }
                    });

                    return null;
                }
            });
    }
}
