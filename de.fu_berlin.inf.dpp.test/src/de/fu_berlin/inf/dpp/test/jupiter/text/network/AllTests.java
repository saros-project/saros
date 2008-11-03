package de.fu_berlin.inf.dpp.test.jupiter.text.network;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

    public static Test suite() {
	TestSuite suite = new TestSuite(
		"Test for de.fu_berlin.inf.dpp.test.jupiter.text.network");
	//$JUnit-BEGIN$
	suite.addTestSuite(SimulatedNetworkTest.class);
	//$JUnit-END$
	return suite;
    }

}
