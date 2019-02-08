package de.fu_berlin.inf.dpp.ui.browser_functions

import de.fu_berlin.inf.dpp.HTMLUIContextFactory
import de.fu_berlin.inf.dpp.account.XMPPAccount
import de.fu_berlin.inf.dpp.ui.browser_functions.BrowserFunction.Policy
import de.fu_berlin.inf.dpp.ui.core_facades.ConnectionFacade

/**
 * Connect with given Account.
 */
class ConnectAccount
/**
 * Created by PicoContainer
 *
 * @param connectionFacade
 * @see HTMLUIContextFactory
 */
	(private val connectionFacade: ConnectionFacade?) : TypedJavaScriptFunction(JS_NAME) {
	
	/**
	 * Connect with given Account.
	 *
	 * @param account
	 */
	@BrowserFunction(Policy.ASYNC)
	fun connect(account: XMPPAccount?) {
		connectionFacade!!.connect(account)
	}

	companion object {
		val JS_NAME = "connect"
	}
}