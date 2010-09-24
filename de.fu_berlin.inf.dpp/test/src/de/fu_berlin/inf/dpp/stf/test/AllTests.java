package de.fu_berlin.inf.dpp.stf.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

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
