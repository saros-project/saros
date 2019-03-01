package saros.ui.browser_functions;

import saros.HTMLUIContextFactory;
import saros.HTMLUIStrings;
import saros.net.xmpp.JID;
import saros.ui.model.ValidationResult;

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
