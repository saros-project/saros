package de.fu_berlin.inf.dpp.ui.browser_functions

import org.apache.log4j.Logger
import org.jivesoftware.smack.XMPPException
import de.fu_berlin.inf.dpp.HTMLUIContextFactory
import de.fu_berlin.inf.dpp.HTMLUIStrings
import de.fu_berlin.inf.dpp.net.xmpp.JID
import de.fu_berlin.inf.dpp.ui.JavaScriptAPI
import de.fu_berlin.inf.dpp.ui.core_facades.RosterFacade

/**
 * Rename a roster contact.
 * Created by PicoContainer
 * @param rosterFacade
 * @see HTMLUIContextFactory
 */
class RenameContact (rosterFacade: RosterFacade?) : TypedJavaScriptFunction(JS_NAME) {
	
	private val rosterFacade: RosterFacade? = rosterFacade

	/**
	 * Rename a contact (identified by the given JID) in the roster of the
	 * active account to the given new name.
	 * <p>
	 * An error is shown to the user if this operation fails.
	 *
	 * @param jid
	 * the contact to be renamed
	 * @param newNickname
	 * the new way this contact should be displayed
	 */
	@BrowserFunction
	fun renameContact(jid: String?, newNickname: String?) {
		if (jid == null || newNickname == null) {
			JavaScriptAPI.showError(
				browser,
				("Internal error: " + this.getName()
						+ ". Null arguments are not allowed.")
			)
			return
		}
		try {
			rosterFacade!!.renameContact(JID(jid), newNickname)
		} catch (e: XMPPException) {
			LOG!!.error("Error while renaming contact", e)
			JavaScriptAPI.showError(
				browser,
				HTMLUIStrings.ERR_CONTACT_RENAME_FAILED
			)
		}
	}

	companion object {
		private val LOG = Logger.getLogger(RenameContact::class.java)
		val JS_NAME = "renameContact"
	}
}