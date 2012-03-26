package de.fu_berlin.inf.dpp.stf.test.rosterviewbehaviour;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.BOB;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.CARL;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.annotation.TestLink;
import de.fu_berlin.inf.dpp.stf.client.StfTestCase;
import de.fu_berlin.inf.dpp.stf.client.util.Util;

@TestLink(id = "Saros-100_remove_users_from_roster")
public class RemoveUsersFromRosterView extends StfTestCase {

    @BeforeClass
    public static void selectTesters() throws Exception {
        select(ALICE, CARL, BOB);
    }

    @Test
    public void testRemoveUsersFromRosterView() throws Exception {

        Util.setUpSessionWithProjectAndFile("foo", "readme.txt", "Hello Bob",
            ALICE, BOB);
        ALICE.superBot().views().packageExplorerView()
            .waitUntilResourceIsShared("foo/readme.txt");

        try {
            ALICE.superBot().views().sarosView().selectBuddy(BOB.getJID())
                .delete();
            fail("Alice could remove Bob from her roster although both are in the same session");
        } catch (WidgetNotFoundException e) {
            try {
                ALICE.remoteBot().shell("Cannot delete a buddy in the session");
            } catch (WidgetNotFoundException e1) {
                fail("Alice could remove Bob from her roster although both are in the same session");
            }
        }

        ALICE.superBot().views().sarosView().selectBuddy(CARL.getJID())
            .delete();

        // wait for roster update
        ALICE.remoteBot().sleep(1000);

        assertFalse("Carl was not removed from the roster", ALICE.superBot()
            .views().sarosView().hasBuddy(CARL.getJID()));

    }
}