package saros.ui.browser_functions;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.XMPPException;
import saros.HTMLUIContextFactory;
import saros.HTMLUIStrings;
import saros.net.xmpp.JID;
import saros.ui.JavaScriptAPI;
import saros.ui.core_facades.RosterFacade;

/** Add a given contact to the roster. */
public class AddContact extends TypedJavascriptFunction {

  private static final Logger LOG = Logger.getLogger(AddContact.class);

  public static final String JS_NAME = "addContact";

  private final RosterFacade rosterFacade;

  /**
   * Created by PicoContainer
   *
   * @param rosterFacade
   * @see HTMLUIContextFactory
   */
  public AddContact(RosterFacade rosterFacade) {
    super(JS_NAME);
    this.rosterFacade = rosterFacade;
  }

  /**
   * Adds contact (given by its JID) to the roster of the active user.
   *
   * <p>An error is shown to the user if this operation fails.
   *
   * @param jid The JID of the new contact
   * @param nickname How the new contact should be displayed in the roster
   */
  @BrowserFunction
  public void addContact(String jid, String nickname) {
    if (jid == null || nickname == null) {
      JavaScriptAPI.showError(
          browser, "Internal error: " + this.getName() + ". Null arguments are not allowed.");
      return;
    }

    JID newContact = new JID(jid);
    if (!(JID.isValid(newContact))) {
      JavaScriptAPI.showError(browser, "Invalid input: '" + jid + "'. Not a valid JID.");
      return;
    }

    try {
      rosterFacade.addContact(newContact, nickname);
    } catch (XMPPException e) {
      LOG.error("Error while adding contact", e);
      JavaScriptAPI.showError(browser, HTMLUIStrings.ERR_CONTACT_ADD_FAILED);
    }
  }
}
