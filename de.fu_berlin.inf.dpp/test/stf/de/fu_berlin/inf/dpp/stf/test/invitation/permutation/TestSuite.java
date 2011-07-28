package de.fu_berlin.inf.dpp.stf.test.invitation.permutation;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;


@RunWith(Suite.class)
@Suite.SuiteClasses({ Share2UsersSequentiallyTest.class,
    Share3UsersSequentiallyTest.class, Share3UsersConcurrentlyTest.class,
    Share3UsersLeavingSessionTest.class,
    ShareProjectUsingExistingProjectTest.class })
public class TestSuite {
    // the class remains completely empty,
    // being used only as a holder for the above annotations
}