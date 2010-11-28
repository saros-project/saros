package de.fu_berlin.inf.dpp.stf.client.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import de.fu_berlin.inf.dpp.stf.client.test.basicElements.TestBasicSarosElements;
import de.fu_berlin.inf.dpp.stf.client.test.testcases.RosterViewBehaviour.TestChangingNameInRosterView;
import de.fu_berlin.inf.dpp.stf.client.test.testcases.fileFolderOperations.TestFileOperations;
import de.fu_berlin.inf.dpp.stf.client.test.testcases.fileFolderOperations.TestFolderOperations;
import de.fu_berlin.inf.dpp.stf.client.test.testcases.fileFolderOperations.TestSVNStateUpdates;
import de.fu_berlin.inf.dpp.stf.client.test.testcases.initialising.TestHandleContacts;
import de.fu_berlin.inf.dpp.stf.client.test.testcases.invitation.TestEditDuringInvitation;
import de.fu_berlin.inf.dpp.stf.client.test.testcases.invitation.TestSVNStateInitialization;
import de.fu_berlin.inf.dpp.stf.client.test.testcases.invitation.TestShare2UsersSequentially;
import de.fu_berlin.inf.dpp.stf.client.test.testcases.invitation.TestShare3UsersConcurrently;
import de.fu_berlin.inf.dpp.stf.client.test.testcases.invitation.TestShare3UsersSequentially;
import de.fu_berlin.inf.dpp.stf.client.test.testcases.invitation.TestShareProjectUsingExistingProject;
import de.fu_berlin.inf.dpp.stf.client.test.testcases.invitation.permutations.TestParallelInvitationWithTerminationByHost;
import de.fu_berlin.inf.dpp.stf.client.test.testcases.invitation.permutations.TestParallelInvitationWithTerminationByInvitees;

@RunWith(Suite.class)
@SuiteClasses({ TestBasicSarosElements.class,
    TestChangingNameInRosterView.class, TestHandleContacts.class,
    TestShare2UsersSequentially.class, TestShare3UsersSequentially.class,
    TestShare3UsersConcurrently.class,
    TestShareProjectUsingExistingProject.class, TestEditDuringInvitation.class,
    TestSVNStateInitialization.class, TestFileOperations.class,
    TestFolderOperations.class,
    TestParallelInvitationWithTerminationByHost.class,
    TestParallelInvitationWithTerminationByInvitees.class,
    TestSVNStateUpdates.class })
public class AllSTFTests {
    /**
     * Run AllTests in the list of SuiteClasses annotation
     */
}
