package de.fu_berlin.inf.dpp.stf.test.followmode;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.BOB;
import static org.junit.Assert.assertFalse;

import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.StfTestCase;
import de.fu_berlin.inf.dpp.stf.client.util.Util;
import de.fu_berlin.inf.dpp.stf.shared.Constants.TypeOfCreateProject;

public class FollowModeDisabledInNewSessionTest extends StfTestCase {

    @BeforeClass
    public static void selectTesters() throws Exception {
        select(ALICE, BOB);
    }

    @Test
    public void testFollowModeDisabledInNewSession() throws Exception {
        Util.setUpSessionWithProjectAndFile("foo", "readme.txt", "bla bla bla",
            ALICE, BOB);

        BOB.superBot().views().packageExplorerView()
            .waitUntilResourceIsShared("foo/readme.txt");

        ALICE.superBot().views().packageExplorerView()
            .selectFile("foo", "readme.txt").open();
        BOB.superBot().views().sarosView().selectParticipant(ALICE.getJID())
            .followParticipant();

        BOB.superBot().views().sarosView().selectParticipant(ALICE.getJID())
            .waitUntilIsFollowing();

        leaveSessionPeersFirst(ALICE);

        // if we close the editor before we leave the session all is fine

        BOB.remoteBot().closeAllEditors();
        BOB.superBot().internal().clearWorkspace();

        Util.buildSessionSequentially("foo", TypeOfCreateProject.NEW_PROJECT,
            ALICE, BOB);

        BOB.superBot().views().packageExplorerView()
            .waitUntilResourceIsShared("foo/readme.txt");

        ALICE.superBot().views().packageExplorerView()
            .selectFile("foo", "readme.txt").open();

        ALICE.remoteBot().sleep(1000);

        // this passes WTF !
        assertFalse("BOB is following ALICE", BOB.superBot().views()
            .sarosView().selectParticipant(ALICE.getJID()).isFollowing());

        // so a little hack

        boolean editorActive = true;

        try {
            editorActive = BOB.remoteBot().editor("readme.txt").isActive();
        } catch (Exception e) {
            editorActive = false;
        }

        assertFalse("BOB is following ALICE", editorActive);
    }
}