package de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.testSTF;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.rmi.RemoteException;

import org.eclipse.core.runtime.CoreException;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.STFTest;

public class RmiTest extends STFTest {

    @BeforeClass
    public static void runBeforeClass() throws RemoteException {
        initTesters(TypeOfTester.ALICE);
        setUpWorkbench();
    }

    // @Test
    // public void testGiveExclusiveRole() throws RemoteException {
    // alice.bot.newJavaProject(PROJECT);
    // alice.buildSessionSequential(PROJECT,
    // SarosConstant.CONTEXT_MENU_SHARE_PROJECT, bob);
    // alice.bot.giveExclusiveDriverRole(bob.getPlainJid());
    // assertTrue(bob.state.isExclusiveDriver());
    // assertFalse(alice.state.isDriver(alice.jid));
    // }
    //
    // @Test
    // @Ignore
    // public void testCancelInvitationByPeer() throws RemoteException {
    // alice.bot.newJavaProject(PROJECT);
    // List<String> peersName = new LinkedList<String>();
    // peersName.add(bob.getPlainJid());
    // alice.bot.shareProject(PROJECT, peersName);
    // bob.bot.waitUntilShellActive("Session Invitation");
    // bob.bot.confirmSessionInvitationWindowStep1();
    // bob.bot.clickButton(SarosConstant.BUTTON_CANCEL);
    // alice.bot.waitUntilShellActive("Problem Occurred");
    // assertTrue(alice.bot.getSecondLabelOfProblemOccurredWindow().matches(
    // bob.getName() + ".*"));
    // alice.bot.clickButton(SarosConstant.BUTTON_OK);
    // }
    //
    //

    @Test
    @Ignore
    public void testNewTextFileLineDelimiter() throws RemoteException {
        alice.windowM.setNewTextFileLineDelimiter("Unix");
        System.out.println(alice.windowM.getTextFileLineDelimiter());
        assertTrue(alice.windowM.getTextFileLineDelimiter().equals("Unix"));
    }

    // @Test
    // @Ignore
    // public void testExistProgress() throws RemoteException {
    // assertFalse(alice.bot.existPorgress());
    // alice.bot.newJavaProjectWithClass(PROJECT, PKG, CLS);
    // List<String> inviteeJIDs = new ArrayList<String>();
    // inviteeJIDs.add(bob.getPlainJid());
    // alice.bot.shareProject(PROJECT, inviteeJIDs);
    // assertTrue(alice.bot.existPorgress());
    //
    // alice.bot.leaveSessionByHost();
    //
    // }

    @Test
    @Ignore
    public void testCloseEditorWithSave() throws IOException, CoreException {
        alice.fileM.newJavaProjectWithClasses(PROJECT1, PKG1, CLS1);
        alice.editor.setTextInJavaEditorWithoutSave(CP1, PROJECT1, PKG1, CLS1);
        String dirtyClsContentOfAlice = alice.editor.getTextOfJavaEditor(
            PROJECT2, PKG1, CLS1);
        alice.editor.closeJavaEditorWithSave(CLS1);
        String clsContentOfAlice = alice.editor.getClassContent(PROJECT1, PKG1,
            CLS1);
        assertTrue(dirtyClsContentOfAlice.equals(clsContentOfAlice));
    }

    @Test
    @Ignore
    public void testCloseEditorWithoutSave() throws IOException, CoreException {
        alice.fileM.newJavaProjectWithClasses(PROJECT1, PKG1, CLS1);
        alice.editor.setTextInJavaEditorWithoutSave(CP1, PROJECT1, PKG1, CLS1);
        String dirtyClsContentOfAlice = alice.editor.getTextOfJavaEditor(
            PROJECT2, PKG1, CLS1);
        alice.editor.closejavaEditorWithoutSave(CLS1);
        String clsContentOfAlice = alice.editor.getClassContent(PROJECT1, PKG1,
            CLS1);
        assertFalse(dirtyClsContentOfAlice.equals(clsContentOfAlice));
    }

    @Test
    public void testIsClassDirty() throws RemoteException {
        alice.fileM.newJavaProjectWithClasses(PROJECT1, PKG1, CLS1);
        assertFalse(alice.editor.isClassDirty(PROJECT1, PKG1, CLS1,
            ID_JAVA_EDITOR));
        alice.editor.setTextInJavaEditorWithSave(CP1, PROJECT1, PKG1, CLS1);
        assertTrue(alice.editor.isClassDirty(PROJECT1, PKG1, CLS1,
            ID_JAVA_EDITOR));
    }

    @Test
    @Ignore
    public void testIsClassesSame() throws RemoteException, CoreException,
        IOException {
        alice.fileM.newJavaProjectWithClasses(PROJECT1, PKG1, CLS1);
        alice.fileM.newClass(PROJECT1, PKG2, CLS1);
        String clsOfPkgProject = alice.editor.getClassContent(PROJECT1, PKG1,
            CLS1);
        String clsOfpkg2Project = alice.editor.getClassContent(PROJECT1, PKG2,
            CLS1);
        assertFalse(clsOfPkgProject.equals(clsOfpkg2Project));

        alice.fileM.newJavaProjectWithClasses(PROJECT2, PKG1, CLS1);
        String clsOfPkgProject2 = alice.editor.getClassContent(PROJECT2, PKG1,
            CLS1);
        assertTrue(clsOfPkgProject.equals(clsOfPkgProject2));
    }

    @Test
    @Ignore
    public void testTypeTextInEditor() throws RemoteException {
        alice.fileM.newJavaProject(PROJECT1);
        alice.fileM.newClassImplementsRunnable(PROJECT1, "pkg", "Cls");
        alice.editor.typeTextInJavaEditor(CP1, PROJECT1, "pkg", "Cls");
    }

    @Test
    @Ignore
    public void testPerspective() throws RemoteException {
        assertTrue(alice.windowM.isJavaPerspectiveActive());
        assertFalse(alice.windowM.isDebugPerspectiveActive());
        alice.windowM.openPerspectiveDebug();
        assertFalse(alice.windowM.isJavaPerspectiveActive());
        assertTrue(alice.windowM.isDebugPerspectiveActive());
    }

    @Test
    @Ignore
    public void testViews() throws RemoteException {
        alice.view.closeViewById(VIEW_SAROS_BUDDIES_ID);
        assertFalse(alice.view.isViewOpen(VIEW_SAROS_BUDDIES));
        alice.view.openViewById(VIEW_SAROS_BUDDIES_ID);
        assertTrue(alice.view.isViewOpen(VIEW_SAROS_BUDDIES));
    }

}
