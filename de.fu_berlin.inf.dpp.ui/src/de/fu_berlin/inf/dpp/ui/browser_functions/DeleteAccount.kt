package de.fu_berlin.inf.dpp.ui.browser_functions

import org.apache.log4j.Logger
import de.fu_berlin.inf.dpp.HTMLUIContextFactory
import de.fu_berlin.inf.dpp.HTMLUIStrings
import de.fu_berlin.inf.dpp.account.XMPPAccount
import de.fu_berlin.inf.dpp.account.XMPPAccountStore
import de.fu_berlin.inf.dpp.ui.JavaScriptAPI

/**
 * Delete an existing account from the account store. The currently active
 * account cannot be deleted.
 */
class DeleteAccount
/**
 * Created by PicoContainer
 *
 * @param accountStore
 * to redirect the delete action
 * @see HTMLUIContextFactory
 */
	(accountStore: XMPPAccountStore?) : TypedJavaScriptFunction(JS_NAME) {
	private val accountStore: XMPPAccountStore? = accountStore

	/**
	 * Delete a given {@link XMPPAccount} from the {@link XMPPAccountStore}. The
	 * active account cannot be deleted.
	 * <p>
	 * If this operation fails, an error is shown to the user.
	 *
	 * @param account
	 * to be deleted
	 */
	@BrowserFunction
	fun deleteAccount(account: XMPPAccount?) {
		try {
			accountStore!!.deleteAccount(account)
		} catch (e: IllegalStateException) {
			LOG!!.warn("Couldn't delete active account: " + e.message, e)
			JavaScriptAPI.showError(
				browser,
				HTMLUIStrings.ERR_ACCOUNT_DELETE_ACTIVE
			)
		} catch (e: IllegalArgumentException) {
			LOG!!.error("Couldn't delete account: " + e.message, e)
			// FIXME Misleading error message
			JavaScriptAPI.showError(
				browser,
				HTMLUIStrings.ERR_ACCOUNT_DELETE_ACTIVE
			)
		}
	}

	companion object {
		private val LOG = Logger.getLogger(DeleteAccount::class.java)
		val JS_NAME = "deleteAccount"
	}
}