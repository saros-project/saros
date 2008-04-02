package de.fu_berlin.inf.dpp.test.jupiter;

import org.apache.log4j.PropertyConfigurator;

import de.fu_berlin.inf.dpp.jupiter.internal.text.DeleteOperation;
import de.fu_berlin.inf.dpp.jupiter.internal.text.InsertOperation;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.test.jupiter.text.ClientSynchronizedDocument;
import de.fu_berlin.inf.dpp.test.jupiter.text.ServerSynchronizedDocument;
import de.fu_berlin.inf.dpp.test.jupiter.text.network.SimulateNetzwork;
import junit.framework.TestCase;

/**
 * this class contains test cases for testing init server side and
 * communication with client sides.
 * @author orieger
 *
 */
public class SimpleServerProxyTest extends JupiterTestCase {

	/**
	 * one client connect with server and create an operation.
	 * @throws Exception
	 */
	public void xtestOneSimpleConnectionWithJupiterProxy() throws Exception{
		JID jid_c1 = new JID("ori79@jabber.cc");
		JID jid_server = new JID("ori78@jabber.cc");
		
		ClientSynchronizedDocument c1 = new ClientSynchronizedDocument("abcdefg",
				network,jid_c1);
		ServerSynchronizedDocument s1 = new ServerSynchronizedDocument("abcdefg",
				network,jid_server);

		network.addClient(c1);
		network.addClient(s1);
		
		/* create proxyqueues. */
		s1.addProxyClient(jid_c1);
		
		c1.sendOperation(new InsertOperation(1,"c"),0);
		Thread.sleep(100);
		assertEquals(c1.getDocument(), s1.getDocument());
		assertEquals("acbcdefg",c1.getDocument());
		
		s1.sendOperation(new DeleteOperation(1,"cb"));
		Thread.sleep(300);
		assertEquals(c1.getDocument(), s1.getDocument());
		assertEquals("acdefg",c1.getDocument());
	}
	
	/**
	 * two clients connect with jupiter server.
	 * @throws Exception
	 */
	public void xtestTwoClientWithJupiterProxy() throws Exception{
		JID jid_c1 = new JID("ori79@jabber.cc");
		JID jid_c2 = new JID("ori80@jabber.cc");
		JID jid_server = new JID("ori78@jabber.cc");
		
		ClientSynchronizedDocument c1 = new ClientSynchronizedDocument("abcdefg",
				network,jid_c1);
		ClientSynchronizedDocument c2 = new ClientSynchronizedDocument("abcdefg",
				network,jid_c2);
		ServerSynchronizedDocument s1 = new ServerSynchronizedDocument("abcdefg",
				network,jid_server);

		network.addClient(c1);
		network.addClient(c2);
		network.addClient(s1);
		
		/* create proxyqueues. */
		s1.addProxyClient(jid_c1);
		s1.addProxyClient(jid_c2);
		
		c1.sendOperation(new InsertOperation(1,"c"),0);
		Thread.sleep(300);
		assertEquals(c1.getDocument(), s1.getDocument());
		assertEquals("acbcdefg",c1.getDocument());
		assertEquals("acbcdefg",c2.getDocument());
		
		/* send two concurrent operations. */
		c2.sendOperation(new InsertOperation(1,"t"), 2000);
		c1.sendOperation(new InsertOperation(1,"x"), 1000);
		Thread.sleep(100);
		/* assert local execution. */
		assertEquals("axcbcdefg",c1.getDocument());
		assertEquals("atcbcdefg",c2.getDocument());
		Thread.sleep(2500);
		/* assert remote operations. */
		assertEquals(s1.getDocument(),c1.getDocument());
		assertEquals(c1.getDocument(),c2.getDocument());
	}
	
	/**
	 * two clients connect with jupiter server.
	 * Concurrent delete and insert operations.
	 * @throws Exception
	 */
	public void testTwoClientWithJupiterProxyDeleteInsertOperations() throws Exception{
		JID jid_c1 = new JID("ori79@jabber.cc");
		JID jid_c2 = new JID("ori80@jabber.cc");
		JID jid_server = new JID("ori78@jabber.cc");
		
		ClientSynchronizedDocument c1 = new ClientSynchronizedDocument("abcdefg",
				network,jid_c1);
		ClientSynchronizedDocument c2 = new ClientSynchronizedDocument("abcdefg",
				network,jid_c2);
		ServerSynchronizedDocument s1 = new ServerSynchronizedDocument("abcdefg",
				network,jid_server);

		network.addClient(c1);
		network.addClient(c2);
		network.addClient(s1);
		
		/* create proxyqueues. */
		s1.addProxyClient(jid_c1);
		s1.addProxyClient(jid_c2);
		
		c1.sendOperation(new InsertOperation(1,"c"),0);
		Thread.sleep(300);
		assertEquals(c1.getDocument(), s1.getDocument());
		assertEquals("acbcdefg",c1.getDocument());
		assertEquals("acbcdefg",c2.getDocument());
		
		/* send two concurrent operations. */
		c2.sendOperation(new InsertOperation(1,"t"), 2000);
		c1.sendOperation(new DeleteOperation(0,"acbc"), 1000);
		Thread.sleep(100);
		/* assert local execution. */
		assertEquals("atcbcdefg",c2.getDocument());
		assertEquals("defg",c1.getDocument());
		Thread.sleep(2500);
		/* assert remote operations. */
		assertEquals(s1.getDocument(),c1.getDocument());
		
		/*TODO: failure: dtefg  != cdefg */
		assertEquals(c1.getDocument(),c2.getDocument());
	}
}
