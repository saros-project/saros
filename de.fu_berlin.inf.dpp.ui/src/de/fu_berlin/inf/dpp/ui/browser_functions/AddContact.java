package de.fu_berlin.inf.dpp.ui.browser_functions;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.XMPPException;

import de.fu_berlin.inf.ag_se.browser.functions.JavascriptFunction;
import de.fu_berlin.inf.dpp.HTMLUIStrings;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.ui.JavaScriptAPI;
import de.fu_berlin.inf.dpp.ui.core_facades.StateFacade;

/**
 * Offers a via Javascript invokable method to add a given contact to the
 * roster.
 * <p>
 * JS-signature: "void __java_addContact(String JID, String nickname);"
 */
public class AddContact extends JavascriptFunction {
    private static final Logger LOG = Logger.getLogger(AddContact.class);
    public static final String JS_NAME = "addContact";

    private final StateFacade stateFacade;

    public AddContact(StateFacade stateFacade) {
        super(NameCreator.getConventionName(JS_NAME));
        this.stateFacade = stateFacade;
    }

    @Override
    public Object function(final Object[] arguments) {
        try {
            stateFacade.addContact(new JID((String) arguments[0]),
                (String) arguments[1]);
        } catch (XMPPException e) {
            LOG.error("Error while adding contact ", e);
            JavaScriptAPI.showError(browser, HTMLUIStrings.ADD_CONTACT_FAILED);
        }
        return null;
    }
}
