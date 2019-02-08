package de.fu_berlin.inf.dpp.ui.browser_functions

import org.apache.log4j.Logger
import de.fu_berlin.inf.dpp.HTMLUIContextFactory
import de.fu_berlin.inf.dpp.HTMLUIStrings
import de.fu_berlin.inf.dpp.account.XMPPAccount
import de.fu_berlin.inf.dpp.account.XMPPAccountStore
import de.fu_berlin.inf.dpp.ui.JavaScriptAPI
import de.fu_berlin.inf.dpp.ui.browser_functions.BrowserFunction.Policy

/**
 * Save a given JID/Password combination.
 * Created by PicoContainer
 *
 * @param accountStore
 * the save action is delegated to
 * @see HTMLUIContextFactory
 */
class SaveAccount (accountStore: XMPPAccountStore?) : TypedJavaScriptFunction(JS_NAME) {
	
	private val accountStore: XMPPAccountStore? = accountStore

	/**
	 * Saves a given {@link XMPPAccount}in the {@link XMPPAccountStore}.
	 * <p>
	 * No account is created and an error will be shown, if there is already an
	 * account with the same values for all of <br>
	 * {@link XMPPAccount#getUsername()}; {@link XMPPAccount#getDomain()};
	 * {@link XMPPAccount#getServer()}; {@link XMPPAccount#getPort()},
	 *
	 * @param acc
	 * the JID of the new account
	 */
	@BrowserFunction(Policy.ASYNC)
	fun saveAccount(acc: XMPPAccount?) {
		try {
			accountStore!!.createAccount(
				acc!!.getUsername(), acc.getPassword(),
				acc.getDomain(), acc.getServer(), acc.getPort(), acc.useTLS(),
				acc.useSASL()
			)
		} catch (e: IllegalArgumentException) {
			// FIXME not all IllegalArgumentExceptions are due to already
			// present accounts
			LOG!!.debug("Account " + acc!!.getUsername() + " already present")
			JavaScriptAPI.showError(
				browser,
				HTMLUIStrings.ERR_ACCOUNT_ALREADY_PRESENT
			)
		}
	}

	companion object {
		private val LOG = Logger.getLogger(SaveAccount::class.java)
		val JS_NAME = "saveAccount"
	}
}