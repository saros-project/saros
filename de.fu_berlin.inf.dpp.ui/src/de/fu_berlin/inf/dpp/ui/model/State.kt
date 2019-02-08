package de.fu_berlin.inf.dpp.ui.model

import java.util.ArrayList
import java.util.Collections
import de.fu_berlin.inf.dpp.account.XMPPAccount
import de.fu_berlin.inf.dpp.net.ConnectionState

/**
 * Represents the state of the browser application. It consists of an
 * {@link XMPPAccount}, a list of {@link de.fu_berlin.inf.dpp.ui.model.Contact}s
 * and the {@link de.fu_berlin.inf.dpp.net.ConnectionState}.
 */
class State
/**
 * @param activeAccount
 * the currently active account
 * @param contactList
 * the list of contacts of the active account
 * @param connectionState
 * the current connection state of the active account
 */
private constructor(
	activeAccount: XMPPAccount?, contactList: List<Contact>?,
	connectionState: ConnectionState?
) {
	/**
	 * Returns the active account or null if there is no account active.
	 *
	 * @return the active account or null
	 */
	var activeAccount: XMPPAccount? = null
	var contactList: List<Contact>? = null
	var connectionState: ConnectionState? = null

	/**
	 * Initial state: no active account, an empty account list, and Saros being
	 * {@link ConnectionState#NOT_CONNECTED}.
	 */
	constructor() : this(
		null, Collections.emptyList<Contact>(),
		de.fu_berlin.inf.dpp.net.ConnectionState.NOT_CONNECTED)

	init {
		this.activeAccount = activeAccount
		this.contactList = ArrayList<Contact>(contactList)
		this.connectionState = connectionState
	}
}