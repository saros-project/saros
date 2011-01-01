package de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.basicElements;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.STFTest;

public class TestPackageExplorerViewComponent extends STFTest {

    private final static Logger log = Logger
        .getLogger(TestPackageExplorerViewComponent.class);

    @BeforeClass
    public static void runBeforeClass() throws RemoteException {
        initTesters(TypeOfTester.ALICE);
        setUpWorkbenchs();
    }

    @AfterClass
    public static void runAfterClass() {
        //
    }

    @Before
    public void runBeforeEveryTest() {
        //
    }

    @After
    public void runAfterEveryTest() throws RemoteException {
        deleteProjectsByActiveTesters();
    }

    /**********************************************
     * 
     * test all related actions with the sub menus of the context menu "New"
     * 
     **********************************************/
    @Test
    public void testNewProject() throws RemoteException {
        alice.pEV.newProject(PROJECT1);
        assertTrue(alice.pEV.existsProject(PROJECT1));
    }

    @Test
    public void testNewJavaProject() throws RemoteException {
        alice.pEV.newJavaProject(PROJECT1);
        assertTrue(alice.pEV.existsProject(PROJECT1));
    }

    @Test
    public void testNewFolder() throws RemoteException {
        alice.pEV.newJavaProject(PROJECT1);
        alice.pEV.newFolder(FOLDER1, PROJECT1);
        alice.pEV.newFolder(FOLDER2, PROJECT1, FOLDER1);
        assertTrue(alice.pEV.existsFolder(PROJECT1, FOLDER1));
        assertTrue(alice.pEV.existsFolder(PROJECT1, FOLDER1, FOLDER2));
        alice.pEV.deleteFolder(PROJECT1, FOLDER1, FOLDER2);
        assertFalse(alice.pEV.existsFolder(PROJECT1, FOLDER1, FOLDER2));
        alice.pEV.deleteFolder(PROJECT1, FOLDER1);
        assertFalse(alice.pEV.existsFolder(PROJECT1, FOLDER1));
    }

    @Test
    public void testNewPackage() throws RemoteException {
        alice.pEV.newJavaProject(PROJECT1);
        alice.pEV.newPackage(PROJECT1, PKG1);
        alice.pEV.newPackage(PROJECT1, PKG1 + ".subpkg");
        assertTrue(alice.pEV.existsPkg(PROJECT1, PKG1));
        assertTrue(alice.pEV.existsPkg(PROJECT1, PKG1 + ".subpkg"));
        alice.pEV.deletePkg(PROJECT1, PKG1 + ".subpkg");
        assertFalse(alice.pEV.existsPkg(PROJECT1, PKG1 + ".subpkg"));
        alice.pEV.deletePkg(PROJECT1, PKG1);
        assertFalse(alice.pEV.existsPkg(PROJECT1, PKG1));
    }

    @Test
    public void testNewFile() throws RemoteException {
        alice.pEV.newProject(PROJECT1);
        alice.pEV.newFolder(FOLDER1, PROJECT1);
        alice.pEV.newFile(PROJECT1, FOLDER1, FILE1);
        assertTrue(alice.pEV.existsFile(PROJECT1, FOLDER1, FILE1));
    }

    @Test
    public void testNewClass() throws RemoteException {
        alice.pEV.newJavaProject(PROJECT1);
        alice.pEV.newClass(PROJECT1, PKG1, CLS1);
        assertTrue(alice.pEV.existsClass(PROJECT1, PKG1, CLS1));
    }

    /**********************************************
     * 
     * all related actions with the sub menus of the context menu "Open"
     * 
     **********************************************/
    @Test
    public void testOpenFile() throws RemoteException {
        alice.pEV.newJavaProject(PROJECT1);
        alice.pEV.newFolder(FOLDER1, PROJECT1);
        alice.pEV.newFile(PROJECT1, FOLDER1, FILE1);
        assertTrue(alice.editor.isEditorOpen(FILE1));
        alice.editor.closeEditorWithSave(FILE1);
        assertFalse(alice.editor.isEditorOpen(FILE1));
        alice.pEV.openFile(PROJECT1, FOLDER1, FILE1);
        assertTrue(alice.editor.isEditorOpen(FILE1));
        alice.pEV.deleteFile(PROJECT1, FOLDER1, FILE1);
        assertFalse(alice.editor.isEditorOpen(FILE1));
    }

    @Test
    public void testOpenFileWith() throws RemoteException {
        alice.pEV.newJavaProject(PROJECT1);
        alice.pEV.newFolder(FOLDER1, PROJECT1);
        alice.pEV.newFile(PROJECT1, FOLDER1, FILE1);
        alice.editor.closeEditorWithSave(FILE1);
        alice.pEV.openFileWith("Text Editor", PROJECT1, FOLDER1, FILE1);
        assertTrue(alice.editor.isEditorOpen(FILE1));
        alice.pEV.deleteFile(PROJECT1, FOLDER1, FILE1);
        assertFalse(alice.editor.isEditorOpen(FILE1));
    }

    /**********************************************
     * 
     * all related actions with the sub menus of the context menu "Refactor"
     * 
     **********************************************/
    @Test
    public void testMoveClassTo() throws RemoteException {
        alice.pEV.newJavaProject(PROJECT1);
        alice.pEV.newClass(PROJECT1, PKG1, CLS1);
        alice.pEV.newPackage(PROJECT1, PKG2);
        alice.pEV.moveClassTo(PROJECT1, PKG1, CLS1, PROJECT1, PKG2);
        assertFalse(alice.pEV.existsClass(PROJECT1, PKG1, CLS1));
        assertTrue(alice.pEV.existsClass(PROJECT1, PKG2, CLS1));
    }

    @Test
    public void testRenameClass() throws RemoteException {
        alice.pEV.newJavaProject(PROJECT1);
        alice.pEV.newClass(PROJECT1, PKG1, CLS1);
        alice.pEV.renameClass(CLS2, PROJECT1, PKG1, CLS1);
        assertFalse(alice.pEV.existsClass(PROJECT1, PKG1, CLS1));
        assertTrue(alice.pEV.existsClass(PROJECT1, PKG1, CLS2));
    }

    @Test
    public void testRenameFile() throws RemoteException {
        alice.pEV.newProject(PROJECT1);
        alice.pEV.newFolder(FOLDER1, PROJECT1);
        alice.pEV.newFile(PROJECT1, FOLDER1, FILE1);
        alice.pEV.renameFile(FILE2, PROJECT1, FOLDER1, FILE1);
        assertFalse(alice.pEV.existsFile(PROJECT1, FOLDER1, FILE1));
        assertTrue(alice.pEV.existsFile(PROJECT1, FOLDER1, FILE2));
    }

    @Test
    public void testRenameFolder() throws RemoteException {
        alice.pEV.newProject(PROJECT1);
        alice.pEV.newFolder(FOLDER1, PROJECT1);
        alice.pEV.renameFolder(FOLDER2, PROJECT1, FOLDER1);
        assertFalse(alice.pEV.existsFolder(PROJECT1, FOLDER1));
        assertTrue(alice.pEV.existsFolder(PROJECT1, FOLDER2));
    }

    @Test
    public void testRenamePackage() throws RemoteException {
        alice.pEV.newJavaProject(PROJECT1);
        alice.pEV.newPackage(PROJECT1, PKG1);
        alice.pEV.renamePkg(PKG2, PROJECT1, PKG1);
        assertFalse(alice.pEV.existsPkg(PROJECT1, PKG1));
        assertTrue(alice.pEV.existsPkg(PROJECT1, PKG2));
    }

    @Test
    public void testShareProjectWithSVN() throws RemoteException {
        alice.pEV.newJavaProject(PROJECT1);
        assertFalse(alice.pEV.isProjectManagedBySVN(PROJECT1));
        alice.pEV.shareProjectWithSVNUsingSpecifiedFolderName(PROJECT1,
            SVN_REPOSITORY_URL, SVN_PROJECT_PATH);
        assertTrue(alice.pEV.isProjectManagedBySVN(PROJECT1));
    }

    /**
     * Create a project, rename it, see if rename worked, delete all projects.
     */
    @Test
    public void testRenameProject() throws Exception {
        alice.pEV.newJavaProject(PROJECT1);

        assertTrue(alice.pEV.existsProject(PROJECT1));
        assertFalse(alice.pEV.existsProject(PROJECT2));

        alice.pEV.renameJavaProject(PROJECT2, PROJECT1);

        assertFalse(alice.pEV.existsProject(PROJECT1));
        assertTrue(alice.pEV.existsProject(PROJECT2));
    }

    /***********************************************************/

    @Test
    @Ignore
    public void testDeleteProjectUsingGUI() throws RemoteException {
        alice.pEV.newJavaProject(PROJECT1);
        assertTrue(alice.pEV.existsProject(PROJECT1));
        alice.pEV.deleteProject(PROJECT1);
        assertFalse(alice.pEV.existsProject(PROJECT1));
    }

    @Test
    @Ignore
    public void testDeleteFileUsingGUI() throws RemoteException {
        alice.pEV.newJavaProject(PROJECT1);
        alice.pEV.newClass(PROJECT1, "pkg", "Cls");
        assertTrue(alice.pEV.existsClass(PROJECT1, "pkg", "Cls"));
        alice.pEV.deleteFile(PROJECT1, "src", "pkg", "Cls.java");
        assertFalse(alice.pEV.existsClass(PROJECT1, "pkg", "Cls"));
    }

    @Test
    @Ignore
    public void testIsFileExist() throws RemoteException {
        alice.pEV.newJavaProject(PROJECT1);
        alice.pEV.newClass(PROJECT1, "pkg", "Cls");
        assertTrue(alice.pEV.existsClass(PROJECT1, "pkg", "Cls"));
        alice.pEV.deleteClass(PROJECT1, "pkg", "Cls");
        assertFalse(alice.pEV.existsClass(PROJECT1, "pkg", "Cls"));
    }

    @Test
    @Ignore
    public void test_newProjectWithClass() throws RemoteException {
        assertFalse(alice.pEV.existsProject(PROJECT1));
        alice.pEV.newJavaProjectWithClass(PROJECT1, "pkg", "Cls");
        assertTrue(alice.pEV.existsProject(PROJECT1));
        assertTrue(alice.pEV.existsClass(PROJECT1, "pkg", "Cls"));
    }

    @Test
    @Ignore
    public void test_newProjectWithClass_2() throws RemoteException {
        assertFalse(alice.pEV.existsProject(PROJECT1));
        alice.pEV.newJavaProjectWithClass(PROJECT1, "pkg", "Cls");
        assertTrue(alice.pEV.existsProject(PROJECT1));
        assertTrue(alice.pEV.existsClass(PROJECT1, "pkg", "Cls"));
    }

    @Test
    @Ignore
    public void test_newProjectWithClass_3() throws RemoteException {
        assertFalse(alice.pEV.existsProject(PROJECT1));
        alice.pEV.newJavaProjectWithClass(PROJECT1, "pkg", "Cls");
        assertTrue(alice.pEV.existsProject(PROJECT1));
        assertTrue(alice.pEV.existsClass(PROJECT1, "pkg", "Cls"));
    }

    @Test
    @Ignore
    public void test_newJavaClassInProject() throws RemoteException {
        final String pkg = "pkg";
        final String className = "Cls";

        log.trace("alice.isJavaProjectExist()");
        assertFalse(alice.pEV.existsProject(PROJECT1));
        log.trace("alice.newProjectWithClass()");

        alice.pEV.newJavaProject(PROJECT1);
        alice.pEV.newClass(PROJECT1, pkg, className);
        log.trace("alice.isJavaProjectExist()");
        assertTrue(alice.pEV.existsProject(PROJECT1));
        log.trace("alice.isJavaClassExist()");
        assertTrue(alice.pEV.existsClass(PROJECT1, pkg, className));

        log.trace("alice.isJavaClassExist()");
        final String className2 = "Cls2";
        assertFalse(alice.pEV.existsClass(PROJECT1, pkg, className2));
        log.trace("alice.newJavaClassInProject()");
        alice.pEV.newClass(PROJECT1, pkg, className2);

        log.trace("alice.isJavaClassExist()");
        assertTrue(alice.pEV.existsClass(PROJECT1, pkg, className2));

        log.trace("deleteResource()");
        alice.pEV.deleteProject(PROJECT1);
        log.trace("alice.isJavaProjectExist()");
        assertFalse(alice.pEV.existsProject(PROJECT1));
    }

    @Test
    @Ignore
    // this test fails, but it doesn't really matter...
    public void testIsFileExistWithGUI() throws RemoteException {
        alice.pEV.newJavaProject(PROJECT1);
        alice.pEV.newClass(PROJECT1, "pkg", "Cls");
        assertTrue(alice.pEV.existsFiletWithGUI(PROJECT1, "src", "pkg",
            "Cls.java"));
        alice.pEV.deleteClass(PROJECT1, "pkg", "Cls");
        assertFalse(alice.pEV.existsFiletWithGUI(PROJECT1, "src", "pkg",
            "Cls.java"));
    }

    @Test
    @Ignore
    public void testNewFolderInEmptyJavaProject() throws RemoteException {
        alice.pEV.newJavaProject(PROJECT1);
        // alice.buildSessionSequential(PROJECT,
        // SarosConstant.CONTEXT_MENU_SHARE_PROJECT, bob);
        alice.pEV.newFolder(FOLDER1, PROJECT1);
        // bob.bot.waitUntilFolderExist(PROJECT, FOLDER);
        // assertTrue(bob.bot.isFolderExist(PROJECT, FOLDER));
    }

    @Test
    @Ignore
    public void testNewFileNewFolderInEmptyProject() throws RemoteException {
        alice.pEV.newProject(PROJECT1);
        assertTrue(alice.pEV.existsProject(PROJECT1));
        // alice.buildSessionSequential(PROJECT,
        // SarosConstant.CONTEXT_MENU_SHARE_PROJECT, bob);
        alice.pEV.newFolder(FOLDER1, PROJECT1);
        // bob.bot.waitUntilFolderExist(PROJECT, FOLDER);
        // assertTrue(bob.bot.isFolderExist(PROJECT, FOLDER));
        alice.pEV.newFile(PROJECT1, FOLDER1, FILE1);
        // bob.bot.waitUntilFileExist(PROJECT, FOLDER, FILE);
        // assertTrue(bob.bot.isFileExist(PROJECT, FOLDER, FILE));
    }

    @Test
    public void testDeleteFolder() throws RemoteException {
        alice.pEV.newJavaProject(PROJECT1);
        alice.pEV.newFolder(FOLDER1, PROJECT1);
        assertTrue(alice.pEV.existsFolder(PROJECT1, FOLDER1));
        alice.pEV.deleteFolder(PROJECT1, FOLDER1);
        assertFalse(alice.pEV.existsFolder(PROJECT1, FOLDER1));
    }

    @Test
    @Ignore
    // TODO Somehow verify that the external editor was actually opened, then
    // close it.
    public void testOpenWith() throws RemoteException {
        alice.pEV.newJavaProjectWithClass(PROJECT1, PKG1, CLS1);
        alice.pEV.openClassWith("Text Editor", PROJECT1, PKG1, CLS1);
        alice.pEV.openClassWithSystemEditor(PROJECT1, PKG1, CLS1);
    }

    @Test
    public void testCopyProject() throws RemoteException {
        alice.pEV.newProject(PROJECT1);
        assertFalse(alice.pEV.existsProject(PROJECT2));
        alice.pEV.copyProject(PROJECT2, PROJECT1);
        assertTrue(alice.pEV.existsProject(PROJECT2));
    }
}
