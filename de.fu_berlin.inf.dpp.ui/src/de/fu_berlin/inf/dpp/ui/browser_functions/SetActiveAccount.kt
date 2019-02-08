package de.fu_berlin.inf.dpp.ui.browser_functions

import org.apache.log4j.Logger
import de.fu_berlin.inf.dpp.HTMLUIContextFactory
import de.fu_berlin.inf.dpp.HTMLUIStrings
import de.fu_berlin.inf.dpp.account.XMPPAccount
import de.fu_berlin.inf.dpp.account.XMPPAccountStore
import de.fu_berlin.inf.dpp.ui.JavaScriptAPI

/**
 * Set an existing account as active. The currently active account can't be
 * deleted.
 *
 * Created by PicoContainer
 *
 * @param accountStore
 * to redirect the set action.
 * @see HTMLUIContextFactory
 
 */
class SetActiveAccount (accountStore: XMPPAccountStore?) : TypedJavaScriptFunction(JS_NAME) {
	private val accountStore: XMPPAccountStore? = accountStore

	/**
	 * Activate the given {@link XMPPAccount}
	 * <p>
	 * Note that, on success, this action will trigger a re-rendering of the
	 * account data to reflect the changes immediately. If this operation fails,
	 * an error is shown to the user.
	 *
	 * @param account
	 * the account to be activated
	 */
	@BrowserFunction
	fun setActiveAccount(account: XMPPAccount?) {
		try {
			accountStore!!.setAccountActive(account)
		} catch (e: IllegalArgumentException) {
			LOG!!.error(
				("Couldn't activate account " + account!!.toString()
						+ ". Error:" + e.message), e
			)
			JavaScriptAPI.showError(
				browser,
				HTMLUIStrings.ERR_ACCOUNT_SET_ACTIVE_FAILED
			)
		}
	}

	companion object {
		private val LOG = Logger.getLogger(SetActiveAccount::class.java)
		val JS_NAME = "setActiveAccount"
	}
}