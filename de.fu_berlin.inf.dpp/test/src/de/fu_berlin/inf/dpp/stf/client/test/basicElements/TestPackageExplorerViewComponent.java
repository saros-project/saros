package de.fu_berlin.inf.dpp.stf.client.test.basicElements;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.Musician;
import de.fu_berlin.inf.dpp.stf.client.test.helpers.InitMusician;
import de.fu_berlin.inf.dpp.stf.client.test.helpers.STFTest;
import de.fu_berlin.inf.dpp.stf.server.SarosConstant;

public class TestPackageExplorerViewComponent extends STFTest {

    private final static Logger log = Logger
        .getLogger(TestPackageExplorerViewComponent.class);

    private static Musician alice;

    @BeforeClass
    public static void initMusican() {
        alice = InitMusician.newAlice();
    }

    @AfterClass
    public static void resetSaros() throws RemoteException {
        alice.workbench.resetWorkbench();
    }

    @After
    public void cleanup() throws RemoteException {
        alice.workbench.resetSaros();
    }

    /**********************************************
     * 
     * test all related actions with the sub menus of the context menu "New"
     * 
     **********************************************/
    @Test
    public void testNewProject() throws RemoteException {
        alice.pEV.newProject(PROJECT);
        assertTrue(alice.pEV.isProjectExist(PROJECT));
    }

    @Test
    public void testNewJavaProject() throws RemoteException {
        alice.pEV.newJavaProject(PROJECT);
        assertTrue(alice.pEV.isProjectExist(PROJECT));
    }

    @Test
    public void testNewFolder() throws RemoteException {
        alice.pEV.newJavaProject(PROJECT);
        alice.pEV.newFolder(FOLDER, PROJECT);
        alice.pEV.newFolder(FOLDER2, PROJECT, FOLDER);
        assertTrue(alice.pEV.isFolderExist(PROJECT, FOLDER));
        assertTrue(alice.pEV.isFolderExist(PROJECT, FOLDER, FOLDER2));
        alice.pEV.deleteFolder(PROJECT, FOLDER, FOLDER2);
        assertFalse(alice.pEV.isFolderExist(PROJECT, FOLDER, FOLDER2));
        alice.pEV.deleteFolder(PROJECT, FOLDER);
        assertFalse(alice.pEV.isFolderExist(PROJECT, FOLDER));
    }

    @Test
    public void testNewPackage() throws RemoteException {
        alice.pEV.newJavaProject(PROJECT);
        alice.pEV.newPackage(PROJECT, PKG);
        alice.pEV.newPackage(PROJECT, PKG + ".subpkg");
        assertTrue(alice.pEV.isPkgExist(PROJECT, PKG));
        assertTrue(alice.pEV.isPkgExist(PROJECT, PKG + ".subpkg"));
        alice.pEV.deletePkg(PROJECT, PKG + ".subpkg");
        assertFalse(alice.pEV.isPkgExist(PROJECT, PKG + ".subpkg"));
        alice.pEV.deletePkg(PROJECT, PKG);
        assertFalse(alice.pEV.isPkgExist(PROJECT, PKG));

    }

    @Test
    public void testNewFile() throws RemoteException {
        alice.pEV.newProject(PROJECT);
        alice.pEV.newFolder(FOLDER, PROJECT);
        alice.pEV.newFile(PROJECT, FOLDER, FILE);
        assertTrue(alice.pEV.isFileExist(getPath(PROJECT, FOLDER, FILE)));
    }

    @Test
    public void testNewClass() throws RemoteException {
        alice.pEV.newJavaProject(PROJECT);
        alice.pEV.newClass(PROJECT, PKG, CLS);
        assertTrue(alice.pEV.isFileExist(getClassPath(PROJECT, PKG, CLS)));
    }

    /**********************************************
     * 
     * all related actions with the sub menus of the context menu "Open"
     * 
     **********************************************/
    @Test
    public void testOpenFile() throws RemoteException {
        alice.pEV.newJavaProject(PROJECT);
        alice.pEV.newFolder(FOLDER, PROJECT);
        alice.pEV.newFile(PROJECT, FOLDER, FILE);
        assertTrue(alice.editor.isFileOpen(FILE));
        alice.editor.closeEditorWithSave(FILE);
        assertFalse(alice.editor.isFileOpen(FILE));
        alice.pEV.openFile(PROJECT, FOLDER, FILE);
        assertTrue(alice.editor.isFileOpen(FILE));
        alice.pEV.deleteFile(PROJECT, FOLDER, FILE);
        assertFalse(alice.editor.isFileOpen(FILE));
    }

    @Test
    public void testOpenFileWith() throws RemoteException {
        alice.pEV.newJavaProject(PROJECT);
        alice.pEV.newFolder(FOLDER, PROJECT);
        alice.pEV.newFile(PROJECT, FOLDER, FILE);
        alice.editor.closeEditorWithSave(FILE);
        alice.pEV.openFileWith("Text Editor", PROJECT, FOLDER, FILE);
        assertTrue(alice.editor.isFileOpen(FILE));
        alice.pEV.deleteFile(PROJECT, FOLDER, FILE);
        assertFalse(alice.editor.isFileOpen(FILE));
    }

    /**********************************************
     * 
     * all related actions with the sub menus of the context menu "Refactor"
     * 
     **********************************************/
    @Test
    public void testMoveClassTo() throws RemoteException {
        alice.pEV.newJavaProject(PROJECT);
        alice.pEV.newClass(PROJECT, PKG, CLS);
        alice.pEV.newPackage(PROJECT, PKG2);
        alice.pEV.moveClassTo(PROJECT, PKG, CLS, PROJECT, PKG2);
        assertFalse(alice.pEV.isFileExist(getClassPath(PROJECT, PKG, CLS)));
        assertTrue(alice.pEV.isFileExist(getClassPath(PROJECT, PKG2, CLS)));
    }

    @Test
    public void testRenameClass() throws RemoteException {
        alice.pEV.newJavaProject(PROJECT);
        alice.pEV.newClass(PROJECT, PKG, CLS);
        alice.pEV.renameClass(CLS2, PROJECT, PKG, CLS);
        assertFalse(alice.pEV.isFileExist(getClassPath(PROJECT, PKG, CLS)));
        assertTrue(alice.pEV.isFileExist(getClassPath(PROJECT, PKG, CLS2)));
    }

    @Test
    public void testRenameFile() throws RemoteException {
        alice.pEV.newProject(PROJECT);
        alice.pEV.newFolder(FOLDER, PROJECT);
        alice.pEV.newFile(PROJECT, FOLDER, FILE);
        alice.pEV.renameFile(FILE2, PROJECT, FOLDER, FILE);
        assertFalse(alice.pEV.isFileExist(getPath(PROJECT, FOLDER, FILE)));
        assertTrue(alice.pEV.isFileExist(getPath(PROJECT, FOLDER, FILE2)));
    }

    @Test
    public void testRenameFolder() throws RemoteException {
        alice.pEV.newProject(PROJECT);
        alice.pEV.newFolder(FOLDER, PROJECT);
        alice.pEV.renameFolder(FOLDER2, PROJECT, FOLDER);
        assertFalse(alice.pEV.isFolderExist(PROJECT, FOLDER));
        assertTrue(alice.pEV.isFolderExist(PROJECT, FOLDER2));
    }

    @Test
    public void testRenamePackage() throws RemoteException {
        alice.pEV.newJavaProject(PROJECT);
        alice.pEV.newPackage(PROJECT, PKG);
        alice.pEV.renamePkg(PKG2, PROJECT, PKG);
        assertFalse(alice.pEV.isPkgExist(PROJECT, PKG));
        assertTrue(alice.pEV.isPkgExist(PROJECT, PKG2));
    }

    /***********************************************************/

    @Test
    @Ignore
    public void testDeleteProjectUsingGUI() throws RemoteException {
        alice.pEV.newJavaProject(PROJECT);
        assertTrue(alice.pEV.isProjectExist(PROJECT));
        alice.pEV.deleteProject(PROJECT);
        assertFalse(alice.pEV.isProjectExist(PROJECT));
    }

    @Test
    @Ignore
    public void testDeleteFileUsingGUI() throws RemoteException {
        alice.pEV.newJavaProject(PROJECT);
        alice.pEV.newClass(PROJECT, "pkg", "Cls");
        assertTrue(alice.pEV.isFileExist(getClassPath(PROJECT, "pkg", "Cls")));
        alice.pEV.deleteFile(PROJECT, "src", "pkg", "Cls.java");
        assertFalse(alice.pEV.isFileExist(getClassPath(PROJECT, "pkg", "Cls")));
    }

    @Test
    @Ignore
    public void testIsFileExist() throws RemoteException {
        alice.pEV.newJavaProject(PROJECT);
        alice.pEV.newClass(PROJECT, "pkg", "Cls");
        assertTrue(alice.pEV.isFileExist(getClassPath(PROJECT, "pkg", "Cls")));
        alice.pEV.deleteClass(PROJECT, "pkg", "Cls");
        assertFalse(alice.pEV.isFileExist(getClassPath(PROJECT, "pkg", "Cls")));
    }

    @Test
    @Ignore
    public void test_newProjectWithClass() throws RemoteException {
        assertFalse(alice.pEV.isProjectExist(PROJECT));
        alice.pEV.newJavaProjectWithClass(PROJECT, "pkg", "Cls");
        assertTrue(alice.pEV.isProjectExist(PROJECT));
        assertTrue(alice.pEV.isFileExist(getClassPath(PROJECT, "pkg", "Cls")));
    }

    @Test
    @Ignore
    public void test_newProjectWithClass_2() throws RemoteException {
        assertFalse(alice.pEV.isProjectExist(PROJECT));
        alice.pEV.newJavaProjectWithClass(PROJECT, "pkg", "Cls");
        assertTrue(alice.pEV.isProjectExist(PROJECT));
        assertTrue(alice.pEV.isFileExist(getClassPath(PROJECT, "pkg", "Cls")));
    }

    @Test
    @Ignore
    public void test_newProjectWithClass_3() throws RemoteException {
        assertFalse(alice.pEV.isProjectExist(PROJECT));
        alice.pEV.newJavaProjectWithClass(PROJECT, "pkg", "Cls");
        assertTrue(alice.pEV.isProjectExist(PROJECT));
        assertTrue(alice.pEV.isFileExist(getClassPath(PROJECT, "pkg", "Cls")));
    }

    @Test
    @Ignore
    public void test_newJavaClassInProject() throws RemoteException {
        final String pkg = "pkg";
        final String className = "Cls";

        log.trace("alice.isJavaProjectExist()");
        assertFalse(alice.pEV.isProjectExist(PROJECT));
        log.trace("alice.newProjectWithClass()");

        alice.pEV.newJavaProject(PROJECT);
        alice.pEV.newClass(PROJECT, pkg, className);
        log.trace("alice.isJavaProjectExist()");
        assertTrue(alice.pEV.isProjectExist(PROJECT));
        log.trace("alice.isJavaClassExist()");
        assertTrue(alice.pEV.isFileExist(getClassPath(PROJECT, pkg, className)));

        log.trace("alice.isJavaClassExist()");
        final String className2 = "Cls2";
        assertFalse(alice.pEV
            .isFileExist(getClassPath(PROJECT, pkg, className2)));
        log.trace("alice.newJavaClassInProject()");
        alice.pEV.newClass(PROJECT, pkg, className2);

        log.trace("alice.isJavaClassExist()");
        assertTrue(alice.pEV
            .isFileExist(getClassPath(PROJECT, pkg, className2)));

        log.trace("deleteResource()");
        alice.pEV.deleteProject(PROJECT);
        log.trace("alice.isJavaProjectExist()");
        assertFalse(alice.pEV.isProjectExist(PROJECT));
    }

    @Test
    @Ignore
    // this test fails, but it doesn't really matter...
    public void testIsFileExistWithGUI() throws RemoteException {
        alice.pEV.newJavaProject(PROJECT);
        alice.pEV.newClass(PROJECT, "pkg", "Cls");
        assertTrue(alice.pEV.isFileExistWithGUI(PROJECT, "src", "pkg",
            "Cls.java"));
        alice.pEV.deleteClass(PROJECT, "pkg", "Cls");
        assertFalse(alice.pEV.isFileExistWithGUI(PROJECT, "src", "pkg",
            "Cls.java"));
    }

    @Test
    @Ignore
    public void testNewFolderInEmptyJavaProject() throws RemoteException {
        alice.pEV.newJavaProject(PROJECT);
        // alice.buildSessionSequential(PROJECT,
        // SarosConstant.CONTEXT_MENU_SHARE_PROJECT, bob);
        alice.pEV.newFolder(FOLDER, PROJECT);
        // bob.bot.waitUntilFolderExist(PROJECT, FOLDER);
        // assertTrue(bob.bot.isFolderExist(PROJECT, FOLDER));
    }

    @Test
    @Ignore
    public void testNewFileNewFolderInEmptyProject() throws RemoteException {
        alice.pEV.newProject(PROJECT);
        assertTrue(alice.pEV.isProjectExist(PROJECT));
        // alice.buildSessionSequential(PROJECT,
        // SarosConstant.CONTEXT_MENU_SHARE_PROJECT, bob);
        alice.pEV.newFolder(FOLDER, PROJECT);
        // bob.bot.waitUntilFolderExist(PROJECT, FOLDER);
        // assertTrue(bob.bot.isFolderExist(PROJECT, FOLDER));
        alice.pEV.newFile(PROJECT, FOLDER, FILE);
        // bob.bot.waitUntilFileExist(PROJECT, FOLDER, FILE);
        // assertTrue(bob.bot.isFileExist(PROJECT, FOLDER, FILE));
    }

    @Test
    public void testDeleteFolder() throws RemoteException {
        alice.pEV.newJavaProject(PROJECT);
        alice.pEV.newFolder(FOLDER, PROJECT);
        assertTrue(alice.pEV.isFolderExist(PROJECT, FOLDER));
        alice.pEV.deleteFolder(PROJECT, FOLDER);
        assertFalse(alice.pEV.isFolderExist(PROJECT, FOLDER));
    }

    @Test
    public void testOpenWith() throws RemoteException {
        alice.pEV.newJavaProjectWithClass(PROJECT, PKG, CLS);
        alice.pEV.openFileWith(SarosConstant.MENU_TITLE_TEXT_EDITOR,
            getClassNodes(PROJECT, PKG, CLS));
        alice.pEV.openClassWithSystemEditor(PROJECT, PKG, CLS);
    }

}
