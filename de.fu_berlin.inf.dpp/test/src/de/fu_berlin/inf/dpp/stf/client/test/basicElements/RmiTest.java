package de.fu_berlin.inf.dpp.stf.client.test.basicElements;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.rmi.RemoteException;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.CoreException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.Musician;
import de.fu_berlin.inf.dpp.stf.client.test.helpers.InitMusician;
import de.fu_berlin.inf.dpp.stf.client.test.helpers.STFTest;
import de.fu_berlin.inf.dpp.stf.server.SarosConstant;

public class RmiTest extends STFTest {
    private final static Logger log = Logger.getLogger(RmiTest.class);

    private static Musician alice;

    // private static Musician bob;

    @BeforeClass
    public static void initMusican() {
        alice = InitMusician.newAlice();
        // bob = InitMusician.newBob();
    }

    @AfterClass
    public static void resetSaros() throws RemoteException {
        // bob.bot.resetSaros();
        alice.workbench.resetSaros();
    }

    @After
    public void cleanup() throws RemoteException {
        alice.workbench.resetWorkbench();
        alice.workbench.resetSaros();
        // bob.bot.resetWorkbench();
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
        alice.mainMenu.newTextFileLineDelimiter("Unix");
        System.out.println(alice.mainMenu.getTextFileLineDelimiter());
        assertTrue(alice.mainMenu.getTextFileLineDelimiter().equals("Unix"));
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
        alice.pEV.newJavaProjectWithClass(PROJECT, PKG, CLS);
        alice.editor.setTextInJavaEditorWithoutSave(CP, PROJECT, PKG, CLS);
        String dirtyClsContentOfAlice = alice.editor.getTextOfJavaEditor(
            PROJECT2, PKG, CLS);
        alice.editor.closeJavaEditorWithSave(CLS);
        String clsContentOfAlice = alice.state.getClassContent(PROJECT, PKG,
            CLS);
        assertTrue(dirtyClsContentOfAlice.equals(clsContentOfAlice));
    }

    @Test
    @Ignore
    public void testCloseEditorWithoutSave() throws IOException, CoreException {
        alice.pEV.newJavaProjectWithClass(PROJECT, PKG, CLS);
        alice.editor.setTextInJavaEditorWithoutSave(CP, PROJECT, PKG, CLS);
        String dirtyClsContentOfAlice = alice.editor.getTextOfJavaEditor(
            PROJECT2, PKG, CLS);
        alice.editor.closejavaEditorWithoutSave(CLS);
        String clsContentOfAlice = alice.state.getClassContent(PROJECT, PKG,
            CLS);
        assertFalse(dirtyClsContentOfAlice.equals(clsContentOfAlice));
    }

    @Test
    public void testIsClassDirty() throws RemoteException {
        alice.pEV.newJavaProjectWithClass(PROJECT, PKG, CLS);
        assertFalse(alice.editor.isClassDirty(PROJECT, PKG, CLS,
            SarosConstant.ID_JAVA_EDITOR));
        alice.editor.setTextInJavaEditorWithSave(CP, PROJECT, PKG, CLS);
        assertTrue(alice.editor.isClassDirty(PROJECT, PKG, CLS,
            SarosConstant.ID_JAVA_EDITOR));
    }

    @Test
    @Ignore
    public void testIsClassesSame() throws RemoteException, CoreException,
        IOException {
        alice.pEV.newJavaProjectWithClass(PROJECT, PKG, CLS);
        alice.pEV.newClass(PROJECT, PKG2, CLS);
        String clsOfPkgProject = alice.state.getClassContent(PROJECT, PKG, CLS);
        String clsOfpkg2Project = alice.state.getClassContent(PROJECT, PKG2,
            CLS);
        assertFalse(clsOfPkgProject.equals(clsOfpkg2Project));

        alice.pEV.newJavaProjectWithClass(PROJECT2, PKG, CLS);
        String clsOfPkgProject2 = alice.state.getClassContent(PROJECT2, PKG,
            CLS);
        assertTrue(clsOfPkgProject.equals(clsOfPkgProject2));
    }

    @Test
    @Ignore
    public void testTypeTextInEditor() throws RemoteException {
        alice.pEV.newJavaProject(PROJECT);
        alice.pEV.newClassImplementsRunnable(PROJECT, "pkg", "Cls");
        alice.editor.typeTextInJavaEditor(CP, PROJECT, "pkg", "Cls");
    }

    @Test
    @Ignore
    public void testPerspective() throws RemoteException {
        assertTrue(alice.mainMenu.isJavaPerspectiveActive());
        assertFalse(alice.mainMenu.isDebugPerspectiveActive());
        alice.mainMenu.openPerspectiveDebug();
        assertFalse(alice.mainMenu.isJavaPerspectiveActive());
        assertTrue(alice.mainMenu.isDebugPerspectiveActive());
    }

    @Test
    @Ignore
    public void testViews() throws RemoteException {
        alice.rosterV.closeRosterView();
        assertFalse(alice.rosterV.isRosterViewOpen());
        alice.rosterV.openRosterView();
        assertTrue(alice.rosterV.isRosterViewOpen());
    }

}
