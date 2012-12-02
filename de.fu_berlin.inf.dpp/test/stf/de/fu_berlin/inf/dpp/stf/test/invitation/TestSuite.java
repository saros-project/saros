package de.fu_berlin.inf.dpp.stf.test.invitation;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ HostInvitesBelatedlyTest.class,
    InviteAndLeaveStressTest.class, InviteWithDifferentVersionsTest.class,
    NonHostInvitesBuddyTest.class,
    ParallelInvitationWithTerminationByHostTest.class,
    Share2UsersSequentiallyTest.class, Share3UsersConcurrentlyTest.class,
    Share3UsersLeavingSessionTest.class, Share3UsersSequentiallyTest.class,
    ShareProjectUsingExistingProjectTest.class, ShareProjectWizardUITest.class,
    UserDeclinesInvitationToCurrentSessionTest.class })
public class TestSuite {
    // the class remains completely empty,
    // being used only as a holder for the above annotations
}