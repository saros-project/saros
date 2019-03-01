package de.fu_berlin.inf.dpp.ui.browser_functions;

import de.fu_berlin.inf.dpp.HTMLUIContextFactory;
import de.fu_berlin.inf.dpp.HTMLUIStrings;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.ui.model.ValidationResult;

/** Validate if a given string is a valid {@link JID}. */
public class GetValidJID extends TypedJavascriptFunction {

  public static final String JS_NAME = "validateJid";

  /**
   * Created by PicoContainer
   *
   * @see HTMLUIContextFactory
   */
  public GetValidJID() {
    super(JS_NAME);
  }

  /**
   * Validate if a given string is a valid {@link JID}.
   *
   * @param jid the JID to validate
   * @return both the boolean result and an explanatory message (optional)
   */
  @BrowserFunction
  public ValidationResult getValidJID(String jid) {
    boolean valid = JID.isValid(new JID(jid));
    String message = "";

    if (!valid) {
      message = HTMLUIStrings.ERR_CONTACT_INVALID_JID;
    }

    return new ValidationResult(valid, message);
  }
}
