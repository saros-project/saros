package de.fu_berlin.inf.dpp.ui.browser_functions

import org.apache.log4j.Logger
import de.fu_berlin.inf.dpp.HTMLUIContextFactory
import de.fu_berlin.inf.dpp.HTMLUIStrings
import de.fu_berlin.inf.dpp.account.XMPPAccount
import de.fu_berlin.inf.dpp.account.XMPPAccountStore
import de.fu_berlin.inf.dpp.ui.JavaScriptAPI

/**
 * Edit an existing account in the account store.
 *
 * Created by PicoContainer
 *
 * @param accountStore
 * to manage the XMPPAccounts
 * @see HTMLUIContextFactory
 
 */
class EditAccount (accountStore: XMPPAccountStore?) : TypedJavaScriptFunction(JS_NAME) {
	private val accountStore: XMPPAccountStore? = accountStore

	/**
	 * Edit a given {@link XMPPAccount} in the {@link XMPPAccountStore}.
	 * <p>
	 * Note that, on success, this action will trigger a re-rendering of the
	 * account data to reflect the changes immediately. If this operation fails,
	 * an error is shown to the user.
	 *
	 * @param oldAccount
	 * the account to be changed
	 * @param newAccount
	 * the new data for the account
	 */
	@BrowserFunction
	fun editAccount(oldAccount: XMPPAccount?, newAccount: XMPPAccount?) {
		try {
			accountStore!!
				.changeAccountData(
					oldAccount, newAccount!!.getUsername(),
					newAccount.getPassword(), newAccount.getDomain(),
					newAccount.getServer(), newAccount.getPort(),
					newAccount.useTLS(), newAccount.useSASL()
				)
		} catch (e: IllegalArgumentException) {
			LOG!!.error("Couldn't edit account " + e.message, e)
			JavaScriptAPI.showError(
				browser,
				HTMLUIStrings.ERR_ACCOUNT_EDIT_FAILED
			)
		}
	}

	companion object {
		private val LOG = Logger.getLogger(EditAccount::class.java)
		val JS_NAME = "editAccount"
	}
}