package de.fu_berlin.inf.dpp.stf.test.permissionsandfollowmode;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.BOB;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.rmi.RemoteException;

import org.eclipse.core.runtime.CoreException;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.StfTestCase;
import de.fu_berlin.inf.dpp.stf.client.util.Util;
import de.fu_berlin.inf.dpp.stf.test.Constants;

public class FollowModeTest extends StfTestCase {

    @BeforeClass
    public static void runBeforeClass() throws RemoteException {
        initTesters(ALICE, BOB);
        setUpWorkbench();
        setUpSaros();
        Util.setUpSessionWithAJavaProjectAndAClass(Constants.PROJECT1,
            Constants.PKG1, Constants.CLS1, ALICE, BOB);
    }

    @After
    public void runAfterEveryTest() throws RemoteException {
        Util.resetFollowModeSequentially(BOB, ALICE);
    }

    /**
     * 
     * @throws IOException
     * @throws CoreException
     */
    @Test
    public void testBobFollowAlice() throws IOException, CoreException {
        ALICE.superBot().views().packageExplorerView()
            .selectClass(Constants.PROJECT1, Constants.PKG1, Constants.CLS1)
            .open();
        ALICE.remoteBot().editor(Constants.CLS1_SUFFIX)
            .setTextFromFile(Constants.CP1);
        BOB.superBot().views().sarosView().selectParticipant(ALICE.getJID())
            .followParticipant();
        BOB.remoteBot().editor(Constants.CLS1_SUFFIX).waitUntilIsActive();
        assertTrue(BOB.superBot().views().sarosView()
            .selectParticipant(ALICE.getJID()).isFollowing());
        assertTrue(BOB.remoteBot().editor(Constants.CLS1_SUFFIX).isActive());

        String clsContentOfAlice = ALICE
            .superBot()
            .views()
            .packageExplorerView()
            .getFileContent(
                Util.getClassPath(Constants.PROJECT1, Constants.PKG1,
                    Constants.CLS1));

        BOB.superBot()
            .views()
            .packageExplorerView()
            .waitUntilFileContentSame(
                clsContentOfAlice,
                Util.getClassPath(Constants.PROJECT1, Constants.PKG1,
                    Constants.CLS1));
        String clsContentOfBob = BOB
            .superBot()
            .views()
            .packageExplorerView()
            .getFileContent(
                Util.getClassPath(Constants.PROJECT1, Constants.PKG1,
                    Constants.CLS1));
        assertTrue(clsContentOfBob.equals(clsContentOfAlice));

        ALICE.superBot().views().packageExplorerView().tree().newC()
            .cls(Constants.PROJECT1, Constants.PKG1, Constants.CLS2);
        BOB.remoteBot().editor(Constants.CLS2_SUFFIX).waitUntilIsActive();
        assertTrue(BOB.remoteBot().editor(Constants.CLS2_SUFFIX).isActive());

        ALICE.superBot().views().sarosView().selectParticipant(BOB.getJID())
            .followParticipant();
        BOB.remoteBot().editor(Constants.CLS1_SUFFIX).show();
        ALICE.remoteBot().editor(Constants.CLS1_SUFFIX).waitUntilIsActive();
        assertTrue(ALICE.superBot().views().sarosView()
            .selectParticipant(BOB.getJID()).isFollowing());
        assertTrue(ALICE.remoteBot().editor(Constants.CLS1_SUFFIX).isActive());

        // BOB.sarosBot().sessionView().followThisBuddy(ALICE.jid);
        // ALICE.fileM.newClass(PROJECT1, PKG1, CLS3);
        // ALICE.editor.waitUntilJavaEditorActive(CLS3);
        // ALICE.bot().editor(CLS3_SUFFIX).setTextAndSave(CP3);
        // ALICE.editor.setBreakPoint(13, PROJECT1, PKG1, CLS3);
        // ALICE.debugJavaFile(BotConfiguration.PROJECTNAME,
        // BotConfiguration.PACKAGENAME, BotConfiguration.CLASSNAME3);
        // BOB.waitUntilJavaEditorActive(BotConfiguration.CLASSNAME3);
        // assertFalse(BOB.isDebugPerspectiveActive());
        // ALICE.openJavaPerspective();
        // BOB.sleep(1000);
        // int lineFromAlice = ALICE.getJavaCursorLinePosition(
        // BotConfiguration.PROJECTNAME, BotConfiguration.PACKAGENAME,
        // BotConfiguration.CLASSNAME3);
        // int lineFromBob = BOB.getJavaCursorLinePosition(
        // BotConfiguration.PROJECTNAME, BotConfiguration.PACKAGENAME,
        // BotConfiguration.CLASSNAME3);
        // assertEquals(lineFromAlice, lineFromBob);
        // ALICE.waitUntilShellActive("Confirm Perspective Switch");
        // assertTrue(ALICE.isShellActive("Confirm Perspective Switch"));
    }
}
