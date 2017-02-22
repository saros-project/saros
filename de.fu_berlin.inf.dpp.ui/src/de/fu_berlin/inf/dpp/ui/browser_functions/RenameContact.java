package de.fu_berlin.inf.dpp.ui.browser_functions;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.XMPPException;

import de.fu_berlin.inf.dpp.HTMLUIContextFactory;
import de.fu_berlin.inf.dpp.HTMLUIStrings;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.ui.JavaScriptAPI;
import de.fu_berlin.inf.dpp.ui.core_facades.StateFacade;

/**
 * Rename a roster contact.
 */
public class RenameContact extends TypedJavascriptFunction {

    private static final Logger LOG = Logger.getLogger(RenameContact.class);

    public static final String JS_NAME = "renameContact";

    private final StateFacade stateFacade;

    /**
     * Created by PicoContainer
     * 
     * @param stateFacade
     * @see HTMLUIContextFactory
     */
    public RenameContact(StateFacade stateFacade) {
        super(JS_NAME);
        this.stateFacade = stateFacade;
    }

    /**
     * Rename a contact (identified by the given JID) in the roster of the
     * active account to the given new name.
     * <p>
     * An error is shown to the user if this operation fails.
     * 
     * @param jid
     *            the contact to be renamed
     * @param newNickname
     *            the new way this contact should be displayed
     */
    @BrowserFunction
    public void renameContact(String jid, String newNickname) {
        if (jid == null || newNickname == null) {
            JavaScriptAPI.showError(browser,
                "Internal error: " + this.getName()
                    + ". Null arguments are not allowed.");
            return;
        }

        try {
            stateFacade.renameContact(new JID(jid), newNickname);
        } catch (XMPPException e) {
            LOG.error("Error while renaming contact", e);
            JavaScriptAPI.showError(browser,
                HTMLUIStrings.ERR_CONTACT_RENAME_FAILED);
        }

    }
}
