package de.fu_berlin.inf.dpp.ui.renderer

import java.util.ArrayList
import org.jivesoftware.smack.Connection
import org.jivesoftware.smack.RosterEntry
import org.jivesoftware.smack.packet.Presence
import de.fu_berlin.inf.ag_se.browser.extensions.IJQueryBrowser
import de.fu_berlin.inf.dpp.HTMLUIContextFactory
import de.fu_berlin.inf.dpp.account.NullAccountStoreListener
import de.fu_berlin.inf.dpp.account.XMPPAccount
import de.fu_berlin.inf.dpp.account.XMPPAccountStore
import de.fu_berlin.inf.dpp.net.ConnectionState
import de.fu_berlin.inf.dpp.net.xmpp.IConnectionListener
import de.fu_berlin.inf.dpp.net.xmpp.XMPPConnectionService
import de.fu_berlin.inf.dpp.ui.JavaScriptAPI
import de.fu_berlin.inf.dpp.ui.core_facades.RosterFacade
import de.fu_berlin.inf.dpp.ui.core_facades.RosterFacade.RosterChangeListener
import de.fu_berlin.inf.dpp.ui.model.Contact
import de.fu_berlin.inf.dpp.ui.model.State
import de.fu_berlin.inf.dpp.util.Pair

/**
 * This class is responsible for transferring information about the state of
 * Saros to the browser so they can be displayed. This information are
 * encapsulated in {@link de.fu_berlin.inf.dpp.ui.model.State}.
 *
 * This class also manages the {@link de.fu_berlin.inf.dpp.ui.model.State} via
 * listeners for the {@link de.fu_berlin.inf.dpp.net.ConnectionState} and the
 * {@link org.jivesoftware.smack.Roster}, from which the list of
 * {@link de.fu_berlin.inf.dpp.ui.model.Contact}s is created.
 */
class StateRenderer
/**
 * Created by PicoContainer
 *
 * @see HTMLUIContextFactory
 */
	(
	connectionService: XMPPConnectionService?,
	rosterFacade: RosterFacade?, accountStore: XMPPAccountStore?
) : Renderer() {
	private val state: State? = State()

	init {
		connectionService!!.addListener(object : IConnectionListener {
			override fun connectionStateChanged(
				connection: Connection?,
				newState: ConnectionState?
			) {
				var sanitizedState = newState
				if (sanitizedState === ConnectionState.ERROR) {
					sanitizedState = ConnectionState.NOT_CONNECTED
				}
				state!!.connectionState = sanitizedState
				render()
			}
		})
		rosterFacade!!.addListener(object : RosterChangeListener() {
			
			override fun setValue(rosterEntries: List<Pair<RosterEntry, Presence>>?) {
				val contacts = ArrayList<Contact>()
				for (entry in rosterEntries!!) {
					var c = ContactRenderer.convert(entry.p, entry.v)
					if(c != null){
						contacts.add(c)
					}
				}
				state!!.contactList = contacts
				render()
			}
		})
		accountStore!!.addListener(object : NullAccountStoreListener() {
			override fun activeAccountChanged(activeAccount: XMPPAccount?) {
				state!!.activeAccount = activeAccount
				render()
			}
		})
	}

	@Override
	@Synchronized
	public override fun render(browser: IJQueryBrowser?) {
		JavaScriptAPI.updateState(browser, this.state)
	}
}