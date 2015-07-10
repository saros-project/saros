package de.fu_berlin.inf.dpp.ui.browser_functions;

import java.util.Arrays;
import java.util.List;

import com.google.gson.Gson;
import de.fu_berlin.inf.dpp.HTMLUIStrings;
import de.fu_berlin.inf.dpp.ui.model.ValidationResult;
import org.apache.log4j.Logger;
import org.jivesoftware.smack.XMPPException;

import de.fu_berlin.inf.ag_se.browser.functions.JavascriptFunction;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.ui.core_facades.StateFacade;

/**
 * Encapsulates the contact specific browser functions, for example to add,
 * rename or delete a contact.
 */
public class ContactSpecificBrowserFunctions {

    private static final Logger LOG = Logger
        .getLogger(ContactSpecificBrowserFunctions.class);

    private final StateFacade stateFacade;

    public ContactSpecificBrowserFunctions(StateFacade stateFacade) {
        this.stateFacade = stateFacade;
    }

    /**
     * Returns the list of browser functions encapsulated by this class. They
     * can be injected into a browser so that they can be called from
     * Javascript.
     */
    public List<JavascriptFunction> getJavascriptFunctions() {
        return Arrays.asList(
            new JavascriptFunction("__java_validateJid") {
                @Override
                public Object function(final Object[] arguments) {

                    boolean valid = JID.isValid(new JID((String) arguments[0]));
                    String message = "";

                    if (!valid) {
                        message = HTMLUIStrings.INVALID_JID;
                    }

                    ValidationResult result = new ValidationResult(valid, message);
                    Gson gson = new Gson();

                    return gson.toJson(result);
                }
            },
            
            new JavascriptFunction("__java_addContact") {
                @Override
                public Object function(final Object[] arguments) {

                try {
                    stateFacade.addContact(new JID((String) arguments[0]),
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
                    stateFacade.renameContact(new JID(
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
                    stateFacade.deleteContact(new JID(
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
