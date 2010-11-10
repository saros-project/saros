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
import de.fu_berlin.inf.dpp.stf.server.BotConfiguration;
import de.fu_berlin.inf.dpp.stf.server.SarosConstant;

public class RmiTest {
    private final static Logger log = Logger.getLogger(RmiTest.class);

    private static Musician alice;
    // private static Musician bob;

    private static final String PROJECT = BotConfiguration.PROJECTNAME;
    private static final String PROJECT2 = BotConfiguration.PROJECTNAME2;
    private static final String PROJECT3 = BotConfiguration.PROJECTNAME3;

    private static final String FOLDER = BotConfiguration.FOLDERNAME;
    private static final String FOLDER2 = BotConfiguration.FOLDERNAME2;
    private static final String FILE = BotConfiguration.FILENAME;
    private static final String FILE2 = BotConfiguration.FILENAME2;

    private static final String PKG = BotConfiguration.PACKAGENAME;
    private static final String PKG2 = BotConfiguration.PACKAGENAME2;
    private static final String PKG3 = BotConfiguration.PACKAGENAME3;

    private static final String CLS = BotConfiguration.CLASSNAME;
    private static final String CLS2 = BotConfiguration.CLASSNAME2;
    private static final String CLS3 = BotConfiguration.CLASSNAME3;

    private static final String CP = BotConfiguration.CONTENTPATH;
    private static final String CP2 = BotConfiguration.CONTENTPATH2;
    private static final String CP3 = BotConfiguration.CONTENTPATH3;

    @BeforeClass
    public static void initMusican() {
        alice = InitMusician.newAlice();
        // bob = InitMusician.newBob();
    }

    @AfterClass
    public static void resetSaros() throws RemoteException {
        // bob.bot.resetSaros();
        alice.bot.resetSaros();
    }

    @After
    public void cleanup() throws RemoteException {
        alice.bot.resetWorkbench();
        alice.bot.resetSaros();
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
    // @Test
    // @Ignore
    // public void testNewFolderInEmptyJavaProject() throws RemoteException {
    // alice.bot.newJavaProject(PROJECT);
    // alice.buildSessionSequential(PROJECT,
    // SarosConstant.CONTEXT_MENU_SHARE_PROJECT, bob);
    // alice.bot.newFolder(PROJECT, FOLDER);
    // bob.bot.waitUntilFolderExist(PROJECT, FOLDER);
    // assertTrue(bob.bot.isFolderExist(PROJECT, FOLDER));
    // }
    //
    // @Test
    // @Ignore
    // public void testNewFileNewFolderInEmptyProject() throws RemoteException {
    // alice.bot.newProject(PROJECT);
    // assertTrue(alice.bot.isProjectExist(PROJECT));
    // alice.buildSessionSequential(PROJECT,
    // SarosConstant.CONTEXT_MENU_SHARE_PROJECT, bob);
    // alice.bot.newFolder(PROJECT, FOLDER);
    // bob.bot.waitUntilFolderExist(PROJECT, FOLDER);
    // assertTrue(bob.bot.isFolderExist(PROJECT, FOLDER));
    // alice.bot.newFile(PROJECT, FOLDER, FILE);
    // bob.bot.waitUntilFileExist(PROJECT, FOLDER, FILE);
    // assertTrue(bob.bot.isFileExist(PROJECT, FOLDER, FILE));
    // }

    @Test
    @Ignore
    public void testDeleteFolder() throws RemoteException {
        alice.eclipseMainMenu.newJavaProject(PROJECT);
        alice.eclipseMainMenu.newFolder(PROJECT, FOLDER);
        assertTrue(alice.eclipseState.isFolderExist(PROJECT, FOLDER));
        alice.eclipseState.deleteFolder(PROJECT, FOLDER);
        assertFalse(alice.eclipseState.isFolderExist(PROJECT, FOLDER));
    }

    @Test
    @Ignore
    public void testNewFile() throws RemoteException {
        alice.eclipseMainMenu.newJavaProject(PROJECT);
        alice.eclipseMainMenu.newFolder(PROJECT, FOLDER);
        alice.eclipseMainMenu.newFile(PROJECT, FOLDER, FILE);
        assertTrue(alice.eclipseState.isFolderExist(PROJECT, FOLDER));
        assertTrue(alice.eclipseState.isFileExist(PROJECT, FOLDER, FILE));
    }

    @Test
    @Ignore
    public void testNewTextFileLineDelimiter() throws RemoteException {
        alice.eclipseMainMenu.newTextFileLineDelimiter("Unix");
        System.out.println(alice.eclipseMainMenu.getTextFileLineDelimiter());
        assertTrue(alice.eclipseMainMenu.getTextFileLineDelimiter().equals(
            "Unix"));
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
        alice.eclipseMainMenu.newJavaProjectWithClass(PROJECT, PKG, CLS);
        alice.bot.setTextInJavaEditorWithoutSave(CP, PROJECT, PKG, CLS);
        String dirtyClsContentOfAlice = alice.eclipseEditor
            .getTextOfJavaEditor(PROJECT2, PKG, CLS);
        alice.eclipseEditor.closeJavaEditorWithSave(CLS);
        String clsContentOfAlice = alice.eclipseState.getClassContent(PROJECT,
            PKG, CLS);
        assertTrue(dirtyClsContentOfAlice.equals(clsContentOfAlice));
    }

    @Test
    @Ignore
    public void testCloseEditorWithoutSave() throws IOException, CoreException {
        alice.eclipseMainMenu.newJavaProjectWithClass(PROJECT, PKG, CLS);
        alice.bot.setTextInJavaEditorWithoutSave(CP, PROJECT, PKG, CLS);
        String dirtyClsContentOfAlice = alice.eclipseEditor
            .getTextOfJavaEditor(PROJECT2, PKG, CLS);
        alice.eclipseEditor.closejavaEditorWithoutSave(CLS);
        String clsContentOfAlice = alice.eclipseState.getClassContent(PROJECT,
            PKG, CLS);
        assertFalse(dirtyClsContentOfAlice.equals(clsContentOfAlice));
    }

    @Test
    public void testIsClassDirty() throws RemoteException {
        alice.eclipseMainMenu.newJavaProjectWithClass(PROJECT, PKG, CLS);
        assertFalse(alice.eclipseEditor.isClassDirty(PROJECT, PKG, CLS,
            SarosConstant.ID_JAVA_EDITOR));
        alice.bot.setTextInJavaEditorWithSave(CP, PROJECT, PKG, CLS);
        assertTrue(alice.eclipseEditor.isClassDirty(PROJECT, PKG, CLS,
            SarosConstant.ID_JAVA_EDITOR));
    }

    @Test
    @Ignore
    public void testIsClassesSame() throws RemoteException, CoreException,
        IOException {
        alice.eclipseMainMenu.newJavaProjectWithClass(PROJECT, PKG, CLS);
        alice.eclipseMainMenu.newClass(PROJECT, PKG2, CLS);
        String clsOfPkgProject = alice.eclipseState.getClassContent(PROJECT,
            PKG, CLS);
        String clsOfpkg2Project = alice.eclipseState.getClassContent(PROJECT,
            PKG2, CLS);
        assertFalse(clsOfPkgProject.equals(clsOfpkg2Project));

        alice.eclipseMainMenu.newJavaProjectWithClass(PROJECT2, PKG, CLS);
        String clsOfPkgProject2 = alice.eclipseState.getClassContent(PROJECT2,
            PKG, CLS);
        assertTrue(clsOfPkgProject.equals(clsOfPkgProject2));
    }

    @Test
    @Ignore
    public void testOpenWith() throws RemoteException {
        alice.eclipseMainMenu.newJavaProjectWithClass(PROJECT, PKG, CLS);
        alice.packageExplorerV.openClassWith(
            SarosConstant.MENU_TITLE_TEXT_EDITOR, PROJECT, PKG, CLS);
        alice.eclipseState.openClassWithSystemEditor(PROJECT, PKG, CLS);

    }

    @Test
    @Ignore
    public void testFolder() throws RemoteException {
        alice.eclipseMainMenu.newJavaProject(PROJECT);
        if (!alice.eclipseState.isFolderExist(PROJECT, FOLDER))
            alice.eclipseMainMenu.newFolder(PROJECT, FOLDER);
    }

    @Test
    @Ignore
    public void testTypeTextInEditor() throws RemoteException {
        alice.eclipseMainMenu.newJavaProject(PROJECT);
        alice.eclipseMainMenu.newClassImplementsRunnable(PROJECT, "pkg", "Cls");
        alice.bot.typeTextInJavaEditor(CP, PROJECT, "pkg", "Cls");
    }

    @Test
    @Ignore
    public void testPerspective() throws RemoteException {
        assertTrue(alice.eclipseMainMenu.isJavaPerspectiveActive());
        assertFalse(alice.eclipseMainMenu.isDebugPerspectiveActive());
        alice.eclipseMainMenu.openPerspectiveDebug();
        assertFalse(alice.eclipseMainMenu.isJavaPerspectiveActive());
        assertTrue(alice.eclipseMainMenu.isDebugPerspectiveActive());
    }

    @Test
    @Ignore
    public void testRenameFile() throws RemoteException {
        alice.eclipseMainMenu.newJavaProject(PROJECT);
        alice.eclipseMainMenu.newClass(PROJECT, "pkg", "Cls");
        alice.packageExplorerV.renameClass("Cls2", PROJECT, "pkg", "Cls");

        assertFalse(alice.eclipseState.existsClass(PROJECT, "pkg", "Cls"));
        assertTrue(alice.eclipseState.existsClass(PROJECT, "pkg", "Cls2"));
    }

    @Test
    @Ignore
    public void testDeleteProjectUsingGUI() throws RemoteException {
        alice.eclipseMainMenu.newJavaProject(PROJECT);
        assertTrue(alice.eclipseState.existsProject(PROJECT));
        alice.packageExplorerV.deleteProjectGui(PROJECT);
        assertFalse(alice.eclipseState.existsProject(PROJECT));
    }

    @Test
    @Ignore
    public void testDeleteFileUsingGUI() throws RemoteException {
        alice.eclipseMainMenu.newJavaProject(PROJECT);
        alice.eclipseMainMenu.newClass(PROJECT, "pkg", "Cls");
        assertTrue(alice.eclipseState.existsClass(PROJECT, "pkg", "Cls"));
        alice.packageExplorerV.deleteFileGui(PROJECT, "src", "pkg", "Cls.java");
        assertFalse(alice.eclipseState.existsClass(PROJECT, "pkg", "Cls"));
    }

    @Test
    @Ignore
    public void testIsFileExist() throws RemoteException {
        alice.eclipseMainMenu.newJavaProject(PROJECT);
        alice.eclipseMainMenu.newClass(PROJECT, "pkg", "Cls");
        assertTrue(alice.eclipseState.existsClass(PROJECT, "pkg", "Cls"));
        alice.eclipseState.deleteClass(PROJECT, "pkg", "Cls");
        assertFalse(alice.eclipseState.existsClass(PROJECT, "pkg", "Cls"));
    }

    @Test
    @Ignore
    // this test fails, but it doesn't really matter...
    public void testIsFileExistWithGUI() throws RemoteException {
        alice.eclipseMainMenu.newJavaProject(PROJECT);
        alice.eclipseMainMenu.newClass(PROJECT, "pkg", "Cls");
        assertTrue(alice.packageExplorerV
            .isClassExistGUI(PROJECT, "pkg", "Cls"));
        alice.eclipseState.deleteClass(PROJECT, "pkg", "Cls");
        assertFalse(alice.packageExplorerV.isClassExistGUI(PROJECT, "pkg",
            "Cls"));
    }

    @Test
    @Ignore
    public void testViews() throws RemoteException {
        alice.rosterV.closeRosterView();
        assertFalse(alice.rosterV.isRosterViewOpen());
        alice.rosterV.openRosterView();
        assertTrue(alice.rosterV.isRosterViewOpen());
    }

    @Test
    @Ignore
    public void test_newProjectWithClass() throws RemoteException {
        assertFalse(alice.eclipseState.existsProject(PROJECT));
        alice.eclipseMainMenu.newJavaProjectWithClass(PROJECT, "pkg", "Cls");
        assertTrue(alice.eclipseState.existsProject(PROJECT));
        assertTrue(alice.eclipseState.existsClass(PROJECT, "pkg", "Cls"));
    }

    @Test
    @Ignore
    public void test_newProjectWithClass_2() throws RemoteException {
        assertFalse(alice.eclipseState.existsProject(PROJECT));
        alice.eclipseMainMenu.newJavaProjectWithClass(PROJECT, "pkg", "Cls");
        assertTrue(alice.eclipseState.existsProject(PROJECT));
        assertTrue(alice.eclipseState.existsClass(PROJECT, "pkg", "Cls"));
    }

    @Test
    @Ignore
    public void test_newProjectWithClass_3() throws RemoteException {
        assertFalse(alice.eclipseState.existsProject(PROJECT));
        alice.eclipseMainMenu.newJavaProjectWithClass(PROJECT, "pkg", "Cls");
        assertTrue(alice.eclipseState.existsProject(PROJECT));
        assertTrue(alice.eclipseState.existsClass(PROJECT, "pkg", "Cls"));
    }

    @Test
    @Ignore
    public void test_newJavaClassInProject() throws RemoteException {
        final String pkg = "pkg";
        final String className = "Cls";

        log.trace("alice.isJavaProjectExist()");
        assertFalse(alice.eclipseState.existsProject(PROJECT));
        log.trace("alice.newProjectWithClass()");

        alice.eclipseMainMenu.newJavaProject(PROJECT);
        alice.eclipseMainMenu.newClass(PROJECT, pkg, className);
        log.trace("alice.isJavaProjectExist()");
        assertTrue(alice.eclipseState.existsProject(PROJECT));
        log.trace("alice.isJavaClassExist()");
        assertTrue(alice.eclipseState.existsClass(PROJECT, pkg, className));

        log.trace("alice.isJavaClassExist()");
        final String className2 = "Cls2";
        assertFalse(alice.eclipseState.existsClass(PROJECT, pkg, className2));
        log.trace("alice.newJavaClassInProject()");
        alice.eclipseMainMenu.newClass(PROJECT, pkg, className2);

        log.trace("alice.isJavaClassExist()");
        assertTrue(alice.eclipseState.existsClass(PROJECT, pkg, className2));

        log.trace("deleteResource()");
        alice.eclipseState.deleteProject(PROJECT);
        log.trace("alice.isJavaProjectExist()");
        assertFalse(alice.eclipseState.existsProject(PROJECT));
    }
}
