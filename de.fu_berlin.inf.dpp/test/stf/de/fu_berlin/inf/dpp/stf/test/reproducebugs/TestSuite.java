package de.fu_berlin.inf.dpp.stf.test.reproducebugs;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ AccountWithDismatchedPasswordTest.class,
    EditDuringInvitationTest.class, HostInvitesBelatedlyTest.class,
    ParallelInvitationWithTerminationByHostTest.class,
    ParallelInvitationWithTerminationByInviteesTest.class,
    StrictSequentialInvitationWithoutTerminationTest.class })
public class TestSuite {
    // the class remains completely empty,
    // being used only as a holder for the above annotations
}