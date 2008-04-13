package de.fu_berlin.inf.dpp.test.jupiter;

import junit.framework.Test;
import junit.framework.TestSuite;
import de.fu_berlin.inf.dpp.jupiter.internal.text.InsertOperation;
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
		
		client = new TwoWayJupiterClientDocument("abc",
				network);
		server = new TwoWayJupiterServerDocument("abc",
				network);

		
		
		network.addClient(client);
		network.addClient(server);

	}
	
	public void testInsertInsertOp1() throws Exception{
		client.sendOperation(new InsertOperation(0,"x"), 100);
		server.sendOperation(new InsertOperation(0,"y"), 200);
		
		Thread.sleep(300);
		
		assertEquals(client.getDocument(), server.getDocument());
	
	}
	
	
//	public static Test suite() {
//		TestSuite suite = new TestSuite("Test for dOPT puzzle.");
//		//$JUnit-BEGIN$
//		suite.addTest(new DOptPuzzleTest("testThreeConcurrentInsertOperations"));
//		suite.addTest(new DOptPuzzleTest("testThreeConcurrentInsertStringOperations"));
//		suite.addTest(new DOptPuzzleTest("testThreeConcurrentDeleteOperations"));
//		suite.addTest(new DOptPuzzleTest("testConcurrentInsertDeleteOperations"));
//		//$JUnit-END$
//		return suite;
//	}
}
