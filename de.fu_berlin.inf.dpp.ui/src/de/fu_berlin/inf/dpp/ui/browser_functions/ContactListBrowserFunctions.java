package de.fu_berlin.inf.dpp.ui.browser_functions;

import com.google.gson.Gson;
import de.fu_berlin.inf.ag_se.browser.IBrowserFunction;
import de.fu_berlin.inf.ag_se.browser.extensions.IJQueryBrowser;
import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.ui.core_services.ContactListCoreService;
import de.fu_berlin.inf.dpp.ui.manager.IDialogManager;
import de.fu_berlin.inf.dpp.ui.model.Account;
import de.fu_berlin.inf.dpp.ui.view_parts.AddContactPage;
import de.fu_berlin.inf.dpp.util.ThreadUtils;
import org.apache.log4j.Logger;
import org.picocontainer.annotations.Inject;
import org.jivesoftware.smack.XMPPException;

/**
 * This class implements the functions to be called by Javascript code for
 * the contact list. These are the callback functions to invoke Java code from
 * Javascript.
 */
public class ContactListBrowserFunctions {

    private static final Logger LOG = Logger
        .getLogger(ContactListBrowserFunctions.class);

    @Inject
    private ContactListCoreService contactListCoreService;

    @Inject
    private IDialogManager dialogManager;

    @Inject
    private AddContactPage addContactPage;

    private IJQueryBrowser browser;

    /**
     * @param browser                the SWT browser in which the functions should be injected
     */
    public ContactListBrowserFunctions(IJQueryBrowser browser) {
        SarosPluginContext.initComponent(this);
        this.browser = browser;
    }

    /**
     * Injects Javascript functions into the HTML page. These functions
     * call Java code below when invoked.
     */
    public void createJavascriptFunctions() {
        //TODO remember to disable button in HTML while connecting
        browser.createBrowserFunction(new IBrowserFunction("__java_connect") {
            @Override
            public Object function(Object[] arguments) {
                if (arguments.length > 0 && arguments[0] != null) {
                    Gson gson = new Gson();
                    final Account account = gson
                        .fromJson((String) arguments[0], Account.class);
                    ThreadUtils.runSafeAsync(LOG, new Runnable() {
                        @Override
                        public void run() {
                            contactListCoreService.connect(account);
                        }
                    });
                } else {
                    LOG.error("Connect was called without an account.");
                    browser.run(
                        "alert('Cannot connect because no account was given.');");
                }
                return null;
            }
        });
        browser
            .createBrowserFunction(new IBrowserFunction("__java_disconnect") {
                @Override
                public Object function(Object[] arguments) {
                    ThreadUtils.runSafeAsync(LOG, new Runnable() {
                        @Override
                        public void run() {
                            contactListCoreService.disconnect();
                        }
                    });
                    return null;
                }
            });

        browser.createBrowserFunction(
            new IBrowserFunction("__java_deleteContact") {
                @Override
                public Object function(final Object[] arguments) {
                    ThreadUtils.runSafeAsync(LOG, new Runnable() {
                        @Override
                        public void run() {
                            try {
                                contactListCoreService.deleteContact(
                                    new JID((String) arguments[0]));
                            } catch (XMPPException e) {
                                LOG.error("Error deleting contact ", e);
                                browser.run("alert('Error deleting contact');");
                            }
                        }
                    });
                    return null;
                }
            });

        browser.createBrowserFunction(
            new IBrowserFunction("__java_showAddContactWizard") {
                @Override
                public Object function(Object[] arguments) {
                    dialogManager.showDialogWindow(addContactPage);
                    return null;
                }
            });
    }
}
