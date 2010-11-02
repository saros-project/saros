package de.fu_berlin.inf.dpp.stf.client.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import de.fu_berlin.inf.dpp.stf.client.test.fileFolderOperations.TestFileOperations;
import de.fu_berlin.inf.dpp.stf.client.test.initialising.TestHandleContacts;
import de.fu_berlin.inf.dpp.stf.client.test.invitation.TestEditDuringInvitation;
import de.fu_berlin.inf.dpp.stf.client.test.invitation.TestShare2UsersSequentially;
import de.fu_berlin.inf.dpp.stf.client.test.invitation.TestShare3UsersConcurrently;
import de.fu_berlin.inf.dpp.stf.client.test.invitation.TestShare3UsersSequentially;
import de.fu_berlin.inf.dpp.stf.client.test.rolesAndFollowmode.TestFollowMode;

@RunWith(Suite.class)
@SuiteClasses({ TestBasicSarosElements.class, TestHandleContacts.class,
    TestShare2UsersSequentially.class, TestShare3UsersSequentially.class,
    TestShare3UsersConcurrently.class, TestEditDuringInvitation.class,
    TestFollowMode.class, TestFileOperations.class })
public class AllTests {
    /**
     * Run AllTests in the list of SuiteClasses annotation
     */
}
