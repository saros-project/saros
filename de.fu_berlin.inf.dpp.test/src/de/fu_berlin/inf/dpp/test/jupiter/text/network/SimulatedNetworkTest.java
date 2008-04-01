package de.fu_berlin.inf.dpp.test.jupiter.text.network;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import de.fu_berlin.inf.dpp.jupiter.internal.text.DeleteOperation;
import de.fu_berlin.inf.dpp.jupiter.internal.text.InsertOperation;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.test.jupiter.text.ClientSynchronizedDocument;
import de.fu_berlin.inf.dpp.test.jupiter.text.ServerSynchronizedDocument;
import junit.framework.TestCase;

public class SimulatedNetworkTest extends TestCase {

	static {
		PropertyConfigurator.configureAndWatch("log4j.properties", 60 * 1000);
		Logger logger = Logger.getLogger("de.fu_berlin.inf.dpp");
	}
	private SimulateNetzwork network;

	public void setUp() {
		network = new SimulateNetzwork();

	}

	public void tearDown() {

	}

	/**
	 * simple test of a two-site communication.
	 */
	public void testSimpleNetzworkMessage() throws Exception{
		ClientSynchronizedDocument c1 = new ClientSynchronizedDocument("abc",
				network);
		c1.setJID(new JID("ori79@jabber.cc"));
		ServerSynchronizedDocument s1 = new ServerSynchronizedDocument("abc",
				network);
		s1.setJID(new JID("ori78@jabber.cc"));

		network.addClient(c1);
		network.addClient(s1);

		/* test network connection */
		c1.sendOperation(new InsertOperation(0, "e"));
		/*short delay. */
		Thread.sleep(100);
		
		assertEquals("eabc", c1.getDocument());
		assertEquals("eabc", s1.getDocument());
		s1.sendOperation(c1.getJID(), new DeleteOperation(0, "e"));
		/*short delay. */
		Thread.sleep(100);
		
		assertEquals("abc", c1.getDocument());
		assertEquals("abc", s1.getDocument());

	}

	public void testNetworkMessagesWithDelay() throws Exception{
		ClientSynchronizedDocument c1 = new ClientSynchronizedDocument("abc",
				network);
		c1.setJID(new JID("ori79@jabber.cc"));
		ServerSynchronizedDocument s1 = new ServerSynchronizedDocument("abc",
				network);
		s1.setJID(new JID("ori78@jabber.cc"));

		network.addClient(c1);
		network.addClient(s1);

		c1.sendOperation(new InsertOperation(0, "e"));
		/*short delay. */
		Thread.sleep(100);
		
		assertEquals("eabc", c1.getDocument());
		assertEquals("eabc", s1.getDocument());

		c1.sendOperation(new InsertOperation(0, "x"), 2000);
		/*short delay. */
		Thread.sleep(100);
		
		assertEquals("xeabc", c1.getDocument());
		assertEquals("eabc", s1.getDocument());
		
		s1.sendOperation(c1.getJID(),new DeleteOperation(0,"e"),0);
		/*short delay. */
		Thread.sleep(100);
		assertEquals("xabc",c1.getDocument());
		Thread.sleep(2000);
		assertEquals("xabc",s1.getDocument());
	}
}
