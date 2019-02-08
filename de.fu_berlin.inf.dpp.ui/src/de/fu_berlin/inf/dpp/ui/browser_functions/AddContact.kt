package de.fu_berlin.inf.dpp.ui.browser_functions

import org.apache.log4j.Logger
import org.jivesoftware.smack.XMPPException
import de.fu_berlin.inf.dpp.HTMLUIContextFactory
import de.fu_berlin.inf.dpp.HTMLUIStrings
import de.fu_berlin.inf.dpp.net.xmpp.JID
import de.fu_berlin.inf.dpp.ui.JavaScriptAPI
import de.fu_berlin.inf.dpp.ui.core_facades.RosterFacade

/**
 * Add a given contact to the roster.
 */
class AddContact
/**
 * Created by PicoContainer
 *
 * @param rosterFacade
 * @see HTMLUIContextFactory
 */
	(rosterFacade: RosterFacade?) : TypedJavaScriptFunction(JS_NAME) {
	private val rosterFacade: RosterFacade? = rosterFacade

	/**
	 * Adds contact (given by its JID) to the roster of the active user.
	 * <p>
	 * An error is shown to the user if this operation fails.
	 *
	 * @param jid
	 * The JID of the new contact
	 * @param nickname
	 * How the new contact should be displayed in the roster
	 */
	@BrowserFunction
	fun addContact(jid: String?, nickname: String?) {
		if (jid == null || nickname == null) {
			JavaScriptAPI.showError(
				browser,
				("Internal error: " + this.getName()
						+ ". Null arguments are not allowed.")
			)
			return
		}
		val newContact = JID(jid)
		if (!(JID.isValid(newContact))) {
			JavaScriptAPI.showError(
				browser, ("Invalid input: '" + jid
						+ "'. Not a valid JID.")
			)
			return
		}
		try {
			rosterFacade!!.addContact(newContact, nickname)
		} catch (e: XMPPException) {
			LOG!!.error("Error while adding contact", e)
			JavaScriptAPI.showError(
				browser,
				HTMLUIStrings.ERR_CONTACT_ADD_FAILED
			)
		}
	}

	companion object {
		private val LOG = Logger.getLogger(AddContact::class.java)
		val JS_NAME = "addContact"
	}
}