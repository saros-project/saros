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
 * Offers a via Javascript invokable method to rename a roster contact
 * (identified by the given JID) to the given new name.
 * <p>
 * JS-signature "void __java_renameContact(String JID, String newContactName)".
 */

public class RenameContact extends JavascriptFunction {
    private static final Logger LOG = Logger.getLogger(RenameContact.class);

    private final StateFacade stateFacade;
    public static final String JS_NAME = "renameContact";

    /**
     * Created by PicoContainer
     * 
     * @param stateFacade
     * @see HTMLUIContextFactory
     */
    public RenameContact(StateFacade stateFacade) {
        super(NameCreator.getConventionName(JS_NAME));
        this.stateFacade = stateFacade;
    }

    @Override
    public Object function(final Object[] arguments) {
        if (arguments.length != 2 || arguments[0] == null
            || arguments[1] == null) {
            LOG.error("Called with invalid arguments");
            JavaScriptAPI.showError(browser,
                HTMLUIStrings.RENAME_CONTACT_FAILED);
            return null;
        }
        try {
            stateFacade.renameContact(new JID((String) arguments[0]),
                (String) arguments[1]);
        } catch (XMPPException e) {
            LOG.error("Error while renaming contact ", e);
            JavaScriptAPI.showError(browser,
                HTMLUIStrings.RENAME_CONTACT_FAILED);
        }
        return null;
    }
}
