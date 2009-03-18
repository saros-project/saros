package de.fu_berlin.inf.dpp.test.jupiter.puzzles;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

    public static Test suite() {
	TestSuite suite = new TestSuite(
		"Test for de.fu_berlin.inf.dpp.test.jupiter.puzzles");
	//$JUnit-BEGIN$
	suite.addTestSuite(InclusionTransformationTest.class);
	suite.addTestSuite(SimpleJupiterDocumentTest.class);
	suite.addTest(CounterExampleTest.suite());
	suite.addTest(DOptPuzzleTest.suite());
	suite.addTestSuite(SimpleServerProxyTest.class);
	suite.addTest(SimpleClientServerTest.suite());
	suite.addTest(ConvergenceProblemTest.suite());
	//$JUnit-END$
	return suite;
    }

}
