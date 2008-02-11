package de.fu_berlin.inf.dpp.net.internal;

import java.util.Collection;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.RosterPacket;

import de.fu_berlin.inf.dpp.ui.IRosterTree;


public class RosterListenerImpl implements RosterListener, PacketListener{

	private static Logger logger = Logger.getLogger(RosterListenerImpl.class);
	
	private XMPPConnection connection;
	
	private IRosterTree rtree;
	
	public RosterListenerImpl(XMPPConnection conn, IRosterTree rtree){
		this.connection = conn;
		this.rtree = rtree;
	}
	
	@Override
	public void entriesAdded(Collection<String> addresses) {
		logger.debug("entriesAdded called.");
		for (Iterator<String> it = addresses.iterator(); it.hasNext();) {
			String address = it.next();
//			RosterEntry entry = roster.getEntry(address);
			RosterEntry entry = connection.getRoster().getEntry(address);
			// When the entry is only from the other user, then send a
			// subscription request
			try {
				if (entry != null
						&& entry.getType() == RosterPacket.ItemType.none) {

					logger.debug("added with type none " + connection.getUser());

				}
				if (entry != null
						&& entry.getType() == RosterPacket.ItemType.from) {
					logger.debug("added with type from " + connection.getUser());

					/* called create entry method to complete the registration process. (third part)*/
					connection.getRoster().createEntry(entry.getUser(), entry.getUser(),
							new String[0]);
				}
			} catch (XMPPException e) {
				logger.error("error while entriesAdded is called.",e);
			}
		}

		rtree.refreshRosterTree(true);
		
	}

	@Override
	public void entriesDeleted(Collection<String> addresses) {
		logger.debug("entry delete");
		
		rtree.refreshRosterTree(false);
	}

	@Override
	public void entriesUpdated(Collection<String> addresses) {
		logger.debug("entries update " + connection.getUser());
		for (String address : addresses) {
			RosterEntry entry = connection.getRoster().getEntry(address);
			if (entry.getType().equals(RosterPacket.ItemType.to)) {
				logger.debug("Type to called for : "+entry.getUser());
			}
			if (entry.getType().equals(RosterPacket.ItemType.from)) {
				logger.debug("Type from called for : "+entry.getUser());
				try {			
					
					connection.getRoster().createEntry(entry.getUser(),
							entry.getUser(), new String[0]);

				} catch (XMPPException e) {
					logger.debug(e.getStackTrace().toString());
				}
			}

			if (entry.getStatus() != null
					&& entry.getStatus().equals(
							RosterPacket.ItemStatus.SUBSCRIPTION_PENDING)) {
				logger.debug("subscripe");

				if (entry.getUser().equals(connection.getUser())) {
					Presence presence = new Presence(Presence.Type.unsubscribed);
					presence.setTo(entry.getUser());
					presence.setFrom(connection.getUser());

					connection.sendPacket(presence);
				} else {
					Presence presence = new Presence(Presence.Type.subscribed);
					presence.setTo(entry.getUser());
					presence.setFrom(connection.getUser());

					connection.sendPacket(presence);
				}
				
			}

		}
		
		rtree.refreshRosterTree(false);
	}

	@Override
	public void presenceChanged(Presence presence) {

		logger.debug("presence changed user" + connection.getUser()
				+ " status :" + presence.getType() + " from: "
				+ presence.getFrom());
		
		rtree.refreshRosterTree(true);
	}

	@Override
	public void processPacket(Packet packet) {
		
//		logger.info("Packet called. " + packet.getFrom());

		
		if (!packet.getFrom().equals(connection.getUser())) {
			/*
			 * 1. überprüfen ob es eine subscribe anfrage ist 2. überprüfen, ob
			 * user im in liste ist. 3. hinzufügen des accounts.
			 */
			if (packet instanceof Presence) {

				Presence p = (Presence) packet;

				/* this states handled by roster listener. */
				if (p.getType().equals(Presence.Type.unavailable)
						|| p.getType().equals(Presence.Type.available)) {
					logger.debug("Presence " + p.getFrom() + " " + p.getType());
					return;
				}

				if (p.getType() == Presence.Type.subscribed) {
					logger.debug("subcriped form " + p.getFrom());
				}

				/* Anfrage für eine Kontakthinzufügung. */
				if (p.getType().equals(Presence.Type.subscribe)) {
					
					RosterEntry e = connection.getRoster().getEntry(packet.getFrom());
					logger.debug("subscribe from " + p.getFrom());

					
					if (e == null) {
						try {
							/* create appropriate entry for request. */							
							connection.getRoster().createEntry(packet.getFrom(), packet.getFrom(), null);

						} catch (XMPPException e1) {
							logger.error(e1);
						}
					}

				}
				if (packet instanceof RosterPacket) {
					RosterPacket rp = (RosterPacket) packet;
					logger.debug("roster packet with type "+rp.getType());
				}
			}
		}
		
	}

}
