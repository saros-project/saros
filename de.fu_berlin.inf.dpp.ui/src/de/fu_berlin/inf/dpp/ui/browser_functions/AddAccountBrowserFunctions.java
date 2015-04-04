package de.fu_berlin.inf.dpp.ui.browser_functions;

import com.google.gson.Gson;
import de.fu_berlin.inf.ag_se.browser.functions.JavascriptFunction;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.ui.core_services.AccountCoreService;
import de.fu_berlin.inf.dpp.ui.ide_embedding.DialogManager;
import de.fu_berlin.inf.dpp.ui.model.ValidationResult;
import de.fu_berlin.inf.dpp.ui.webpages.AddAccountPage;
import de.fu_berlin.inf.dpp.util.ThreadUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.List;

/**
 * Encapsulates the browser functions for the add account page.
 */
public class AddAccountBrowserFunctions {

    private static final Logger LOG = Logger
        .getLogger(AddAccountBrowserFunctions.class);

    private final DialogManager dialogManager;

    private final AccountCoreService accountCoreService;

    public AddAccountBrowserFunctions(DialogManager dialogManager,
        AccountCoreService accountCoreService) {
        this.dialogManager = dialogManager;
        this.accountCoreService = accountCoreService;
    }

    /**
     * Injects Javascript functions into the HTML page. These functions
     * call Java code below when invoked.
     */
    public List<JavascriptFunction> getJavascriptFunctions() {
        return Arrays
            .asList(new JavascriptFunction("__java_cancelAddAccountWizard") {
                        @Override
                        public Object function(Object[] arguments) {
                            dialogManager
                                .closeDialogWindow(AddAccountPage.WEB_PAGE);
                            return true;
                        }
                    }, new JavascriptFunction("__java_validateJID") {
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
                                    validationResult = new ValidationResult(
                                        false,
                                        "Invalid JID: username must not be empty.");
                                } else if (StringUtils.isBlank(domain)) {
                                    validationResult = new ValidationResult(
                                        false,
                                        "Invalid JID: domain must not be empty.");
                                } else if (!domain.toLowerCase()
                                    .equals(domain)) {
                                    validationResult = new ValidationResult(
                                        false,
                                        "Invalid JID: domain must not contain upper case letters.");
                                } else if (accountCoreService
                                    .existsAccount(jid)) {
                                    validationResult = new ValidationResult(
                                        false, "Account already exists.");
                                } else {
                                    validationResult = new ValidationResult(
                                        true, "");
                                }
                            }

                            Gson gson = new Gson();
                            return gson.toJson(validationResult);
                        }
                    },

                new JavascriptFunction("__java_saveAccount") {
                    @Override
                    public Object function(Object[] arguments) {
                        if (arguments.length != 2
                            || !(arguments[0] instanceof String)
                            || !(arguments[1] instanceof String)) {
                            browser
                                .run("alert('Please provide valid inputs');");
                            return false;
                        }

                        final String jid = (String) arguments[0];
                        final String password = (String) arguments[1];

                        ThreadUtils.runSafeAsync(LOG, new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    accountCoreService
                                        .createAccount(jid, password);
                                    dialogManager.closeDialogWindow(
                                        AddAccountPage.WEB_PAGE);
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
