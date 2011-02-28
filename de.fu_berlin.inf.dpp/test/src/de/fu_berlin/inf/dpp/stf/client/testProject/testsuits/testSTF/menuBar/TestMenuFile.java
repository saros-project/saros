package de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.testSTF.menuBar;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.STFTest;

public class TestMenuFile extends STFTest {

    @BeforeClass
    public static void runBeforeClass() throws RemoteException {
        initTesters(TypeOfTester.ALICE);
        setUpWorkbench();
    }

    @After
    public void runAfterEveryTest() throws RemoteException {
        deleteAllProjectsByActiveTesters();
    }

    /**********************************************
     * 
     * test all related actions with the sub menus of the context menu "New"
     * 
     **********************************************/
    @Test
    public void testNewProject() throws RemoteException {
        alice.sarosBot().file().newProject(PROJECT1);
        assertTrue(alice.sarosBot().state().existsProjectNoGUI(PROJECT1));
    }

    @Test
    public void testNewJavaProject() throws RemoteException {
        alice.sarosBot().file().newJavaProject(PROJECT1);
        assertTrue(alice.sarosBot().state().existsProjectNoGUI(PROJECT1));
    }

    @Test
    public void testNewFolder() throws RemoteException {
        alice.sarosBot().file().newJavaProject(PROJECT1);
        alice.sarosBot().file().newFolder(PROJECT1, FOLDER1);
        alice.sarosBot().file().newFolder(PROJECT1, FOLDER1, FOLDER2);
        assertTrue(alice.sarosBot().state()
            .existsFolderNoGUI(PROJECT1, FOLDER1));
        assertTrue(alice.sarosBot().state()
            .existsFolderNoGUI(PROJECT1, FOLDER1, FOLDER2));
        alice.noBot().deleteFolderNoGUI(PROJECT1, FOLDER1, FOLDER2);
        assertFalse(alice.sarosBot().state()
            .existsFolderNoGUI(PROJECT1, FOLDER1, FOLDER2));
        alice.noBot().deleteFolderNoGUI(PROJECT1, FOLDER1);
        assertFalse(alice.sarosBot().state()
            .existsFolderNoGUI(PROJECT1, FOLDER1));
    }

    @Test
    public void testNewPackage() throws RemoteException {
        alice.sarosBot().file().newJavaProject(PROJECT1);
        alice.sarosBot().file().newPackage(PROJECT1, PKG1);
        alice.sarosBot().file().newPackage(PROJECT1, PKG1 + ".subpkg");
        assertTrue(alice.sarosBot().state().existsPkgNoGUI(PROJECT1, PKG1));
        assertTrue(alice.sarosBot().state()
            .existsPkgNoGUI(PROJECT1, PKG1 + ".subpkg"));
        alice.noBot().deletePkgNoGUI(PROJECT1, PKG1 + ".subpkg");
        assertFalse(alice.sarosBot().state()
            .existsPkgNoGUI(PROJECT1, PKG1 + ".subpkg"));
        alice.noBot().deletePkgNoGUI(PROJECT1, PKG1);
        assertFalse(alice.sarosBot().state().existsPkgNoGUI(PROJECT1, PKG1));
    }

    @Test
    public void testNewFile() throws RemoteException {
        alice.sarosBot().file().newProject(PROJECT1);
        alice.sarosBot().file().newFolder(PROJECT1, FOLDER1);
        alice.sarosBot().file().newFile(PROJECT1, FOLDER1, FILE1);
        assertTrue(alice.sarosBot().state()
            .existsFileNoGUI(PROJECT1, FOLDER1, FILE1));
    }

    @Test
    public void testNewClass() throws RemoteException {
        alice.sarosBot().file().newJavaProject(PROJECT1);
        alice.sarosBot().file().newClass(PROJECT1, PKG1, CLS1);
        assertTrue(alice.sarosBot().state()
            .existsClassNoGUI(PROJECT1, PKG1, CLS1));
    }

    @Test
    @Ignore
    public void test_newProjectWithClass() throws RemoteException {
        assertFalse(alice.sarosBot().state().existsProjectNoGUI(PROJECT1));
        alice.sarosBot().file()
            .newJavaProjectWithClasses(PROJECT1, "pkg", "Cls");
        assertTrue(alice.sarosBot().state().existsProjectNoGUI(PROJECT1));
        assertTrue(alice.sarosBot().state()
            .existsClassNoGUI(PROJECT1, "pkg", "Cls"));
    }

    @Test
    @Ignore
    public void test_newProjectWithClass_2() throws RemoteException {
        assertFalse(alice.sarosBot().state().existsProjectNoGUI(PROJECT1));
        alice.sarosBot().file()
            .newJavaProjectWithClasses(PROJECT1, "pkg", "Cls");
        assertTrue(alice.sarosBot().state().existsProjectNoGUI(PROJECT1));
        assertTrue(alice.sarosBot().state()
            .existsClassNoGUI(PROJECT1, "pkg", "Cls"));
    }

    @Test
    @Ignore
    public void test_newProjectWithClass_3() throws RemoteException {
        assertFalse(alice.sarosBot().state().existsProjectNoGUI(PROJECT1));
        alice.sarosBot().file()
            .newJavaProjectWithClasses(PROJECT1, "pkg", "Cls");
        assertTrue(alice.sarosBot().state().existsProjectNoGUI(PROJECT1));
        assertTrue(alice.sarosBot().state()
            .existsClassNoGUI(PROJECT1, "pkg", "Cls"));
    }

    @Test
    @Ignore
    public void test_newJavaClassInProject() throws RemoteException {
        final String pkg = "pkg";
        final String className = "Cls";

        log.trace("alice.isJavaProjectExist()");
        assertFalse(alice.sarosBot().state().existsProjectNoGUI(PROJECT1));
        log.trace("alice.newProjectWithClass()");

        alice.sarosBot().file().newJavaProject(PROJECT1);
        alice.sarosBot().file().newClass(PROJECT1, pkg, className);
        log.trace("alice.isJavaProjectExist()");
        assertTrue(alice.sarosBot().state().existsProjectNoGUI(PROJECT1));
        log.trace("alice.isJavaClassExist()");
        assertTrue(alice.sarosBot().state()
            .existsClassNoGUI(PROJECT1, pkg, className));

        log.trace("alice.isJavaClassExist()");
        final String className2 = "Cls2";
        assertFalse(alice.sarosBot().state()
            .existsClassNoGUI(PROJECT1, pkg, className2));
        log.trace("alice.newJavaClassInProject()");
        alice.sarosBot().file().newClass(PROJECT1, pkg, className2);

        log.trace("alice.isJavaClassExist()");
        assertTrue(alice.sarosBot().state()
            .existsClassNoGUI(PROJECT1, pkg, className2));

        log.trace("deleteResource()");
        alice.noBot().deleteProjectNoGUI(PROJECT1);
        log.trace("alice.isJavaProjectExist()");
        assertFalse(alice.sarosBot().state().existsProjectNoGUI(PROJECT1));
    }

    @Test
    @Ignore
    public void testNewFolderInEmptyJavaProject() throws RemoteException {
        alice.sarosBot().file().newJavaProject(PROJECT1);
        // alice.buildSessionSequential(PROJECT,
        // SarosConstant.CONTEXT_MENU_SHARE_PROJECT, bob);
        alice.sarosBot().file().newFolder(PROJECT1, FOLDER1);
        // bob.bot.waitUntilFolderExist(PROJECT, FOLDER);
        // assertTrue(bob.bot.isFolderExist(PROJECT, FOLDER));
    }

    @Test
    @Ignore
    public void testNewFileNewFolderInEmptyProject() throws RemoteException {
        alice.sarosBot().file().newProject(PROJECT1);
        assertTrue(alice.sarosBot().state().existsProjectNoGUI(PROJECT1));
        // alice.buildSessionSequential(PROJECT,
        // SarosConstant.CONTEXT_MENU_SHARE_PROJECT, bob);
        alice.sarosBot().file().newFolder(PROJECT1, FOLDER1);
        // bob.bot.waitUntilFolderExist(PROJECT, FOLDER);
        // assertTrue(bob.bot.isFolderExist(PROJECT, FOLDER));
        alice.sarosBot().file().newFile(PROJECT1, FOLDER1, FILE1);
        // bob.bot.waitUntilFileExist(PROJECT, FOLDER, FILE);
        // assertTrue(bob.bot.isFileExist(PROJECT, FOLDER, FILE));
    }
}
