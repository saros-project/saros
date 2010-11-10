package de.fu_berlin.inf.dpp.stf.client.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import de.fu_berlin.inf.dpp.stf.client.test.basicElements.TestBasicSarosElements;
import de.fu_berlin.inf.dpp.stf.client.test.testcases.fileFolderOperations.TestFileOperations;
import de.fu_berlin.inf.dpp.stf.client.test.testcases.initialising.TestHandleContacts;
import de.fu_berlin.inf.dpp.stf.client.test.testcases.invitation.TestEditDuringInvitation;
import de.fu_berlin.inf.dpp.stf.client.test.testcases.invitation.TestShare2UsersSequentially;
import de.fu_berlin.inf.dpp.stf.client.test.testcases.invitation.TestShare3UsersConcurrently;
import de.fu_berlin.inf.dpp.stf.client.test.testcases.invitation.TestShare3UsersSequentially;

@RunWith(Suite.class)
@SuiteClasses({ TestBasicSarosElements.class, TestHandleContacts.class,
    TestShare2UsersSequentially.class, TestShare3UsersSequentially.class,
    TestShare3UsersConcurrently.class, TestEditDuringInvitation.class,
    TestFileOperations.class })
public class AllTests {
    /**
     * Run AllTests in the list of SuiteClasses annotation
     */
}
