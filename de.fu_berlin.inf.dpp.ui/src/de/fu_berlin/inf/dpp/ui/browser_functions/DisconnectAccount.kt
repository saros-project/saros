package de.fu_berlin.inf.dpp.ui.browser_functions

import de.fu_berlin.inf.dpp.HTMLUIContextFactory
import de.fu_berlin.inf.dpp.ui.browser_functions.BrowserFunction.Policy
import de.fu_berlin.inf.dpp.ui.core_facades.ConnectionFacade

/**
 * Disconnect the active account.
 *
 * Created by PicoContainer
 *
 * @param connectionFacade
 * @see HTMLUIContextFactory
 */
class DisconnectAccount (connectionFacade: ConnectionFacade?) : TypedJavaScriptFunction(JS_NAME) {
	private val connectionFacade: ConnectionFacade? = connectionFacade

	/**
	 * Disconnect the active account.
	 */
	@BrowserFunction(Policy.ASYNC)
	fun disconnect() {
		connectionFacade!!.disconnect()
	}

	companion object {
		val JS_NAME = "disconnect"
	}
}