package de.fu_berlin.inf.dpp.ui.browser_functions;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.XMPPException;

import de.fu_berlin.inf.ag_se.browser.functions.JavascriptFunction;
import de.fu_berlin.inf.dpp.HTMLUIContextFactory;
import de.fu_berlin.inf.dpp.HTMLUIStrings;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.ui.JavaScriptAPI;
import de.fu_berlin.inf.dpp.ui.core_facades.StateFacade;

/**
 * Offers a via Javascript invokable method to delete a given JID from the
 * contact roster of the active account. *
 * <p>
 * JS-signature: void __java_deleteContact(String JID);"
 */
public class DeleteContact extends JavascriptFunction {
    private static final Logger LOG = Logger.getLogger(DeleteContact.class);

    private final StateFacade stateFacade;
    public static final String JS_NAME = "deleteContact";

    /**
     * Created by PicoContainer
     * 
     * @param stateFacade
     * @see HTMLUIContextFactory
     */
    public DeleteContact(StateFacade stateFacade) {
        super(NameCreator.getConventionName(JS_NAME));
        this.stateFacade = stateFacade;
    }

    @Override
    public Object function(final Object[] arguments) {
        try {
            stateFacade.deleteContact(new JID((String) arguments[0]));
        } catch (XMPPException e) {
            LOG.error("Error while deleting contact ", e);
            JavaScriptAPI.showError(browser,
                HTMLUIStrings.DELETE_CONTACT_FAILED);
        }
        return null;
    }
}
