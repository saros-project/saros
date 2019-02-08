package de.fu_berlin.inf.dpp.ui.renderer

import org.jivesoftware.smack.RosterEntry
import org.jivesoftware.smack.packet.Presence
import org.jivesoftware.smack.packet.RosterPacket
import de.fu_berlin.inf.dpp.net.util.XMPPUtils
import de.fu_berlin.inf.dpp.ui.model.Contact

object ContactRenderer {
	/**
	 * Factory method to create Contact from roster entry.
	 *
	 * @param entry
	 * the roster entry of the contact
	 * @param presence
	 * the presence object of the contact
	 * @return a new contact created from the roster entry
	 */
	@JvmStatic
	fun convert(entry: RosterEntry?, presence: Presence?): Contact? {
		val displayableName = XMPPUtils.getDisplayableName(entry)
		val addition = createAdditionString(entry, presence)
		val presenceString = createPresenceString(presence)
		val jid = entry!!.getUser()
		return Contact(displayableName, presenceString, addition, jid)
	}

	private fun createPresenceString(presence: Presence?): String? {
		if (!presence!!.isAvailable())
			return "Offline"
		var mode = presence.getMode()
		if (mode == null) {
// see Presence#getMode();
			mode = Presence.Mode.available
		}
		when (mode) {
			Presence.Mode.away -> return "Away"
			Presence.Mode.dnd -> return "DND"
			Presence.Mode.xa -> return "XA"
			Presence.Mode.chat -> return "Chat"
			Presence.Mode.available -> return "Online"
			else -> return "Online"
		}
	}

	private fun createAdditionString(
		entry: RosterEntry?,
		presence: Presence?
	): String? {
		var addition = ""
		if (entry!!.getStatus() === RosterPacket.ItemStatus.SUBSCRIPTION_PENDING) {
			addition = "subscription pending"
		} else if ((entry!!.getType() === RosterPacket.ItemType.none || entry!!.getType() === RosterPacket.ItemType.from)) {
/*
* see http://xmpp.org/rfcs/rfc3921.html chapter 8.2.1, 8.3.1 and
* 8.6
*/
			addition = "subscription cancelled"
		} else if (presence!!.isAway()) {
			addition = presence.getMode().toString()
		}
		return addition
	}
}