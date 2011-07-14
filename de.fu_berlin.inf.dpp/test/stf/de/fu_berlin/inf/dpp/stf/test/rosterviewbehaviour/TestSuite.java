package de.fu_berlin.inf.dpp.stf.test.rosterviewbehaviour;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ ChangingNameInRosterViewTest.class,
    SortBuddiesOnlineOverOfflineTest.class, RemoveUsersFromRosterView.class })
public class TestSuite {
    // the class remains completely empty,
    // being used only as a holder for the above annotations
}