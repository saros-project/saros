package de.fu_berlin.inf.dpp.net.internal;

import java.util.Collection;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.RosterPacket;

import de.fu_berlin.inf.dpp.ui.IRosterTree;

public class RosterListenerImpl implements RosterListener, PacketListener {

	private static Logger logger = Logger.getLogger(RosterListenerImpl.class);

	private XMPPConnection connection;

	private IRosterTree rtree;

	public RosterListenerImpl(XMPPConnection conn, IRosterTree rtree) {
		this.connection = conn;
		this.rtree = rtree;
	}

	public void entriesAdded(Collection<String> addresses) {
		rtree.refreshRosterTree(true);
	}

	public void entriesDeleted(Collection<String> addresses) {
		logger.debug("entry delete");

		rtree.refreshRosterTree(false);
	}

	// TODO realize a manual subsctiption
	public void entriesUpdated(Collection<String> addresses) {
		for (String address : addresses) {
			Roster roster = connection.getRoster();
			RosterEntry entry = roster.getEntry(address);
			if (entry.getStatus() != null
					&& entry.getStatus().equals(
							RosterPacket.ItemStatus.SUBSCRIPTION_PENDING)) {
				Presence presence = new Presence(Presence.Type.subscribed);
				presence.setTo(entry.getUser());
				presence.setFrom(connection.getUser());

				connection.sendPacket(presence);

			}
		}
		rtree.refreshRosterTree(false);
	}

	public void presenceChanged(Presence presence) {

		logger.debug("presence changed user" + connection.getUser()
				+ " status :" + presence.getType() + " from: "
				+ presence.getFrom());

		rtree.refreshRosterTree(true);
	}

	public void processPacket(Packet packet) {

		logger.debug("Packet called. " + packet.getFrom());

		if (!packet.getFrom().equals(connection.getUser())) {

			if (packet instanceof Presence) {
				Presence p = (Presence) packet;

				if (p.getType() == Presence.Type.subscribed) {
					logger.debug("subcribed from " + p.getFrom());
				}

				// if (p.getType().equals(Presence.Type.subscribe)) {
				//
				// RosterEntry e = connection.getRoster().getEntry(
				// packet.getFrom());
				// logger.debug("subscribe from " + p.getFrom());
				//
				// if (e == null) {
				// try {
				// /* create appropriate entry for request. */
				// connection.getRoster().createEntry(
				// packet.getFrom(), packet.getFrom(), null);
				//
				// } catch (XMPPException e1) {
				// logger.error(e1);
				// }
				// }
				// }
				if (packet instanceof RosterPacket) {
					RosterPacket rp = (RosterPacket) packet;
					logger.debug("roster packet with type " + rp.getType());
				}
			}
		}

	}

}
