package de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.permissionsAndFollowmode;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.rmi.RemoteException;

import org.eclipse.core.runtime.CoreException;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.STFTest;

public class TestFollowMode extends STFTest {

    @BeforeClass
    public static void runBeforeClass() throws RemoteException,
        InterruptedException {
        initTesters(TypeOfTester.ALICE, TypeOfTester.BOB);
        setUpWorkbench();
        setUpSaros();
        setUpSessionWithAJavaProjectAndAClass(alice, bob);
    }

    @After
    public void runAfterEveryTest() throws RemoteException {
        resetFollowModeSequentially(bob, alice);
    }

    /**
     * 
     * @throws IOException
     * @throws CoreException
     */
    @Test
    public void testBobFollowAlice() throws IOException, CoreException {
        alice.sarosBot().views().packageExplorerView()
            .selectClass(PROJECT1, PKG1, CLS1).open();
        alice.bot().editor(CLS1_SUFFIX).setTexWithSave(CP1);
        bob.sarosBot().views().sessionView().selectParticipant(alice.getJID())
            .followThisBuddy();
        bob.bot().editor(CLS1_SUFFIX).waitUntilIsActive();
        assertTrue(bob.sarosBot().views().sessionView()
            .selectParticipant(alice.getJID()).isFollowingThisBuddy());
        assertTrue(bob.bot().editor(CLS1_SUFFIX).isActive());

        String clsContentOfAlice = alice.noBot().getFileContent(
            getClassPath(PROJECT1, PKG1, CLS1));

        bob.noBot().waitUntilFileContentSame(clsContentOfAlice,
            getClassPath(PROJECT1, PKG1, CLS1));
        String clsContentOfBob = bob.noBot().getFileContent(
            getClassPath(PROJECT1, PKG1, CLS1));
        assertTrue(clsContentOfBob.equals(clsContentOfAlice));

        alice.sarosBot().views().packageExplorerView().tree().newC()
            .cls(PROJECT1, PKG1, CLS2);
        bob.bot().editor(CLS2_SUFFIX).waitUntilIsActive();
        assertTrue(bob.bot().editor(CLS2_SUFFIX).isActive());

        alice.sarosBot().views().sessionView().selectParticipant(bob.getJID())
            .followThisBuddy();
        bob.bot().editor(CLS1_SUFFIX).show();
        alice.bot().editor(CLS1_SUFFIX).waitUntilIsActive();
        assertTrue(alice.sarosBot().views().sessionView()
            .selectParticipant(bob.getJID()).isFollowingThisBuddy());
        assertTrue(alice.bot().editor(CLS1_SUFFIX).isActive());

        // bob.sarosBot().sessionView().followThisBuddy(alice.jid);
        // alice.fileM.newClass(PROJECT1, PKG1, CLS3);
        // alice.editor.waitUntilJavaEditorActive(CLS3);
        // alice.bot().editor(CLS3_SUFFIX).setTextAndSave(CP3);
        // alice.editor.setBreakPoint(13, PROJECT1, PKG1, CLS3);
        // alice.debugJavaFile(BotConfiguration.PROJECTNAME,
        // BotConfiguration.PACKAGENAME, BotConfiguration.CLASSNAME3);
        // bob.waitUntilJavaEditorActive(BotConfiguration.CLASSNAME3);
        // assertFalse(bob.isDebugPerspectiveActive());
        // alice.openJavaPerspective();
        // bob.sleep(1000);
        // int lineFromAlice = alice.getJavaCursorLinePosition(
        // BotConfiguration.PROJECTNAME, BotConfiguration.PACKAGENAME,
        // BotConfiguration.CLASSNAME3);
        // int lineFromBob = bob.getJavaCursorLinePosition(
        // BotConfiguration.PROJECTNAME, BotConfiguration.PACKAGENAME,
        // BotConfiguration.CLASSNAME3);
        // assertEquals(lineFromAlice, lineFromBob);
        // alice.waitUntilShellActive("Confirm Perspective Switch");
        // assertTrue(alice.isShellActive("Confirm Perspective Switch"));
    }
}
