package de.fu_berlin.inf.dpp.test;

import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Logger;

import junit.framework.TestCase;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterGroup;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.Roster.SubscriptionMode;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.RosterPacket;
import org.jivesoftware.smack.packet.RosterPacket.ItemType;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.test.invitation.internal.XMPPChatTransmitterFileTransferTest;
import de.fu_berlin.inf.dpp.test.net.RosterListenerImpl;

public class SarosTest extends TestCase {

	static {
		XMPPConnection.DEBUG_ENABLED = true;
		Roster.setDefaultSubscriptionMode(SubscriptionMode.accept_all);
	}

	private static Logger logger = Logger.getLogger(XMPPTestCase.class
			.toString());

	public XMPPConnection connection = null;
	public XMPPConnection received_connection = null;

	
	public static  String server = "jabber.org";
	public static String User1 = "ori78@"+server;
	public static String User2 = "ori79@"+server;
	
	
	// public FileTransferManager transferManager1 = null;

	public SarosTest() {
		// super("jabber.org");
	}

	@Override
	public void setUp() throws XMPPException {
		ConnectionConfiguration conConfig = new ConnectionConfiguration(
				"jabber.cc");
		// conConfig.setSocketFactory(SocketFactory.getDefault());

		conConfig.setReconnectionAllowed(true);
		// try{
		connection = new XMPPConnection(server);
		received_connection = new XMPPConnection(server);
		
		// connection1 = new XMPPConnection(conConfig);

		// while (!connection1.isAuthenticated()) {
		// System.out.println("connecting user1");
		connection.connect();
		received_connection.connect();

		connection.login("ori78", "123456");
		received_connection.login("ori79", "123456");
		logger.info("connection established.");
	}

	@Override
	public void tearDown() {
		connection.disconnect();
		received_connection.disconnect();
	}

	
	/**
	 * 1. Löschen der Entry Listen
	 * 2. Hinzufügen von RosterListener zu B
	 * 3. A.CreateEntry(B)
	 * 
	 * B hat Presence von A.
	 * @throws XMPPException
	 * @throws InterruptedException
	 */
	public void xtestCreateEntryA() throws XMPPException, InterruptedException{
		Roster re_roster = received_connection.getRoster();
		RosterListenerImpl list2 = new RosterListenerImpl(received_connection,
				re_roster);
		received_connection.addPacketListener(list2, null);
		
		
		Roster roster = connection.getRoster();
		RosterListenerImpl list1 = new RosterListenerImpl(connection,
				roster);
		connection.addPacketListener(list1, null);
		
		/* delete lists.*/
		emptyUserList(roster);
		Thread.sleep(1000);
		emptyUserList(re_roster);
		
		
		
		roster.addRosterListener(list1);
		roster.reload();
		re_roster.addRosterListener(list2);
		re_roster.reload();
		
		Thread.sleep(1000);
//		Presence presence = new Presence(
//				Presence.Type.subscribe);
//		presence.setTo(received_connection.getUser());
//		presence.setFrom(connection.getUser());
//		connection.sendPacket(presence);
		
		/*2. neue Verbindung erstellen. */
		roster.createEntry(User2, User2, null);
		
		Thread.sleep(1000);
		
		Collection<RosterEntry> entries = re_roster.getEntries();
		for(RosterEntry entry : entries){
			
			Presence p = re_roster.getPresence(entry.getUser());
			if(p != null){
				System.out.println(p.getType());
			}
		}
		
		entries = re_roster.getEntries();
		for(RosterEntry entry : entries){
			
			Presence p = roster.getPresence(entry.getUser());
			if(p != null){
				System.out.println(p.getType());
			}
		}
		
	}
	
	public void xtestCreateAccount() throws XMPPException, InterruptedException{
		XMPPConnection connection = new XMPPConnection(server);
		connection.connect();
		connection.getAccountManager().createAccount("ori78", "123456");
		Thread.sleep(1000);
		connection.getAccountManager().createAccount("ori79", "123456");
	}
	
	public void xtestDeleteAccount() throws XMPPException{
		connection.getAccountManager().deleteAccount();
		received_connection.getAccountManager().deleteAccount();
	}
	
	public void testPresence() throws InterruptedException, XMPPException{
		Roster re_roster = received_connection.getRoster();
		RosterListenerImpl list2 = new RosterListenerImpl(received_connection,
				re_roster);
		re_roster.addRosterListener(list2);
		received_connection.addPacketListener(list2, null);
		re_roster.reload();
		
		Roster roster = connection.getRoster();
		RosterListenerImpl list1 = new RosterListenerImpl(connection,
				roster);
		roster.addRosterListener(list1);
		connection.addPacketListener(list1, null);
		roster.reload();
		
		Thread.sleep(2000);
		Collection<RosterEntry> entries = roster.getEntries();
		for(RosterEntry entry : entries){
			
			System.out.println("entry : "+entry.getUser());
			Presence p = roster.getPresence(entry.getUser());
			if(p != null){
				System.out.println(p.getType());
			}
		}
		
		entries = re_roster.getEntries();
		for(RosterEntry entry : entries){
			
			Presence p = re_roster.getPresence(entry.getUser());
			if(p != null){
				System.out.println(p.getType());
			}
		}
		
	}
	
	/**
	 * FINAL SOLUTIONS
	 * diese methode testet die unterschiedlichen methoden informationen
	 * aus dem roster auszulesen
	 * @throws XMPPException 
	 * @throws InterruptedException 
	 */
	public void xtestRosterPacketInformation() throws XMPPException, InterruptedException{
		Roster re_roster = received_connection.getRoster();
		RosterListenerImpl list2 = new RosterListenerImpl(received_connection,
				re_roster);
		re_roster.addRosterListener(list2);
		received_connection.addPacketListener(list2, null);
		re_roster.reload();
		
		Roster roster = connection.getRoster();
		RosterListenerImpl list1 = new RosterListenerImpl(connection,
				roster);
		roster.addRosterListener(list1);
		connection.addPacketListener(list1, null);
		roster.reload();
		
		emptyUserList(roster);
		emptyUserList(re_roster);
		
		/*1. bisherige Einträge auslesen. */
		Collection<RosterEntry> entries = roster.getEntries();
		for(RosterEntry entry:entries){
			System.out.println("user: "+entry.getUser()+" name : "+entry.getName()+" status: "+entry.getStatus()+" type: "+entry.getType());
		}
		Thread.sleep(1000);
		/*2. neue Verbindung erstellen. */
		roster.createEntry(received_connection.getUser(), new JID(received_connection.getUser()).getName(), null);
		
		/*3. setzen, dass man von dem user interesse hat. */
        RosterPacket.Item rosterItem = new RosterPacket.Item(received_connection.getUser(),new JID(received_connection.getUser()).getName());;
        rosterItem.setItemType(ItemType.both);
        rosterItem.setName(received_connection.getUser());
        
        RosterPacket rosterPacket = new RosterPacket();
        rosterPacket.setType(IQ.Type.SET);
        rosterPacket.addRosterItem(rosterItem);
        connection.sendPacket(rosterPacket);
        Thread.sleep(1000);
//		/*aktiviere */
//		Presence presence = new Presence(Presence.Type.subscribed);
//		presence.setFrom(received_connection.getUser());
//		presence.setTo(connection.getUser());
//		received_connection.sendPacket(presence);
		Thread.sleep(2000);
		
		entries = roster.getEntries();
		for(RosterEntry entry:entries){
			Presence p = roster.getPresence(entry.getUser());
			System.out.println("user: "+entry.getUser()+" name : "+entry.getName()+" status: "+entry.getStatus()+" type: "+entry.getType());
		}
		
		System.out.println("Ende");
	}
	
	
	private void emptyUserList(Roster roster) throws XMPPException {
		logger.info("empty list");

		System.out.println("count before : " + roster.getEntryCount());
		Collection<RosterEntry> entries = roster.getEntries();
		try {
		for (RosterEntry entry : entries) {
			System.out.println(""+entry.getName()+" "+entry.getUser()+" "+entry.getStatus()+" "+entry.getType());
			roster.removeEntry(entry);
			Thread.sleep(500);
		}
		
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("count after: " + roster.getEntryCount());
//		assertEquals(0, roster.getEntryCount());

	}

	/**
	 * both user are connected and entry will added with listener.
	 * 
	 * @throws XMPPException
	 * @throws InterruptedException
	 */
	public void xtestCreateNewEntry() throws XMPPException,
			InterruptedException {
		/* add listener. */

		Roster roster = connection.getRoster();
		Roster re_roster = received_connection.getRoster();
		RosterListenerImpl list1 = new RosterListenerImpl(connection, roster);
		connection.getRoster().addRosterListener(list1);
		
		
		RosterListenerImpl list2 = new RosterListenerImpl(received_connection,
				re_roster);
		received_connection.getRoster().addRosterListener(list2);

		emptyUserList(roster);
		assertEquals(0, roster.getUnfiledEntryCount());

		roster.createEntry(received_connection.getUser(), "ori79",
				new String[] { "default" });
		roster.createGroup("default");
		// RosterGroup gr = roster.getGroup("default");
		// if(gr == null){

		// }
		Thread.sleep(500);
		assertEquals(1, connection.getRoster().getEntryCount());

		// Collection<RosterEntry> entries = roster.getEntries();
		// roster.reload();

		// assertEquals(0,roster.getUnfiledEntryCount());
		// Thread.sleep(500);
		// connection.getRoster().getEntries();
		// assertEquals(0,roster.getUnfiledEntryCount());
		assertEquals(1, roster.getEntryCount());
		// assertEquals(1, roster.getGroupCount());
	}

	/**
	 * both user are connected and entry will added with listener.
	 * 
	 * @throws XMPPException
	 * @throws InterruptedException
	 */
	public void xtestCreateNewEntry2() throws XMPPException,
			InterruptedException {
		/* add listener. */

		Roster roster = connection.getRoster();
		RosterListenerImpl list1 = new RosterListenerImpl(connection, roster);
		roster.addRosterListener(list1);
		connection.addPacketListener(list1, null);
		
		Roster re_roster = received_connection.getRoster();
		RosterListenerImpl list2 = new RosterListenerImpl(received_connection,
				re_roster);
		re_roster.addRosterListener(list2);
		received_connection.addPacketListener(list2, null);
		
		
		Thread.sleep(1000);
		
		
		emptyUserList(roster);
		emptyUserList(re_roster);
		
//		Collection<RosterPacket.Item> items = roster.
//		assertEquals(0, roster.getUnfiledEntryCount());
//		list1.addUser(received_connection.getUser(), received_connection.getUser());
		roster.createEntry(received_connection.getUser(), new JID(received_connection.getUser()).getName(),
				new String[0]);

		Thread.sleep(500);
		assertEquals(1, roster.getEntryCount());
		assertEquals(1,re_roster.getEntryCount());

	}
	
	/**
	 * entries überprüfen und presence checken
	 */
	public void xtestUpdateExistEntry() {
		/* add listener. */
		Roster roster = connection.getRoster();
		Roster re_roster = received_connection.getRoster();
		RosterListenerImpl list1 = new RosterListenerImpl(connection, roster);
		connection.getRoster().addRosterListener(list1);
		RosterListenerImpl list2 = new RosterListenerImpl(received_connection,
				re_roster);
		received_connection.getRoster().addRosterListener(list2);

		RosterGroup gr = roster.getGroup("default");
		Collection<RosterEntry> entries = null;
		if (gr != null) {
			gr.getEntries();
			roster.createGroup("default");
		}
		else{
			entries = roster.getEntries();
		}
		System.out.println(" Entry count: "+roster.getEntryCount());
		for (RosterEntry entry : entries) {
			
			
			System.out.println(entry.getType());
			Presence p = roster.getPresence(entry.getUser());
			System.out.println(p.getStatus());
			list1.addUser(entry.getUser(), entry.getName());
		}
	}

	public void xtestCreateNewRosterEntry() {
		XMPPConnection conn = connection;
		Roster roster = conn.getRoster();

		Collection<RosterEntry> unfiled = roster.getUnfiledEntries();
		for (RosterEntry ent : unfiled) {
			RosterPacket packet = new RosterPacket();

			System.out.println(ent.getUser());
		}
		// roster.setSubscriptionMode(SubscriptionMode.accept_all);

		Collection<RosterEntry> entries = conn.getRoster().getEntries();
		for (RosterEntry en : entries) {
			// Presence.Type.subscribe

			System.out.println(roster.getPresence(en.getUser()));
			System.out.println(conn.getUser() + " " + en.getUser());

		}
	}

}
