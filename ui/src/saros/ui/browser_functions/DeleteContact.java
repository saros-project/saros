package saros.ui.browser_functions;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.XMPPException;
import saros.HTMLUIContextFactory;
import saros.HTMLUIStrings;
import saros.net.xmpp.JID;
import saros.ui.JavaScriptAPI;
import saros.ui.core_facades.RosterFacade;

/** Delete a contact (given by its JID) from the roster of the active account. */
public class DeleteContact extends TypedJavascriptFunction {

  private static final Logger LOG = Logger.getLogger(DeleteContact.class);

  public static final String JS_NAME = "deleteContact";

  private final RosterFacade rosterFacade;

  /**
   * Created by PicoContainer
   *
   * @param rosterFacade
   * @see HTMLUIContextFactory
   */
  public DeleteContact(RosterFacade rosterFacade) {
    super(JS_NAME);
    this.rosterFacade = rosterFacade;
  }

  /**
   * Delete a contact (given by its JID) from the roster of the active account.
   *
   * <p>An error is show to the user if this operation fails.
   *
   * @param jid the contact to remove from the roster
   */
  @BrowserFunction
  public void deleteContact(String jid) {
    if (jid == null) {
      JavaScriptAPI.showError(
          browser, "Internal error: " + this.getName() + ". Null arguments are not allowed.");
      return;
    }

    try {
      rosterFacade.deleteContact(new JID(jid));
    } catch (XMPPException e) {
      LOG.error("Error while deleting contact", e);
      JavaScriptAPI.showError(browser, HTMLUIStrings.ERR_CONTACT_DELETE_FAILED);
    }
  }
}
