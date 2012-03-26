package de.fu_berlin.inf.dpp.stf.test.reproducebugs;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import de.fu_berlin.inf.dpp.stf.test.invitation.permutation.EditDuringInvitationTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({ EditDuringInvitationTest.class,
    HostInvitesBelatedlyTest.class,
    ParallelInvitationWithTerminationByHostTest.class, })
public class TestSuite {
    // the class remains completely empty,
    // being used only as a holder for the above annotations
}