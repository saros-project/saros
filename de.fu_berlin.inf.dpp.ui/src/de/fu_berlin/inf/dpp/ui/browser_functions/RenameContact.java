package de.fu_berlin.inf.dpp.ui.browser_functions;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.XMPPException;

import de.fu_berlin.inf.ag_se.browser.functions.JavascriptFunction;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.ui.core_facades.StateFacade;

/**
 * Offers a via Javascript invokable method to rename a roster contact
 * (identified by the given JID) to the given new name.
 * <p>
 * JS-signature "void __java_renameContact(String JID, String newContactName)".
 */

public class RenameContact extends JavascriptFunction {
    private static final Logger LOG = Logger.getLogger(RenameContact.class);

    private final StateFacade stateFacade;
    public static final String JS_NAME = "renameContact";

    public RenameContact(StateFacade stateFacade) {
        super(NameCreator.getConventionName(JS_NAME));
        this.stateFacade = stateFacade;
    }

    @Override
    public Object function(final Object[] arguments) {

        // TODO: Check arguments
        try {
            stateFacade.renameContact(new JID((String) arguments[0]),
                (String) arguments[1]);
        } catch (XMPPException e) {
            LOG.error("Error while renaming contact ", e);
            // TODO: handle exception, signal that operation have been failed
            // to the user. Use HTMLUIStrings for all MSG, and
            // JavaScriptAPI.showMSG() in all Browserfunctions
        }

        return null;
    }
}
