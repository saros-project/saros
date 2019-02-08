package de.fu_berlin.inf.dpp.ui.core_facades

import java.util.ArrayList
import org.jivesoftware.smack.Roster
import org.jivesoftware.smack.RosterEntry
import org.jivesoftware.smack.XMPPException
import org.jivesoftware.smack.packet.Presence
import de.fu_berlin.inf.dpp.HTMLUIContextFactory
import de.fu_berlin.inf.dpp.net.util.XMPPUtils
import de.fu_berlin.inf.dpp.net.xmpp.JID
import de.fu_berlin.inf.dpp.net.xmpp.XMPPConnectionService
import de.fu_berlin.inf.dpp.net.xmpp.roster.IRosterListener
import de.fu_berlin.inf.dpp.net.xmpp.roster.RosterTracker
import de.fu_berlin.inf.dpp.observables.ObservableValue
import de.fu_berlin.inf.dpp.observables.ValueChangeListener
import de.fu_berlin.inf.dpp.util.Pair

/**
 * Bundles all backend calls to alter the currently active account's contact
 * list. Provides a {@linkplain #addListener(RosterChangeListener) listener
 * interface} for notifications on any roster changes.
 * Created by PicoContainer
 *
 * @param connectionService
 * @param rosterTracker
 * @see HTMLUIContextFactory
 */
class RosterFacade (connectionService: XMPPConnectionService?, rosterTracker: RosterTracker?) {
	
	private val rosterListener = RosterTrackerListener()
	private val connectionService: XMPPConnectionService? = connectionService
	
	/**
	 * Note that all modifying methods of the returned roster instance might
	 * throw {@link IllegalStateException} if the connection is lost in between
	 * operations.
	 *
	 * @return the roster for the currently active connection.
	 * @throws XMPPException
	 * if the connection isn't established,<br>
	 *
	 */
	private val roster: Roster?
		@Throws(XMPPException::class)
		get() {
			val roster = connectionService!!.getRoster()
			if (roster == null) {
				throw XMPPException(CONNECTION_STATE_FAILURE)
			}
			return roster
		}

	/**
	 * Shorthand name for
	 * <code>ValueChangeListener&lt;List&lt;Pair&lt;RosterEntry, Presence&gt;&gt;&gt;</code>
	 */
	abstract class RosterChangeListener : ValueChangeListener<List<Pair<RosterEntry, Presence>>>// just a shorthand name

	/**
	 * Listens to changes propagated by the {@link RosterTracker} and converts
	 * them such that regardless of the roster change, a full list of all
	 * entries including their presence is made available to registered
	 * {@link RosterChangeListener}s.
	 */
	private inner class RosterTrackerListener : IRosterListener {
		internal val observable: ObservableValue<List<Pair<RosterEntry, Presence>>>? =
			ObservableValue<List<Pair<RosterEntry, Presence>>>(null)
		private// No roster available, clear entries
		/*
		* Buggish SMACK crap at its best ! The entries returned here can be
		* just plain references (see implementation) so we have to lookup
		* them correctly !
		*/
		val entries: List<Pair<RosterEntry, Presence>>?
			get() {
				if( roster == null){
					return ArrayList<Pair<RosterEntry, Presence>>()
				}
				val entries = ArrayList<Pair<RosterEntry, Presence>>(
					roster!!.getEntries().size
				)
				for (entryReference in roster!!.getEntries()) {
					val correctEntry = roster!!.getEntry(
						entryReference!!
							.getUser()
					)
					if (correctEntry == null)
						continue
					val presence = roster!!.getPresence(correctEntry.getUser())
					entries.add(
						Pair<RosterEntry, Presence>(
							correctEntry,
							presence
						)
					)
				}
				return entries
			}

		
		override fun entriesUpdated(addresses: Collection<String>?) {
			notifyListeners()
		}

		
		override fun entriesDeleted(addresses: Collection<String>?) {
			notifyListeners()
		}

		
		override fun entriesAdded(addresses: Collection<String>?) {
			notifyListeners()
		}

		
		override fun presenceChanged(presence: Presence?) {
			notifyListeners()
		}

		
		override fun rosterChanged(roster: Roster?) {
			notifyListeners()
		}

		private fun notifyListeners() {
			observable!!.setValue(entries)
		}
	}

	init {
		rosterTracker!!.addRosterListener(rosterListener)
	}

	/**
	 * Add a new listener to be notified on all changes to the roster
	 *
	 * @param listener
	 */
	fun addListener(listener: RosterChangeListener?) {
		rosterListener.observable!!.add(listener)
	}

	/**
	 * Deletes a contact from the contact list
	 *
	 * @param jid
	 * the JID of the contact to be deleted
	 */
	@Throws(XMPPException::class)
	fun deleteContact(jid: JID?) {
		try {
			XMPPUtils.removeFromRoster(
				connectionService!!.getConnection(),
				getEntry(jid)
			)
		} catch (e: IllegalStateException) {
			throw XMPPException(CONNECTION_STATE_FAILURE, e)
		}
	}

	/**
	 * Renames a contact (given by JID)
	 *
	 * @param jid
	 * the JID of the contact to be renamed
	 * @param name
	 * the new name of the contact
	 * @throws XMPPException
	 */
	@Throws(XMPPException::class)
	fun renameContact(jid: JID?, name: String?) {
		if (name == null) {
			throw IllegalArgumentException("name cannot be null")
		}
		try {
			getEntry(jid)!!.setName(name)
		} catch (e: IllegalStateException) {
			throw XMPPException(CONNECTION_STATE_FAILURE, e)
		}
	}

	/**
	 * Adds a contact to the contact list
	 *
	 * @param jid
	 * the JID of the contact to be added
	 * @param nickname
	 * the nickname of the contact
	 */
	@Throws(XMPPException::class)
	fun addContact(jid: JID?, nickname: String?) {
		try {
			roster!!.createEntry(jid!!.getBase(), nickname, null)
		} catch (e: IllegalStateException) {
			throw XMPPException(CONNECTION_STATE_FAILURE, e)
		}
	}

	/**
	 * @param jid
	 * to get the associated roster entry from
	 * @return the roster entry for the given jid
	 * @throws XMPPException
	 * if the connection isn't established,<br>
	 * if no entry couldn't been found
	 */
	@Throws(XMPPException::class)
	private fun getEntry(jid: JID?): RosterEntry? {
		val entry = roster!!.getEntry(jid!!.getBase())
		if (entry == null) {
			throw XMPPException(("Couldn't find an entry for " + jid.getBareJID()))
		}
		return entry
	}

	companion object {
		private val CONNECTION_STATE_FAILURE = "Invalide state, connection might be lost."
	}
}