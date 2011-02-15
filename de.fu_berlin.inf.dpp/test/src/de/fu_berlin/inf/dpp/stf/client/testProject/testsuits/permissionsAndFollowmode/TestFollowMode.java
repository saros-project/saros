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
        alice.openC.openClass(VIEW_PACKAGE_EXPLORER, PROJECT1, PKG1, CLS1);
        alice.bot().editor(CLS1_SUFFIX).setTextAndSave(CP1);
        bob.sarosSessionV.followThisBuddy(alice.jid);
        bob.bot().editor(CLS1_SUFFIX).waitUntilIsActive();
        assertTrue(bob.sarosSessionV.isInFollowModeNoGUI());
        assertTrue(bob.bot().editor(CLS1_SUFFIX).isActive());

        String clsContentOfAlice = alice.editor.getClassContent(PROJECT1, PKG1,
            CLS1);

        bob.editor.waitUntilClassContentsSame(PROJECT1, PKG1, CLS1,
            clsContentOfAlice);
        String clsContentOfBob = bob.editor.getClassContent(PROJECT1, PKG1,
            CLS1);
        assertTrue(clsContentOfBob.equals(clsContentOfAlice));

        alice.fileM.newClass(PROJECT1, PKG1, CLS2);
        bob.bot().editor(CLS2_SUFFIX).waitUntilIsActive();
        assertTrue(bob.bot().editor(CLS2_SUFFIX).isActive());

        alice.sarosSessionV.followThisBuddy(bob.jid);
        bob.bot().editor(CLS1_SUFFIX).activate();
        alice.bot().editor(CLS1_SUFFIX).waitUntilIsActive();
        assertTrue(alice.sarosSessionV.isInFollowModeNoGUI());
        assertTrue(alice.bot().editor(CLS1_SUFFIX).isActive());

        // bob.sarosSessionV.followThisBuddy(alice.jid);
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
