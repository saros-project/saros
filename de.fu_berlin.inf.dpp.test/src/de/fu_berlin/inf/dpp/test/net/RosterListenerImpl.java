package de.fu_berlin.inf.dpp.test.net;

import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.RosterPacket;
import org.jivesoftware.smack.packet.RosterPacket.ItemStatus;
import org.jivesoftware.smack.packet.RosterPacket.ItemType;

import de.fu_berlin.inf.dpp.net.JID;

public class RosterListenerImpl implements RosterListener, PacketListener {

	private static Logger logger = Logger.getLogger(RosterListenerImpl.class
			.toString());

	private XMPPConnection connection;

//	private Roster roster;

	public RosterListenerImpl(XMPPConnection conn, Roster roster) {
		this.connection = conn;
//		this.roster = roster;
	}

	public void entriesAdded(Collection<String> addresses) {
		logger.info("entriesAdded on "+connection.getUser());
		for (Iterator<String> it = addresses.iterator(); it.hasNext();) {
			String address = it.next();
//			RosterEntry entry = roster.getEntry(address);
			RosterEntry entry = connection.getRoster().getEntry(address);
			// When the entry is only from the other user, then send a
			// subscription request
			try {
				if (entry != null
						&& entry.getType() == RosterPacket.ItemType.none) {

					logger.info("added with type none " + connection.getUser());
					String name = entry.getName();
					if (entry.getName() == null) {
						name = new JID(entry.getUser()).getName();
					}
					// addUser(entry.getUser(), entry.getName());
					// connection.getRoster().createEntry(entry.getUser(),
					// name, new String[0]);

				}
				if (entry != null
						&& entry.getType() == RosterPacket.ItemType.from) {
					logger.info("added with type from " + connection.getUser());

//					String name = entry.getName();
//					if (entry.getName() == null) {
//						name = new JID(entry.getUser()).getName();
//					}
					
					connection.getRoster().createEntry(entry.getUser(), entry.getUser(),
							new String[0]);
//					roster.createEntry(entry.getUser(), entry.getUser(),
//							new String[0]);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	public void entriesDeleted(Collection<String> addresses) {
		logger.info("entries delete " + connection.getUser());

	}

	public void entriesUpdated(Collection<String> addresses) {
		logger.info("entries update " + connection.getUser());
		for (String address : addresses) {
			RosterEntry entry = connection.getRoster().getEntry(address);
			if (entry.getType().equals(RosterPacket.ItemType.to)) {
				System.out.println("to");
//
//				Presence presence = new Presence(Presence.Type.subscribed);
//				presence.setTo(entry.getUser());
//				presence.setFrom(connection.getUser());
//
//				connection.sendPacket(presence);
				
				/* wir kommen hier in eine endlosschleife. */
				// RosterPacket.Item rosterItem = new RosterPacket.Item(
				// entry.getUser(), entry.getUser());
				// ;
				// rosterItem.setItemType(ItemType.both);
				// rosterItem.setName(entry.getUser());
				//				
				// RosterPacket rosterPacket = new RosterPacket();
				// rosterPacket.setType(IQ.Type.SET);
				// rosterPacket.addRosterItem(rosterItem);
				// connection.sendPacket(rosterPacket);
			}
			if (entry.getType().equals(RosterPacket.ItemType.from)) {
				System.out.println("from");
				try {
					// RosterPacket.Item rosterItem = new RosterPacket.Item(
					// connection.getUser(), connection.getUser());
					// ;
					// rosterItem.setItemType(ItemType.both);
					// rosterItem.setName(entry.getUser());
					//					
					// RosterPacket rosterPacket = new RosterPacket();
					// rosterPacket.setType(IQ.Type.SET);
					// rosterPacket.addRosterItem(rosterItem);
					// connection.sendPacket(rosterPacket);

					// if(roster.getEntry(entry.getUser()) == null){
					
					
					connection.getRoster().createEntry(entry.getUser(),
							entry.getUser(), new String[0]);
//					roster.createEntry(entry.getUser(),
//							entry.getUser(), new String[0]);
					
					
					
					// }
				} catch (XMPPException e) {
					logger.info(e.getStackTrace().toString());
//					e.printStackTrace();
				}
			}
			// if (entry.getType().equals(RosterPacket.ItemType.none)) {
			// System.out.println("none");
			// }
			if (entry.getStatus() != null
					&& entry.getStatus().equals(
							RosterPacket.ItemStatus.SUBSCRIPTION_PENDING)) {
				logger.info("subscripe");

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
				
				// addUser(entry.getUser(),new JID(entry.getUser()).getName());
			}
			// addUser(entry.getUser(), entry.getName());
			// System.out.println(entry.getStatus());
			// System.out.println(entry.getName());
		}
	}

	public void presenceChanged(Presence presence) {

		logger.info("presence changed user" + connection.getUser()
				+ " status :" + presence.getType() + " from: "
				+ presence.getFrom());
	}

	@Deprecated
	public void addUser(String user, String name) {
		RosterPacket.Item rosterItem = new RosterPacket.Item(user, name);
		rosterItem.setItemType(ItemType.both);
		rosterItem.setName(name);

		RosterPacket rosterPacket = new RosterPacket();
		rosterPacket.setType(IQ.Type.SET);
		rosterPacket.addRosterItem(rosterItem);
		// rosterPacket.toXML();

		connection.sendPacket(rosterPacket);

		Presence presence = new Presence(Presence.Type.subscribed);
		presence.setTo(user);
		presence.setFrom(connection.getUser());

		connection.sendPacket(presence);
	}

	public void processPacket(Packet packet) {
		// TODO Auto-generated method stub
		logger.info("Packet called. " + packet.getFrom());

		if (!packet.getFrom().equals(connection.getUser())) {

			// RosterPacket rosterp = (RosterPacket) packet;
			/*
			 * 1. überprüfen ob es eine subscribe anfrage ist 2. überprüfen, ob
			 * user im in liste ist. 3. hinzufügen des accounts.
			 */
			if (packet instanceof Presence) {

				Presence p = (Presence) packet;

				/* this states handled by roster listener. */
				if (p.getType().equals(Presence.Type.unavailable)
						|| p.getType().equals(Presence.Type.available)) {
					logger.info("Presence " + p.getFrom() + " " + p.getType());
					return;
				}

				if (p.getType() == Presence.Type.subscribed) {
					logger.info("subcriped form " + p.getFrom());
				}

				/* Anfrage für eine Kontakthinzufügung. */
				if (p.getType().equals(Presence.Type.subscribe)) {
					
					//TODO: Änderung ohne roster object
					RosterEntry e = connection.getRoster().getEntry(packet.getFrom());
					logger.info("subscribe from " + p.getFrom());
//					RosterEntry e = roster.getEntry(packet.getFrom());
//					logger.info("subscribe from " + p.getFrom());
					
					
					if (e == null) {
						try {
//							Presence presence = new Presence(
//									Presence.Type.subscribe);
//							presence.setTo(packet.getFrom());
//							presence.setFrom(connection.getUser());
//
//							connection.sendPacket(presence);

							//TODO: Änderung ohne Roster object
							connection.getRoster().createEntry(packet.getFrom(), packet.getFrom(), null);
//							roster.createEntry(packet.getFrom(), packet.getFrom(), null);
							/* allow presence. */

							// RosterPacket.Item rosterItem = new
							// RosterPacket.Item(
							// packet.getFrom(), new
							// JID(packet.getFrom()).getName());
							// ;
							// rosterItem.setItemType(ItemType.both);
							// rosterItem.setName(new
							// JID(packet.getFrom()).getName());
							//							
							// RosterPacket rosterPacket = new RosterPacket();
							// rosterPacket.setType(IQ.Type.SET);
							// rosterPacket.addRosterItem(rosterItem);
							// connection.sendPacket(rosterPacket);
						} catch (XMPPException e1) {
							logger.log(Level.WARNING, e1.getStackTrace().toString());
						}
					}

				}
				if (packet instanceof RosterPacket) {
					RosterPacket rp = (RosterPacket) packet;
					System.out.println(rp.getType());
				}
			}
			// addUser(rosterp.getFrom(), rosterp.getFrom());
			// try {
			// roster.createEntry(rosterp.getFrom(), rosterp.getFrom(), null);
			// } catch (XMPPException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }
		}
	}

}
