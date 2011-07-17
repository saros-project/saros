package de.fu_berlin.inf.dpp.stf.test.session;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.BOB;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.CARL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.StfTestCase;
import de.fu_berlin.inf.dpp.stf.client.tester.AbstractTester;
import de.fu_berlin.inf.dpp.stf.client.util.Util;
import de.fu_berlin.inf.dpp.stf.shared.Constants.TypeOfCreateProject;

public class CreateSameFileAtOnce extends StfTestCase {

    @BeforeClass
    public static void selectTesters() throws Exception {
        select(ALICE, BOB, CARL);
    }

    @Test
    public void testCreateSameFileAtOnce() throws Exception {
        ALICE.superBot().internal().createProject("foo");
        Util.buildSessionSequentially("foo", TypeOfCreateProject.NEW_PROJECT,
            ALICE, BOB, CARL);

        BOB.superBot().views().packageExplorerView()
            .waitUntilResourceIsShared("foo");
        CARL.superBot().views().packageExplorerView()
            .waitUntilResourceIsShared("foo");

        if ((System.currentTimeMillis() & 1L) != 0L) {
            BOB.superBot().internal()
                .createFile("foo", "readme.txt", BOB.toString());
            CARL.superBot().internal()
                .createFile("foo", "readme.txt", CARL.toString());
        } else {
            CARL.superBot().internal()
                .createFile("foo", "readme.txt", BOB.toString());
            BOB.superBot().internal()
                .createFile("foo", "readme.txt", CARL.toString());
        }

        ALICE.remoteBot().sleep(5000);
        ALICE.superBot().views().packageExplorerView()
            .selectFile("foo", "readme.txt").open();
        ALICE.remoteBot().editor("readme.txt").waitUntilIsActive();

        String aliceText = ALICE.remoteBot().editor("readme.txt").getText();

        BOB.superBot().views().packageExplorerView()
            .selectFile("foo", "readme.txt").open();
        BOB.remoteBot().editor("readme.txt").waitUntilIsActive();

        String bobText = BOB.remoteBot().editor("readme.txt").getText();

        CARL.superBot().views().packageExplorerView()
            .selectFile("foo", "readme.txt").open();
        CARL.remoteBot().editor("readme.txt").waitUntilIsActive();

        String carlText = CARL.remoteBot().editor("readme.txt").getText();

        if (bobText.equals(aliceText) && carlText.equals(aliceText))
            return; // already corrected

        AbstractTester tester = null;

        if (aliceText.equals(bobText))
            tester = CARL;
        else if (aliceText.equals(carlText))
            tester = BOB;
        else {
            fail("the content of Alice editor: " + aliceText
                + " is not expected, it must be '" + BOB.toString() + "' or '"
                + CARL.toString() + "'");
            return; // just for get rid of the null pointer warning
        }

        tester.superBot().views().sarosView()
            .waitUntilIsInconsistencyDetected();
        tester.superBot().views().sarosView().resolveInconsistency();

        tester.superBot().views().packageExplorerView()
            .selectFile("foo", "readme.txt").open();
        tester.remoteBot().editor("readme.txt").waitUntilIsActive();

        String repairedText = tester.remoteBot().editor("readme.txt").getText();
        assertEquals(aliceText, repairedText);
    }

}
