package de.fu_berlin.inf.dpp.ui.browser_functions;

import com.google.gson.Gson;

import de.fu_berlin.inf.ag_se.browser.functions.JavascriptFunction;
import de.fu_berlin.inf.dpp.HTMLUIContextFactory;
import de.fu_berlin.inf.dpp.HTMLUIStrings;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.ui.model.ValidationResult;

/**
 * Offers a via Javascript invokable method to validate if a given string is a
 * valid {@link JID}.
 * <p>
 * JS-signature: "String __java_validateJid(String JID);" <br>
 * This return a {@link ValidationResult} Json to the caller.
 */
public class GetValidJID extends JavascriptFunction {
    public static final String JS_NAME = "validateJid";

    /**
     * Created by PicoContainer
     * 
     * @see HTMLUIContextFactory
     */
    public GetValidJID() {
        super(NameCreator.getConventionName(JS_NAME));
    }

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
}
