package de.fu_berlin.inf.dpp.ui.browser_functions;

import de.fu_berlin.inf.ag_se.browser.functions.JavascriptFunction;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.ui.core_facades.ContactListFacade;
import de.fu_berlin.inf.dpp.ui.ide_embedding.DialogManager;
import de.fu_berlin.inf.dpp.ui.webpages.AddContactPage;
import de.fu_berlin.inf.dpp.util.ThreadUtils;
import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.List;

/**
 * Encapsulates the browser functions for the add contact page.
 */
public class AddContactBrowserFunctions {

    private static final Logger LOG = Logger
        .getLogger(AddContactBrowserFunctions.class);

    private final ContactListFacade contactListFacade;

    private final DialogManager dialogManager;

    public AddContactBrowserFunctions(
        ContactListFacade contactListFacade,
        DialogManager dialogManager) {
        this.contactListFacade = contactListFacade;
        this.dialogManager = dialogManager;
    }

    /**
     * Injects Javascript functions into the HTML page. These functions
     * call Java code below when invoked.
     */
    public List<JavascriptFunction> getJavascriptFunctions() {
        return Arrays.asList(new JavascriptFunction("__java_addContact") {
                @Override
                public Object function(final Object[] arguments) {
                    ThreadUtils.runSafeAsync(LOG, new Runnable() {
                        @Override
                        public void run() {
                            contactListFacade
                                .addContact(new JID((String) arguments[0]));

                        }
                    });
                    dialogManager.closeDialogWindow(AddContactPage.WEB_PAGE);
                    return null;
                }
            },

            new JavascriptFunction("__java_cancelAddContactWizard") {
                @Override
                public Object function(Object[] arguments) {
                    dialogManager.closeDialogWindow(AddContactPage.WEB_PAGE);
                    return null;
                }
            });
    }
}
