package de.fu_berlin.inf.dpp.test.jupiter.server;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

    public static Test suite() {
	TestSuite suite = new TestSuite(
		"Test for de.fu_berlin.inf.dpp.test.jupiter.server");
	//$JUnit-BEGIN$
	suite.addTestSuite(TestClientManaging.class);
	//$JUnit-END$
	return suite;
    }

}
