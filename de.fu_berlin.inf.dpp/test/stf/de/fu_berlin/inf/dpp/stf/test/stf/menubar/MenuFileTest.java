package de.fu_berlin.inf.dpp.stf.test.stf.menubar;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.SUFFIX_JAVA;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.StfTestCase;
import de.fu_berlin.inf.dpp.stf.test.Constants;

public class MenuFileTest extends StfTestCase {

    @BeforeClass
    public static void runBeforeClass() throws RemoteException {
        initTesters(ALICE);
        setUpWorkbench();
    }

    @Override
    @After
    public void tearDown() throws RemoteException {
        announceTestCaseEnd();
        deleteAllProjectsByActiveTesters();
    }

    /**********************************************
     * 
     * test all related actions with the sub menus of the context menu "New"
     * 
     **********************************************/
    @Test
    public void testNewProject() throws RemoteException {

        ALICE.superBot().views().packageExplorerView().tree().newC()
            .project(Constants.PROJECT1);
        assertTrue(ALICE.superBot().views().packageExplorerView().tree()
            .existsWithRegex(Constants.PROJECT1));
    }

    @Test
    public void testNewJavaProject() throws RemoteException {
        ALICE.superBot().views().packageExplorerView().tree().newC()
            .javaProject(Constants.PROJECT1);
        assertTrue(ALICE.superBot().views().packageExplorerView().tree()
            .existsWithRegex(Constants.PROJECT1));
    }

    @Test
    public void testNewFolder() throws RemoteException {
        ALICE.superBot().views().packageExplorerView().tree().newC()
            .javaProject(Constants.PROJECT1);
        ALICE.superBot().views().packageExplorerView()
            .selectJavaProject(Constants.PROJECT1).newC()
            .folder(Constants.FOLDER1);
        ALICE.superBot().views().packageExplorerView()
            .selectFolder(Constants.PROJECT1, Constants.FOLDER1).newC()
            .folder(Constants.FOLDER2);
        assertTrue(ALICE.superBot().views().packageExplorerView()
            .selectProject(Constants.PROJECT1)
            .existsWithRegex(Constants.FOLDER1));
        assertTrue(ALICE.superBot().views().packageExplorerView()
            .selectFolder(Constants.PROJECT1, Constants.FOLDER1)
            .existsWithRegex(Constants.FOLDER2));
        ALICE
            .superBot()
            .views()
            .packageExplorerView()
            .selectFolder(Constants.PROJECT1, Constants.FOLDER1,
                Constants.FOLDER2).delete();
        assertFalse(ALICE.superBot().views().packageExplorerView()
            .selectFolder(Constants.PROJECT1, Constants.FOLDER1)
            .existsWithRegex(Constants.FOLDER2));
        ALICE.superBot().views().packageExplorerView()
            .selectFolder(Constants.PROJECT1, Constants.FOLDER1).delete();
        assertFalse(ALICE.superBot().views().packageExplorerView()
            .selectProject(Constants.PROJECT1)
            .existsWithRegex(Constants.FOLDER1));
    }

    @Test
    public void testNewPackage1() throws RemoteException {
        ALICE.superBot().views().packageExplorerView().tree().newC()
            .javaProject(Constants.PROJECT1);
        ALICE.superBot().views().packageExplorerView().tree().newC()
            .pkg(Constants.PROJECT1, Constants.PKG1);
        ALICE.superBot().views().packageExplorerView().tree().newC()
            .pkg(Constants.PROJECT1, Constants.PKG1 + ".subpkg");
        assertTrue(ALICE.superBot().views().packageExplorerView()
            .selectSrc(Constants.PROJECT1).existsWithRegex(Constants.PKG1));
        assertTrue(ALICE.superBot().views().packageExplorerView()
            .selectSrc(Constants.PROJECT1)
            .existsWithRegex(Constants.PKG1 + ".subpkg"));
        ALICE.superBot().views().packageExplorerView()
            .selectPkg(Constants.PROJECT1, Constants.PKG1 + ".subpkg").delete();
        assertFalse(ALICE.superBot().views().packageExplorerView()
            .selectProject(Constants.PROJECT1)
            .existsWithRegex(Constants.PKG1 + ".subpkg"));
    }

    @Test
    public void testNewPackage2() throws RemoteException {
        ALICE.superBot().views().packageExplorerView().tree().newC()
            .project(Constants.PROJECT1);
        ALICE.superBot().views().packageExplorerView()
            .selectProject(Constants.PROJECT1).newC().folder(Constants.FOLDER1);
        ALICE.superBot().views().packageExplorerView()
            .selectFolder(Constants.PROJECT1, Constants.FOLDER1).newC()
            .file(Constants.FILE1);
        assertTrue(ALICE.superBot().views().packageExplorerView()
            .selectFolder(Constants.PROJECT1, Constants.FOLDER1)
            .existsWithRegex(Constants.FILE1));
    }

    @Test
    public void testNewClass() throws RemoteException {
        ALICE.superBot().views().packageExplorerView().tree().newC()
            .javaProject(Constants.PROJECT1);
        ALICE.superBot().views().packageExplorerView().tree().newC()
            .cls(Constants.PROJECT1, Constants.PKG1, Constants.CLS1);
        assertTrue(ALICE.superBot().views().packageExplorerView()
            .selectPkg(Constants.PROJECT1, Constants.PKG1)
            .existsWithRegex(Constants.CLS1_SUFFIX));
    }

    @Test
    @Ignore
    public void testNewProjectWithClass() throws RemoteException {
        assertFalse(ALICE.superBot().views().packageExplorerView().tree()
            .existsWithRegex(Constants.PROJECT1));
        ALICE.superBot().views().packageExplorerView().tree().newC()
            .javaProjectWithClasses(Constants.PROJECT1, "pkg", "Cls");
        assertTrue(ALICE.superBot().views().packageExplorerView().tree()
            .existsWithRegex(Constants.PROJECT1));
        assertTrue(ALICE.superBot().views().packageExplorerView()
            .selectPkg(Constants.PROJECT1, "pkg")
            .existsWithRegex("Cls" + SUFFIX_JAVA));

    }

    // @Test
    // @Ignore
    // public void test_newProjectWithClass_2() throws RemoteException {
    // assertFalse(ALICE.sarosBot().packageExplorerView().tree()
    // .exists(PROJECT1));
    // ALICE.sarosBot().packageExplorerView().tree().newC()
    // .newJavaProjectWithClasses(PROJECT1, "pkg", "Cls");
    // assertTrue(ALICE.sarosBot().packageExplorerView().tree()
    // .exists(PROJECT1));
    // assertTrue(ALICE.sarosBot().packageExplorerView()
    // .selectPkg(PROJECT1, "pkg").exists("Cls" +Constants.SUFFIX_JAVA));
    // }
    //
    // @Test
    // @Ignore
    // public void test_newProjectWithClass_3() throws RemoteException {
    // assertFalse(ALICE.sarosBot().packageExplorerView().tree()
    // .exists(PROJECT1));
    // ALICE.sarosBot().packageExplorerView().tree().newC()
    // .newJavaProjectWithClasses(PROJECT1, "pkg", "Cls");
    // assertTrue(ALICE.sarosBot().packageExplorerView().tree()
    // .exists(PROJECT1));
    // assertTrue(ALICE.sarosBot().packageExplorerView()
    // .selectPkg(PROJECT1, "pkg").exists("Cls" +Constants.SUFFIX_JAVA));
    // }
    //
    // @Test
    // @Ignore
    // public void test_newJavaClassInProject() throws RemoteException {
    // final String pkg = "pkg";
    // final String className = "Cls";
    //
    // log.trace("ALICE.isJavaProjectExist()");
    // assertFalse(ALICE.sarosBot().state().existsProjectNoGUI(PROJECT1));
    // log.trace("ALICE.newProjectWithClass()");
    //
    // ALICE.sarosBot().packageExplorerView().tree().newC()
    // .newJavaProject(PROJECT1);
    // ALICE.sarosBot().packageExplorerView().tree().newC()
    // .newClass(PROJECT1, pkg, className);
    // log.trace("ALICE.isJavaProjectExist()");
    // assertTrue(ALICE.sarosBot().state().existsProjectNoGUI(PROJECT1));
    // log.trace("ALICE.isJavaClassExist()");
    // assertTrue(ALICE.sarosBot().state()
    // .existsClassNoGUI(PROJECT1, pkg, className));
    //
    // log.trace("ALICE.isJavaClassExist()");
    // final String className2 = "Cls2";
    // assertFalse(ALICE.sarosBot().state()
    // .existsClassNoGUI(PROJECT1, pkg, className2));
    // log.trace("ALICE.newJavaClassInProject()");
    // ALICE.sarosBot().packageExplorerView().tree().newC()
    // .newClass(PROJECT1, pkg, className2);
    //
    // log.trace("ALICE.isJavaClassExist()");
    // assertTrue(ALICE.sarosBot().state()
    // .existsClassNoGUI(PROJECT1, pkg, className2));
    //
    // log.trace("deleteResource()");
    // ALICE.noBot().deleteProjectNoGUI(PROJECT1);
    // log.trace("ALICE.isJavaProjectExist()");
    // assertFalse(ALICE.sarosBot().state().existsProjectNoGUI(PROJECT1));
    // }
    //
    // @Test
    // @Ignore
    // public void testNewFolderInEmptyJavaProject() throws RemoteException {
    // ALICE.sarosBot().packageExplorerView().tree().newC()
    // .newJavaProject(PROJECT1);
    // // ALICE.buildSessionSequential(PROJECT,
    // // SarosConstant.CONTEXT_MENU_SHARE_PROJECT, BOB);
    // ALICE.sarosBot().packageExplorerView().selectJavaProject(PROJECT1)
    // .newC().newFolder(FOLDER1);
    // // BOB.bot.waitUntilFolderExist(PROJECT, FOLDER);
    // // assertTrue(BOB.bot.isFolderExist(PROJECT, FOLDER));
    // }
    //
    // @Test
    // @Ignore
    // public void testNewFileNewFolderInEmptyProject() throws RemoteException {
    // ALICE.sarosBot().packageExplorerView().tree().newC().project(PROJECT1);
    // assertTrue(ALICE.sarosBot().state().existsProjectNoGUI(PROJECT1));
    // // ALICE.buildSessionSequential(PROJECT,
    // // SarosConstant.CONTEXT_MENU_SHARE_PROJECT, BOB);
    // ALICE.sarosBot().packageExplorerView().selectJavaProject(PROJECT1)
    // .newC().newFolder(FOLDER1);
    // // BOB.bot.waitUntilFolderExist(PROJECT, FOLDER);
    // // assertTrue(BOB.bot.isFolderExist(PROJECT, FOLDER));
    // ALICE.sarosBot().packageExplorerView().selectFolder(PROJECT1, FOLDER1)
    // .newC().newFile(FILE1);
    // // BOB.bot.waitUntilFileExist(PROJECT, FOLDER, FILE);
    // // assertTrue(BOB.bot.isFileExist(PROJECT, FOLDER, FILE));
    // }
}
