package de.fu_berlin.inf.dpp.test.jupiter.puzzles;

import junit.framework.Test;
import junit.framework.TestSuite;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.DeleteOperation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.InsertOperation;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.test.jupiter.text.ClientSynchronizedDocument;
import de.fu_berlin.inf.dpp.test.jupiter.text.JupiterTestCase;
import de.fu_berlin.inf.dpp.test.jupiter.text.ServerSynchronizedDocument;
import de.fu_berlin.inf.dpp.test.jupiter.text.TwoWayJupiterClientDocument;
import de.fu_berlin.inf.dpp.test.jupiter.text.TwoWayJupiterServerDocument;
import de.fu_berlin.inf.dpp.test.jupiter.text.network.SimulateNetzwork;
/**
 * Test class contains all possible transformation of operations.
 * 
 * @author orieger
 *
 */

public class InclusionTransformationTest extends JupiterTestCase {

	TwoWayJupiterClientDocument client;
	TwoWayJupiterServerDocument server;
	
	public InclusionTransformationTest(String method){
		super(method);
	}
	
	public void setUp(){
		
		network = new SimulateNetzwork();
		
		client = new TwoWayJupiterClientDocument("abcdefg",
				network);
		server = new TwoWayJupiterServerDocument("abcdefg",
				network);

		network.addClient(client);
		network.addClient(server);

	}
	
	/**
	 * insert before insert
	 * @throws Exception
	 */
	public void testCase1() throws Exception{
		client.sendOperation(new InsertOperation(0,"x"), 100);
		server.sendOperation(new InsertOperation(0,"y"), 200);
		
		Thread.sleep(300);
		
		assertEquals(client.getDocument(), server.getDocument());
		
		client.sendOperation(new InsertOperation(0,"x"), 100);
		server.sendOperation(new InsertOperation(1,"y"), 200);
		
		Thread.sleep(300);
		
		assertEquals(client.getDocument(), server.getDocument());
	
	}
	
	/**
	 * insert after insert
	 * @throws Exception
	 */
	public void testCase2() throws Exception{
		client.sendOperation(new InsertOperation(1,"xx"), 100);
		server.sendOperation(new DeleteOperation(0,"yyy"), 200);
		
		Thread.sleep(300);
		
		assertEquals(client.getDocument(), server.getDocument());
		
		client.sendOperation(new InsertOperation(2,"x"), 100);
		server.sendOperation(new InsertOperation(1,"y"), 200);
		
		Thread.sleep(300);
		
		assertEquals(client.getDocument(), server.getDocument());
	
	}
	
	/**
	 * insert before delete operation
	 * @throws Exception
	 */
	public void testCase3() throws Exception{
		client.sendOperation(new InsertOperation(1,"x"), 100);
		server.sendOperation(new DeleteOperation(2,"c"), 200);
		
		Thread.sleep(300);
		
		assertEquals(client.getDocument(), server.getDocument());
		
		client.sendOperation(new InsertOperation(0,"y"), 100);
		server.sendOperation(new DeleteOperation(0,"a"), 200);
		
		Thread.sleep(300);
		
		assertEquals(client.getDocument(), server.getDocument());
	
	}
	
	
	/**
	 * insert after delete operation
	 * @throws Exception
	 */
	public void testCase4() throws Exception{
		client.sendOperation(new InsertOperation(1,"x"), 100);
		server.sendOperation(new DeleteOperation(0,"a"), 200);
		
		Thread.sleep(300);
		
		assertEquals(client.getDocument(), server.getDocument());
	
	}
	
	/**
	 * insert inside delete area
	 * @throws Exception
	 */
	public void testCase5() throws Exception{
		client.sendOperation(new InsertOperation(1,"x"), 100);
		server.sendOperation(new DeleteOperation(0,"abc"), 200);
		
		Thread.sleep(300);
		
		assertEquals(client.getDocument(), server.getDocument());
	
	}
	
	public static Test suite() {
		TestSuite suite = new TestSuite("Test for dOPT puzzle.");
		//$JUnit-BEGIN$
		suite.addTest(new InclusionTransformationTest("testCase1"));
		suite.addTest(new InclusionTransformationTest("testCase2"));
		suite.addTest(new InclusionTransformationTest("testCase3"));
		suite.addTest(new InclusionTransformationTest("testCase4"));
		suite.addTest(new InclusionTransformationTest("testCase5"));
		//$JUnit-END$
		return suite;
	}
}
