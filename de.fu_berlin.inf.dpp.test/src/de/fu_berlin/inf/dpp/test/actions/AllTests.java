package de.fu_berlin.inf.dpp.test.actions;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

    public static Test suite() {
	TestSuite suite = new TestSuite(
		"Test for de.fu_berlin.inf.dpp.test.actions");
	//$JUnit-BEGIN$
	suite.addTestSuite(TestActions.class);
	//$JUnit-END$
	return suite;
    }

}
