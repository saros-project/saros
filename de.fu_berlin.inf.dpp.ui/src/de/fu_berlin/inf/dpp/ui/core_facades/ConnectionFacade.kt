package de.fu_berlin.inf.dpp.ui.core_facades

import de.fu_berlin.inf.dpp.HTMLUIContextFactory
import de.fu_berlin.inf.dpp.account.XMPPAccount
import de.fu_berlin.inf.dpp.account.XMPPAccountStore
import de.fu_berlin.inf.dpp.communication.connection.ConnectionHandler

/**
 * Bundles backend calls for connecting to and disconnecting from a server.
 */
class ConnectionFacade
/**
 * Created by PicoContainer
 *
 * @param connectionHandler
 * @param accountStore
 * @see HTMLUIContextFactory
 */
	(
	connectionHandler: ConnectionHandler?,
	accountStore: XMPPAccountStore?
) {
	private val connectionHandler: ConnectionHandler? = connectionHandler
	private val accountStore: XMPPAccountStore? = accountStore

	/**
	 * Connects the given XMPP account to the server.
	 *
	 * @param account
	 * representing an XMPP account
	 */
	fun connect(account: XMPPAccount?) {
		accountStore!!.setAccountActive(account)
		connectionHandler!!.connect(account, false)
	}

	/**
	 * Disconnects the currently connected account.
	 */
	fun disconnect() {
		connectionHandler!!.disconnect()
	}
}