package de.fu_berlin.inf.dpp.ui.browser_functions

import de.fu_berlin.inf.dpp.HTMLUIContextFactory
import de.fu_berlin.inf.dpp.HTMLUIStrings
import de.fu_berlin.inf.dpp.net.xmpp.JID
import de.fu_berlin.inf.dpp.ui.model.ValidationResult

/**
 * Validate if a given string is a valid {@link JID}.
 */
/**
 * Created by PicoContainer
 *
 * @see HTMLUIContextFactory
 */
class GetValidJID : TypedJavaScriptFunction(JS_NAME) {
	
	/**
	 * Validate if a given string is a valid {@link JID}.
	 *
	 * @param jid
	 * the JID to validate
	 * @return both the boolean result and an explanatory message (optional)
	 */
	@BrowserFunction
	fun getValidJID(jid: String?): ValidationResult? {
		val valid = JID.isValid(JID(jid))
		var message = ""
		if (!valid) {
			message = HTMLUIStrings.ERR_CONTACT_INVALID_JID
		}
		return ValidationResult(valid, message)
	}

	companion object {
		val JS_NAME = "validateJid"
	}
}