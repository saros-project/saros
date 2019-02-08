package de.fu_berlin.inf.dpp.ui.browser_functions

import org.apache.log4j.Logger
import org.jivesoftware.smack.XMPPException
import de.fu_berlin.inf.dpp.HTMLUIContextFactory
import de.fu_berlin.inf.dpp.HTMLUIStrings
import de.fu_berlin.inf.dpp.net.xmpp.JID
import de.fu_berlin.inf.dpp.ui.JavaScriptAPI
import de.fu_berlin.inf.dpp.ui.core_facades.RosterFacade

/**
 * Delete a contact (given by its JID) from the roster of the active account.
 *
 *
 * Created by PicoContainer
 *
 * @param rosterFacade
 * @see HTMLUIContextFactory
 */
class DeleteContact (rosterFacade: RosterFacade?) : TypedJavaScriptFunction(JS_NAME) {
	private val rosterFacade: RosterFacade? = rosterFacade

	/**
	 * Delete a contact (given by its JID) from the roster of the active
	 * account.
	 * <p>
	 * An error is show to the user if this operation fails.
	 *
	 * @param jid
	 * the contact to remove from the roster
	 */
	@BrowserFunction
	fun deleteContact(jid: String?) {
		if (jid == null) {
			JavaScriptAPI.showError(
				browser,
				("Internal error: " + this.getName()
						+ ". Null arguments are not allowed.")
			)
			return
		}
		try {
			rosterFacade!!.deleteContact(JID(jid))
		} catch (e: XMPPException) {
			LOG!!.error("Error while deleting contact", e)
			JavaScriptAPI.showError(
				browser,
				HTMLUIStrings.ERR_CONTACT_DELETE_FAILED
			)
		}
	}

	companion object {
		private val LOG = Logger.getLogger(DeleteContact::class.java)
		val JS_NAME = "deleteContact"
	}
}