package de.fu_berlin.inf.dpp.ui.browser_functions;

import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.XMPPException;

import de.fu_berlin.inf.ag_se.browser.functions.JavascriptFunction;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.ui.core_facades.ContactListFacade;

/**
 * Encapsulates the browser functions for the add contact page.
 * 
 * A note to future developers: the browser functions do not have to be split
 * according to webpages, it just suited the current state of the prototype.
 * Instead there may be more BrowserFunction classes per page and each
 * BrowserFunction class may be used be by many pages. Split them in such a way
 * that no code duplication arises.
 */
public class ContactSpecificBrowserFunctions {

    private static final Logger LOG = Logger
        .getLogger(MainPageBrowserFunctions.class);

    private final ContactListFacade contactListFacade;

    public ContactSpecificBrowserFunctions(ContactListFacade contactListFacade) {
        this.contactListFacade = contactListFacade;
    }

    /**
     * Returns the list of browser functions encapsulated by this class. They
     * can be injected into a browser so that they can be called from
     * Javascript.
     */
    public List<JavascriptFunction> getJavascriptFunctions() {
        return Arrays.asList(new JavascriptFunction("__java_addContact") {
            @Override
            public Object function(final Object[] arguments) {

                try {
                    contactListFacade.addContact(new JID((String) arguments[0]),
                        (String) arguments[1]);
                } catch (XMPPException e) {
                    LOG.error("Error while adding contact ", e);
                    // TODO: handle exception
                }

                return null;
            }
        },

        new JavascriptFunction("__java_renameContact") {
            @Override
            public Object function(final Object[] arguments) {

                try {
                    contactListFacade.renameContact(new JID(
                        (String) arguments[0]), (String) arguments[1]);
                } catch (XMPPException e) {
                    LOG.error("Error while renaming contact ", e);
                    /*
                     * TODO: handle exception. Here we would want to call
                     * browser.run to trigger an error on the frontend, but how
                     * to do so??
                     * 
                     * Maybe use the return value to indicate errors? For
                     * example return a string with an error message and
                     * otherwise null?
                     */
                }

                return null;
            }
        },

        new JavascriptFunction("__java_deleteContact") {
            @Override
            public Object function(final Object[] arguments) {

                try {
                    contactListFacade.deleteContact(new JID(
                        (String) arguments[0]));
                } catch (XMPPException e) {
                    LOG.error("Error while deleting contact ", e);
                    // TODO
                }

                return null;
            }
        });
    }
}
