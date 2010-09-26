package de.fu_berlin.inf.dpp.stf.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import de.fu_berlin.inf.dpp.stf.test.initialising.TestHandleContacts;
import de.fu_berlin.inf.dpp.stf.test.invitation.TestEditDuringInvitation;
import de.fu_berlin.inf.dpp.stf.test.invitation.TestShare3UsersConcurrently;
import de.fu_berlin.inf.dpp.stf.test.invitation.TestShareProject;
import de.fu_berlin.inf.dpp.stf.test.invitation.TestShareProject3Users;
import de.fu_berlin.inf.dpp.stf.test.invitation.TestShareProjectUsingExistingProject;
import de.fu_berlin.inf.dpp.stf.test.rolesAndFollowmode.TestFollowMode;

@RunWith(Suite.class)
@SuiteClasses({ TestBasicSarosElements.class, TestHandleContacts.class,
    TestShareProject.class, TestShareProjectUsingExistingProject.class,
    TestShareProject3Users.class, TestShare3UsersConcurrently.class,
    TestEditDuringInvitation.class, TestFollowMode.class })
public class AllTests {
    /**
     * Run AllTests in the list of SuiteClasses annotation
     */
}
