package de.fu_berlin.inf.dpp.test;

import java.util.Collection;
import java.util.logging.Logger;

import junit.framework.TestCase;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.Roster.SubscriptionMode;
import org.jivesoftware.smack.packet.Presence;

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

    public static String server = "jabber.cc";
    public static String User1 = "ori78@" + server;
    public static String User2 = "ori79@" + server;

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
     * 1. Löschen der Entry Listen 2. Hinzufügen von RosterListener zu B 3.
     * A.CreateEntry(B)
     * 
     * B hat Presence von A.
     * 
     * @throws XMPPException
     * @throws InterruptedException
     */
    public void xtestCreateEntryA() throws XMPPException, InterruptedException {
	Roster re_roster = received_connection.getRoster();
	RosterListenerImpl list2 = new RosterListenerImpl(received_connection,
		re_roster);
	received_connection.addPacketListener(list2, null);

	Roster roster = connection.getRoster();
	RosterListenerImpl list1 = new RosterListenerImpl(connection, roster);
	connection.addPacketListener(list1, null);

	/* delete lists. */
	emptyUserList(roster);
	Thread.sleep(1000);
	emptyUserList(re_roster);

	roster.addRosterListener(list1);
	roster.reload();
	re_roster.addRosterListener(list2);
	re_roster.reload();

	Thread.sleep(1000);
	// Presence presence = new Presence(
	// Presence.Type.subscribe);
	// presence.setTo(received_connection.getUser());
	// presence.setFrom(connection.getUser());
	// connection.sendPacket(presence);

	/* 2. neue Verbindung erstellen. */
	roster.createEntry(User2, User2, null);

	Thread.sleep(2000);

	roster.reload();
	re_roster.reload();

	Thread.sleep(2000);
	Collection<RosterEntry> entries = re_roster.getEntries();
	for (RosterEntry entry : entries) {

	    Presence p = re_roster.getPresence(entry.getUser());
	    if (p != null) {
		System.out.println(p.getType());
	    }
	}

	entries = re_roster.getEntries();
	for (RosterEntry entry : entries) {

	    Presence p = roster.getPresence(entry.getUser());
	    if (p != null) {
		System.out.println(p.getType());
	    }
	}

	// Thread.sleep(500);
	// assertEquals(1, roster.getEntryCount());
	// assertEquals(1,re_roster.getEntryCount());

    }

    public void testCreateAccount() throws XMPPException, InterruptedException {
	XMPPConnection connection = new XMPPConnection(server);
	connection.connect();
	connection.getAccountManager().createAccount("anton78", "123456");
	Thread.sleep(1000);
	connection.getAccountManager().createAccount("carl80", "123456");
	Thread.sleep(1000);
	connection.getAccountManager().createAccount("bob79", "123456");
    }

    public void xtestDeleteAccount() throws XMPPException {
	connection.getAccountManager().deleteAccount();
	received_connection.getAccountManager().deleteAccount();
    }

    public void xtestDeleteAllEntries() throws XMPPException,
	    InterruptedException {
	Roster re_roster = received_connection.getRoster();
	RosterListenerImpl list2 = new RosterListenerImpl(received_connection,
		re_roster);
	received_connection.addPacketListener(list2, null);

	Roster roster = connection.getRoster();
	RosterListenerImpl list1 = new RosterListenerImpl(connection, roster);
	connection.addPacketListener(list1, null);

	/* delete lists. */
	emptyUserList(roster);
	Thread.sleep(1000);
	emptyUserList(re_roster);

    }

    public void xtestPresence() throws InterruptedException, XMPPException {
	Roster re_roster = received_connection.getRoster();
	RosterListenerImpl list2 = new RosterListenerImpl(received_connection,
		re_roster);
	re_roster.addRosterListener(list2);
	received_connection.addPacketListener(list2, null);
	re_roster.reload();

	Roster roster = connection.getRoster();
	RosterListenerImpl list1 = new RosterListenerImpl(connection, roster);
	roster.addRosterListener(list1);
	connection.addPacketListener(list1, null);
	roster.reload();

	Thread.sleep(2000);
	Collection<RosterEntry> entries = roster.getEntries();
	for (RosterEntry entry : entries) {

	    System.out.println("entry : " + entry.getUser());
	    Presence p = roster.getPresence(entry.getUser());
	    if (p != null) {
		System.out.println(p.getType());
	    }
	}

	entries = re_roster.getEntries();
	for (RosterEntry entry : entries) {

	    Presence p = re_roster.getPresence(entry.getUser());
	    if (p != null) {
		System.out.println(p.getType());
	    }
	}

    }

    private void emptyUserList(Roster roster) throws XMPPException {
	logger.info("empty list");

	System.out.println("count before : " + roster.getEntryCount());
	Collection<RosterEntry> entries = roster.getEntries();
	try {
	    for (RosterEntry entry : entries) {
		System.out.println("" + entry.getName() + " " + entry.getUser()
			+ " " + entry.getStatus() + " " + entry.getType());
		roster.removeEntry(entry);
		Thread.sleep(500);
	    }

	    Thread.sleep(500);
	} catch (InterruptedException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	System.out.println("count after: " + roster.getEntryCount());
	// assertEquals(0, roster.getEntryCount());

    }

}
