package de.fu_berlin.inf.dpp.ui.browser_functions;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.XMPPException;

import de.fu_berlin.inf.dpp.HTMLUIContextFactory;
import de.fu_berlin.inf.dpp.HTMLUIStrings;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.ui.JavaScriptAPI;
import de.fu_berlin.inf.dpp.ui.core_facades.StateFacade;

/**
 * Delete a contact (given by its JID) from the roster of the active account.
 */
public class DeleteContact extends TypedJavascriptFunction {

    private static final Logger LOG = Logger.getLogger(DeleteContact.class);

    public static final String JS_NAME = "deleteContact";

    private final StateFacade stateFacade;

    /**
     * Created by PicoContainer
     * 
     * @param stateFacade
     * @see HTMLUIContextFactory
     */
    public DeleteContact(StateFacade stateFacade) {
        super(JS_NAME);
        this.stateFacade = stateFacade;
    }

    /**
     * Delete a contact (given by its JID) from the roster of the active
     * account.
     * <p>
     * An error is show to the user if this operation fails.
     * 
     * @param jid
     *            the contact to remove from the roster
     */
    @BrowserFunction
    public void deleteContact(String jid) {
        if (jid == null) {
            JavaScriptAPI.showError(browser,
                "Internal error: " + this.getName()
                    + ". Null arguments are not allowed.");
            return;
        }

        try {
            stateFacade.deleteContact(new JID(jid));
        } catch (XMPPException e) {
            LOG.error("Error while deleting contact", e);
            JavaScriptAPI.showError(browser,
                HTMLUIStrings.DELETE_CONTACT_FAILED);
        }
    }
}
