package de.fu_berlin.inf.dpp.test.net;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.jivesoftware.smack.XMPPConnection;

import de.fu_berlin.inf.dpp.net.internal.MultiUserChatManager;
import junit.framework.TestCase;

public class MultiUserChatManagerTest extends TestCase {

	static {
        XMPPConnection.DEBUG_ENABLED = true;
    }
	
	private String server = "jabber.cc";
	
	private XMPPConnection conn1;
	private String user1 = "ori79";
	private String user2 = "ori78";
	private String password = "123456";
	
	private XMPPConnection conn2;
	
	protected void setUp() throws Exception{
		PropertyConfigurator.configureAndWatch("log4j.properties", 60 * 1000);
		Logger logger = Logger.getLogger("de.fu_berlin.inf.dpp");
		
		conn1 = new XMPPConnection(server);
		conn1.connect();
		conn1.login(user1, password);
		
		conn2 = new XMPPConnection(server);
		conn2.connect();
		conn2.login(user2, password);
		
	}
	
	protected void tearDown() throws Exception {
		conn1.disconnect();
		conn2.disconnect();
	}
	
	public void testRoomExistConnection() throws Exception {
		MultiUserChatManager mucManager = new MultiUserChatManager();
		mucManager.initMUC(conn1, user1);
		
		MultiUserChatManager mucMananagerUser2 = new MultiUserChatManager();
		mucMananagerUser2.initMUC(conn2, user2);
	}
	
}
