package de.fu_berlin.inf.dpp.test.jupiter.text.network;

import de.fu_berlin.inf.dpp.jupiter.internal.text.DeleteOperation;
import de.fu_berlin.inf.dpp.jupiter.internal.text.InsertOperation;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.test.jupiter.text.ClientSynchronizedDocument;
import de.fu_berlin.inf.dpp.test.jupiter.text.ServerSynchronizedDocument;
import junit.framework.TestCase;

public class SimulatedNetworkTest extends TestCase{

	private SimulateNetzwork network;
	
	public void setUp(){
		network = new SimulateNetzwork();
	}
	
	public void tearDown(){
		
	}
	
	/**
	 * simple test of a two-site communication.
	 */
	public void testSimpleNetzworkMessage(){
		ClientSynchronizedDocument c1 = new ClientSynchronizedDocument("abc",network);
		c1.setJID(new JID("ori79@jabber.cc"));
		ServerSynchronizedDocument s1 = new ServerSynchronizedDocument("abc",network);
		s1.setJID(new JID("ori78@jabber.cc"));
		
		network.addClient(c1);
		network.addClient(s1);
		
		/*test network connection*/
		c1.sendOperation(new InsertOperation(0,"e"));
		assertEquals("eabc",c1.getDocument().toString());
		s1.sendOperation(c1.getJID(), new DeleteOperation(0,"e"));
		assertEquals("abc",c1.getDocument().toString());
		
	}
}
