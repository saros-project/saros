package de.fu_berlin.inf.dpp.stf.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses( { RmiTest.class, TestBasicSarosElements.class,
    TestHandleContacts.class, TestShareProject.class,
    TestShareProject3Users.class })
public class AllTests {
    /**
     * Run AllTests in the list of SuiteClasses annotation
     */
}
