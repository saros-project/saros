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
 *
 * A note to future developers: the browser functions do not have to be split
 * according to webpages, it just suited the current state of the prototype.
 * Instead there may be more BrowserFunction classes per page and each BrowserFunction
 * class may be used be by many pages. Split them in such a way that no code duplication
 * arises.
 */
public class AddContactBrowserFunctions {

    private static final Logger LOG = Logger
        .getLogger(AddContactBrowserFunctions.class);

    private final ContactListFacade contactListFacade;

    private final DialogManager dialogManager;

    public AddContactBrowserFunctions(ContactListFacade contactListFacade,
        DialogManager dialogManager) {
        this.contactListFacade = contactListFacade;
        this.dialogManager = dialogManager;
    }

    /**
     * Returns the list of browser functions encapsulated by this class.
     * They can be injected into a browser so that they can be called from Javascript.
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
