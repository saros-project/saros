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

        alice.superBot().views().packageExplorerView().tree().newC()
            .project(PROJECT1);
        assertTrue(alice.superBot().views().packageExplorerView().tree()
            .existsWithRegex(PROJECT1));
    }

    @Test
    public void testNewJavaProject() throws RemoteException {
        alice.superBot().views().packageExplorerView().tree().newC()
            .javaProject(PROJECT1);
        assertTrue(alice.superBot().views().packageExplorerView().tree()
            .existsWithRegex(PROJECT1));
    }

    @Test
    public void testNewFolder() throws RemoteException {
        alice.superBot().views().packageExplorerView().tree().newC()
            .javaProject(PROJECT1);
        alice.superBot().views().packageExplorerView()
            .selectJavaProject(PROJECT1).newC().folder(FOLDER1);
        alice.superBot().views().packageExplorerView()
            .selectFolder(PROJECT1, FOLDER1).newC().folder(FOLDER2);
        assertTrue(alice.superBot().views().packageExplorerView()
            .selectProject(PROJECT1).existsWithRegex(FOLDER1));
        assertTrue(alice.superBot().views().packageExplorerView()
            .selectFolder(PROJECT1, FOLDER1).existsWithRegex(FOLDER2));
        alice.superBot().views().packageExplorerView()
            .selectFolder(PROJECT1, FOLDER1, FOLDER2).delete();
        assertFalse(alice.superBot().views().packageExplorerView()
            .selectFolder(PROJECT1, FOLDER1).existsWithRegex(FOLDER2));
        alice.superBot().views().packageExplorerView()
            .selectFolder(PROJECT1, FOLDER1).delete();
        assertFalse(alice.superBot().views().packageExplorerView()
            .selectProject(PROJECT1).existsWithRegex(FOLDER1));
    }

    @Test
    public void testNewPackage() throws RemoteException {
        alice.superBot().views().packageExplorerView().tree().newC()
            .javaProject(PROJECT1);
        alice.superBot().views().packageExplorerView().tree().newC()
            .pkg(PROJECT1, PKG1);
        alice.superBot().views().packageExplorerView().tree().newC()
            .pkg(PROJECT1, PKG1 + ".subpkg");
        assertTrue(alice.superBot().views().packageExplorerView()
            .selectSrc(PROJECT1).existsWithRegex(PKG1));
        assertTrue(alice.superBot().views().packageExplorerView()
            .selectSrc(PROJECT1).existsWithRegex(PKG1 + ".subpkg"));
        alice.superBot().views().packageExplorerView()
            .selectPkg(PROJECT1, PKG1 + ".subpkg").delete();
        assertFalse(alice.superBot().views().packageExplorerView()
            .selectProject(PROJECT1).existsWithRegex(PKG1 + ".subpkg"));
    }

    @Test
    public void testNewpackage() throws RemoteException {
        alice.superBot().views().packageExplorerView().tree().newC()
            .project(PROJECT1);
        alice.superBot().views().packageExplorerView().selectProject(PROJECT1)
            .newC().folder(FOLDER1);
        alice.superBot().views().packageExplorerView()
            .selectFolder(PROJECT1, FOLDER1).newC().file(FILE1);
        assertTrue(alice.superBot().views().packageExplorerView()
            .selectFolder(PROJECT1, FOLDER1).existsWithRegex(FILE1));
    }

    @Test
    public void testNewClass() throws RemoteException {
        alice.superBot().views().packageExplorerView().tree().newC()
            .javaProject(PROJECT1);
        alice.superBot().views().packageExplorerView().tree().newC()
            .cls(PROJECT1, PKG1, CLS1);
        assertTrue(alice.superBot().views().packageExplorerView()
            .selectPkg(PROJECT1, PKG1).existsWithRegex(CLS1_SUFFIX));
    }

    @Test
    @Ignore
    public void test_newProjectWithClass() throws RemoteException {
        assertFalse(alice.superBot().views().packageExplorerView().tree()
            .existsWithRegex(PROJECT1));
        alice.superBot().views().packageExplorerView().tree().newC()
            .javaProjectWithClasses(PROJECT1, "pkg", "Cls");
        assertTrue(alice.superBot().views().packageExplorerView().tree()
            .existsWithRegex(PROJECT1));
        assertTrue(alice.superBot().views().packageExplorerView()
            .selectPkg(PROJECT1, "pkg").existsWithRegex("Cls" + SUFFIX_JAVA));

    }

    // @Test
    // @Ignore
    // public void test_newProjectWithClass_2() throws RemoteException {
    // assertFalse(alice.sarosBot().packageExplorerView().tree()
    // .exists(PROJECT1));
    // alice.sarosBot().packageExplorerView().tree().newC()
    // .newJavaProjectWithClasses(PROJECT1, "pkg", "Cls");
    // assertTrue(alice.sarosBot().packageExplorerView().tree()
    // .exists(PROJECT1));
    // assertTrue(alice.sarosBot().packageExplorerView()
    // .selectPkg(PROJECT1, "pkg").exists("Cls" + SUFFIX_JAVA));
    // }
    //
    // @Test
    // @Ignore
    // public void test_newProjectWithClass_3() throws RemoteException {
    // assertFalse(alice.sarosBot().packageExplorerView().tree()
    // .exists(PROJECT1));
    // alice.sarosBot().packageExplorerView().tree().newC()
    // .newJavaProjectWithClasses(PROJECT1, "pkg", "Cls");
    // assertTrue(alice.sarosBot().packageExplorerView().tree()
    // .exists(PROJECT1));
    // assertTrue(alice.sarosBot().packageExplorerView()
    // .selectPkg(PROJECT1, "pkg").exists("Cls" + SUFFIX_JAVA));
    // }
    //
    // @Test
    // @Ignore
    // public void test_newJavaClassInProject() throws RemoteException {
    // final String pkg = "pkg";
    // final String className = "Cls";
    //
    // log.trace("alice.isJavaProjectExist()");
    // assertFalse(alice.sarosBot().state().existsProjectNoGUI(PROJECT1));
    // log.trace("alice.newProjectWithClass()");
    //
    // alice.sarosBot().packageExplorerView().tree().newC()
    // .newJavaProject(PROJECT1);
    // alice.sarosBot().packageExplorerView().tree().newC()
    // .newClass(PROJECT1, pkg, className);
    // log.trace("alice.isJavaProjectExist()");
    // assertTrue(alice.sarosBot().state().existsProjectNoGUI(PROJECT1));
    // log.trace("alice.isJavaClassExist()");
    // assertTrue(alice.sarosBot().state()
    // .existsClassNoGUI(PROJECT1, pkg, className));
    //
    // log.trace("alice.isJavaClassExist()");
    // final String className2 = "Cls2";
    // assertFalse(alice.sarosBot().state()
    // .existsClassNoGUI(PROJECT1, pkg, className2));
    // log.trace("alice.newJavaClassInProject()");
    // alice.sarosBot().packageExplorerView().tree().newC()
    // .newClass(PROJECT1, pkg, className2);
    //
    // log.trace("alice.isJavaClassExist()");
    // assertTrue(alice.sarosBot().state()
    // .existsClassNoGUI(PROJECT1, pkg, className2));
    //
    // log.trace("deleteResource()");
    // alice.noBot().deleteProjectNoGUI(PROJECT1);
    // log.trace("alice.isJavaProjectExist()");
    // assertFalse(alice.sarosBot().state().existsProjectNoGUI(PROJECT1));
    // }
    //
    // @Test
    // @Ignore
    // public void testNewFolderInEmptyJavaProject() throws RemoteException {
    // alice.sarosBot().packageExplorerView().tree().newC()
    // .newJavaProject(PROJECT1);
    // // alice.buildSessionSequential(PROJECT,
    // // SarosConstant.CONTEXT_MENU_SHARE_PROJECT, bob);
    // alice.sarosBot().packageExplorerView().selectJavaProject(PROJECT1)
    // .newC().newFolder(FOLDER1);
    // // bob.bot.waitUntilFolderExist(PROJECT, FOLDER);
    // // assertTrue(bob.bot.isFolderExist(PROJECT, FOLDER));
    // }
    //
    // @Test
    // @Ignore
    // public void testNewFileNewFolderInEmptyProject() throws RemoteException {
    // alice.sarosBot().packageExplorerView().tree().newC().project(PROJECT1);
    // assertTrue(alice.sarosBot().state().existsProjectNoGUI(PROJECT1));
    // // alice.buildSessionSequential(PROJECT,
    // // SarosConstant.CONTEXT_MENU_SHARE_PROJECT, bob);
    // alice.sarosBot().packageExplorerView().selectJavaProject(PROJECT1)
    // .newC().newFolder(FOLDER1);
    // // bob.bot.waitUntilFolderExist(PROJECT, FOLDER);
    // // assertTrue(bob.bot.isFolderExist(PROJECT, FOLDER));
    // alice.sarosBot().packageExplorerView().selectFolder(PROJECT1, FOLDER1)
    // .newC().newFile(FILE1);
    // // bob.bot.waitUntilFileExist(PROJECT, FOLDER, FILE);
    // // assertTrue(bob.bot.isFileExist(PROJECT, FOLDER, FILE));
    // }
}
