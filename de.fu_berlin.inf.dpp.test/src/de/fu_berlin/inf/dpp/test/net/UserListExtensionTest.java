package de.fu_berlin.inf.dpp.test.net;

import java.util.List;
import java.util.Vector;

import junit.framework.TestCase;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.packet.DefaultPacketExtension;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.User.UserRole;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.PacketExtensions;

/**
 * this class contains test cases for testing the user list jabber extensions.
 * 
 * @author orieger
 * 
 */
public class UserListExtensionTest extends TestCase implements PacketListener,
	MessageListener {

    static {
	XMPPConnection.DEBUG_ENABLED = true;
    }
    private XMPPConnection connection1;
    private XMPPConnection connection2;
    List<User> userList;

    public UserListExtensionTest(String name) {
	super(name);
    }

    @Override
    protected void setUp() throws Exception {
	super.setUp();
	connection1 = new XMPPConnection("jabber.cc");
	connection1.connect();
	connection1.login("ori78", "123456");

	connection2 = new XMPPConnection("jabber.cc");
	connection2.connect();
	connection2.login("ori79", "123456");
	connection2.addPacketListener(this, new MessageTypeFilter(
		Message.Type.chat));
    }

    @Override
    protected void tearDown() throws Exception {
	super.tearDown();
    }

    public void testUserListExtensionTransfer() throws Exception {
	userList = new Vector<User>();
	User u = new User(new JID("ori1@jabber.de"));
	u.setColorID(1);
	u.setUserRole(UserRole.DRIVER);
	userList.add(u);
	User u1 = new User(new JID("ori2@jabber.de"));
	u1.setColorID(1);
	u1.setUserRole(UserRole.OBSERVER);
	userList.add(u1);

	ChatManager chatmanager = connection1.getChatManager();
	Chat newChat = chatmanager.createChat(connection2.getUser(), this);

	try {
	    Message message = new Message();
	    // Request req = new RequestImpl(1,new JupiterVectorTime(1,3),new
	    // DeleteOperation(34,"insert text"));
	    // req.setEditorPath(new Path("hello"));
	    // req.setJID(new JID("ori78@jabber.cc"));
	    message.addExtension(PacketExtensions
		    .createUserListExtension(userList));
	    newChat.sendMessage(message);
	} catch (XMPPException e) {
	    System.out.println("Error Delivering block");
	}
	Thread.sleep(500);
    }

    public void processPacket(Packet packet) {
	Message message = (Message) packet;
	processMessage(null, message);
    }

    public void processMessage(Chat chat, Message message) {
	List<User> incomingUser = new Vector<User>();
	DefaultPacketExtension userlistExtension = PacketExtensions
		.getUserlistExtension(message);

	int count = 0;
	while (true) {
	    String jidS = userlistExtension.getValue("User" + count);
	    if (jidS == null) {
		break;
		// log.debug("   *:" + jidS);
	    }

	    JID jid = new JID(jidS);
	    User user = new User(jid);

	    String userRole = userlistExtension.getValue("UserRole" + count);
	    user.setUserRole(de.fu_berlin.inf.dpp.User.UserRole
		    .valueOf(userRole));

	    String color = userlistExtension.getValue("UserColor" + count);
	    try {
		user.setColorID(Integer.parseInt(color));
	    } catch (NumberFormatException nfe) {
		// log.warn("Exception during convert user color form userlist for user : "+user.getJid());
		System.out.println("NumberFormatException.");
	    }
	    count++;

	    incomingUser.add(user);

	}

	assertEquals(userList, incomingUser);

    }

}
