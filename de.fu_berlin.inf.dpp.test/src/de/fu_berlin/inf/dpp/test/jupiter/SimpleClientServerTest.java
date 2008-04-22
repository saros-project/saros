package de.fu_berlin.inf.dpp.test.jupiter;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.log4j.PropertyConfigurator;

import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.DeleteOperation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.InsertOperation;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.test.jupiter.text.ClientSynchronizedDocument;
import de.fu_berlin.inf.dpp.test.jupiter.text.JupiterTestCase;
import de.fu_berlin.inf.dpp.test.jupiter.text.ServerSynchronizedDocument;
import de.fu_berlin.inf.dpp.test.jupiter.text.TwoWayJupiterServerDocument;
import de.fu_berlin.inf.dpp.test.jupiter.text.network.SimulateNetzwork;

public class SimpleClientServerTest extends JupiterTestCase{


	public SimpleClientServerTest(String method){
		super(method);
	}
	
	public void setUp() {
		network = new SimulateNetzwork();

	}
	
	public void tearDown(){
		network = null;
	}	
	
	/**
	 * simple test scenario between server and client.
	 * The client and server send operation from same state.
	 * Server message has delay, so that client create a new
	 * Operation. So if server message arrive client and server
	 * have different document state and jupiter algorithm 
	 * has to converge the document states.
	 */
	public void test2WayProtocol() throws Exception{
		ClientSynchronizedDocument c1 = new ClientSynchronizedDocument("abc",
				network);
		c1.setJID(new JID("ori79@jabber.cc"));
		TwoWayJupiterServerDocument s1 = new TwoWayJupiterServerDocument("abc",
				network);
		s1.setJID(new JID("ori78@jabber.cc"));

		network.addClient(c1);
		network.addClient(s1);

		c1.sendOperation(new InsertOperation(0, "e"),1000);
		/*short delay. */
		Thread.sleep(100);
		
		assertEquals("eabc", c1.getDocument());
		assertEquals("abc", s1.getDocument());

		c1.sendOperation(new InsertOperation(0, "x"), 2000);
		/*short delay. */
		Thread.sleep(100);
		
		assertEquals("xeabc", c1.getDocument());
		assertEquals("abc", s1.getDocument());
		
		
		
		s1.sendOperation(c1.getJID(),new DeleteOperation(0,"a"),0);
		/*short delay. */
		Thread.sleep(500);
		assertEquals("xebc",c1.getDocument());
		Thread.sleep(2000);
		assertEquals("xebc",s1.getDocument());
		
		Thread.sleep(6000);
//		assertEquals("xebc",c1.getDocument());
//		assertEquals("xebc", s1.getDocument());
		assertEquals(c1.getDocument(), s1.getDocument());
	}
	
	/**
	 * Site A insert a char into the delete area of site b.
	 * The delete operation has delay of two seconds.
	 * @throws Exception
	 */
	public void testDeleteStringWithConcurentInsert() throws Exception{
		ClientSynchronizedDocument c1 = new ClientSynchronizedDocument("abcdefg",
				network);
		c1.setJID(new JID("ori79@jabber.cc"));
		TwoWayJupiterServerDocument s1 = new TwoWayJupiterServerDocument("abcdefg",
				network);
		s1.setJID(new JID("ori78@jabber.cc"));
		
		network.addClient(c1);
		network.addClient(s1);
		
		
		
		c1.sendOperation(new InsertOperation(3, "x"),1000);
		s1.sendOperation(c1.getJID(),new DeleteOperation(1,"bcde"),2000);
		Thread.sleep(1100);
		assertEquals("abcxdefg", c1.getDocument());
		
		Thread.sleep(4000);
		assertEquals(c1.getDocument(), s1.getDocument());
		assertEquals("axfg",c1.getDocument());
		
	}
	
	/**
	 * Site A insert a char into the delete area of site b.
	 * The insert operation has delay of two seconds.
	 * @throws Exception
	 */
	public void testInsertStringWithConcurentDelete() throws Exception{
		ClientSynchronizedDocument c1 = new ClientSynchronizedDocument("abcdefg",
				network);
		c1.setJID(new JID("ori79@jabber.cc"));
		TwoWayJupiterServerDocument s1 = new TwoWayJupiterServerDocument("abcdefg",
				network);
		s1.setJID(new JID("ori78@jabber.cc"));
		
		network.addClient(c1);
		network.addClient(s1);
		
		c1.sendOperation(new InsertOperation(3, "x"),2000);
		s1.sendOperation(c1.getJID(),new DeleteOperation(1,"bcde"),0);
		Thread.sleep(100);
		assertEquals("afg", s1.getDocument());
		
		Thread.sleep(4000);
		assertEquals(c1.getDocument(), s1.getDocument());
		assertEquals("axfg",c1.getDocument());
		
	}
	
	public static Test suite() {
		TestSuite suite = new TestSuite("Test for simple client server test cases.");
		//$JUnit-BEGIN$
		suite.addTest(new SimpleClientServerTest("testInsertStringWithConcurentDelete"));
		suite.addTest(new SimpleClientServerTest("test2WayProtocol"));
		suite.addTest(new SimpleClientServerTest("testDeleteStringWithConcurentInsert"));

		//$JUnit-END$
		return suite;
	}
}
