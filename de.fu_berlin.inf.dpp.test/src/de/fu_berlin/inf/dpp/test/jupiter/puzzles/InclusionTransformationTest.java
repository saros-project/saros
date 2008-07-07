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
		assertEquals(client.getDocument(),"xyyxabcdefg");
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
		assertEquals(client.getDocument(),"xyxxdefg");
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
		assertEquals(client.getDocument(),"yxbdefg");
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
		assertEquals(client.getDocument(),"xbcdefg");
	}
	
	/**
	 * insert operation inside delete operation area
	 * @throws Exception
	 */
	public void testCase5() throws Exception{
		client.sendOperation(new InsertOperation(1,"x"), 100);
		server.sendOperation(new DeleteOperation(0,"abc"), 200);
		
		Thread.sleep(300);
		
		assertEquals(client.getDocument(), server.getDocument());
		assertEquals(client.getDocument(),"xdefg");
	}
	
	/**
	 * insert operation after delete operation 
	 * @throws Exception
	 */
	public void testCase6() throws Exception{
		client.sendOperation(new DeleteOperation(0,"a"), 100);
		server.sendOperation(new InsertOperation(1,"x"), 200);
		
		Thread.sleep(300);
		
		assertEquals(client.getDocument(), server.getDocument());
		assertEquals(client.getDocument(),"xbcdefg");
	}
	
	/**
	 * insert operation at same position of delete operation
	 * @throws Exception
	 */
	public void testCase7() throws Exception{
		client.sendOperation(new DeleteOperation(0,"a"), 200);
		server.sendOperation(new InsertOperation(0,"x"), 100);
		
		Thread.sleep(300);
		
		assertEquals(client.getDocument(), server.getDocument());
		assertEquals(client.getDocument(),"xbcdefg");
	}
	
	/**
	 * insert operation is in area of delete operation. 
	 * @throws Exception
	 */
	public void testCase8() throws Exception{
		client.sendOperation(new DeleteOperation(0,"abc"), 100);
		server.sendOperation(new InsertOperation(1,"x"), 200);
		
		Thread.sleep(300);
		
		assertEquals(client.getDocument(), server.getDocument());
		assertEquals(client.getDocument(),"xdefg");
	}
	
	/**
	 * first delete operation is completely before second operation.
	 * @throws Exception
	 */
	public void testCase9() throws Exception{
		client.sendOperation(new DeleteOperation(0,"a"), 100);
		server.sendOperation(new DeleteOperation(1,"bc"), 200);
		
		Thread.sleep(300);
		
		assertEquals(client.getDocument(), server.getDocument());
		assertEquals(client.getDocument(),"defg");
	}
	
	/**
	 * delete operation inside delete operation area
	 * @throws Exception
	 */
	public void testCase10() throws Exception{
		client.sendOperation(new DeleteOperation(0,"abcd"), 100);
		server.sendOperation(new DeleteOperation(1,"bc"), 200);
		
		Thread.sleep(300);
		
		assertEquals(client.getDocument(), server.getDocument());
		assertEquals(client.getDocument(),"efg");
	}
	
	/**
	 * delete operation starts before second delete operation and ends inside.
	 * @throws Exception
	 */
	public void testCase11() throws Exception{
		client.sendOperation(new DeleteOperation(0,"ab"), 100);
		server.sendOperation(new DeleteOperation(1,"bcd"), 200);
		
		Thread.sleep(300);
		
		assertEquals(client.getDocument(), server.getDocument());
		assertEquals(client.getDocument(),"efg");
	}
	
	/**
	 * delete operation starts inside of delete operation area and ends
	 * after this.
	 * @throws Exception
	 */
	public void testCase12() throws Exception{
		client.sendOperation(new DeleteOperation(1,"bcd"), 100);
		server.sendOperation(new DeleteOperation(0,"abc"), 200);
		
		Thread.sleep(300);
		
		assertEquals(client.getDocument(), server.getDocument());
		assertEquals(client.getDocument(),"efg");
	}
	
	/**
	 * delete operation inside second delete operation area
	 * @throws Exception
	 */
	public void testCase13() throws Exception{
		client.sendOperation(new DeleteOperation(1,"b"), 100);
		server.sendOperation(new DeleteOperation(0,"abc"), 200);
		
		Thread.sleep(300);
		
		assertEquals(client.getDocument(), server.getDocument());
		assertEquals(client.getDocument(),"defg");
	
	}
	
	public static Test suite() {
		TestSuite suite = new TestSuite("Test of inclusion transformations.");
		//$JUnit-BEGIN$
		suite.addTest(new InclusionTransformationTest("testCase1"));
		suite.addTest(new InclusionTransformationTest("testCase2"));
		suite.addTest(new InclusionTransformationTest("testCase3"));
		suite.addTest(new InclusionTransformationTest("testCase4"));
		suite.addTest(new InclusionTransformationTest("testCase5"));
		suite.addTest(new InclusionTransformationTest("testCase5"));
		suite.addTest(new InclusionTransformationTest("testCase6"));
		suite.addTest(new InclusionTransformationTest("testCase7"));
		suite.addTest(new InclusionTransformationTest("testCase8"));
		suite.addTest(new InclusionTransformationTest("testCase9"));
		suite.addTest(new InclusionTransformationTest("testCase10"));
		suite.addTest(new InclusionTransformationTest("testCase11"));
		suite.addTest(new InclusionTransformationTest("testCase12"));
		suite.addTest(new InclusionTransformationTest("testCase13"));
		//$JUnit-END$
		return suite;
	}
}
