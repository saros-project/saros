package de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.testSTF.menuBar;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.STFTest;

public class TestMenuRefactor extends STFTest {

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
     * all related actions with the sub menus of the context menu "Refactor"
     * 
     **********************************************/
    @Test
    public void testMoveClassTo() throws RemoteException {
        alice.fileM.newJavaProject(PROJECT1);
        alice.fileM.newClass(PROJECT1, PKG1, CLS1);
        alice.fileM.newPackage(PROJECT1, PKG2);
        alice.pEV.selectClass(PROJECT1, PKG1, CLS1);
        alice.refactorM.moveClassTo(PROJECT1, PKG2);
        assertFalse(alice.fileM.existsClassNoGUI(PROJECT1, PKG1, CLS1));
        assertTrue(alice.fileM.existsClassNoGUI(PROJECT1, PKG2, CLS1));
    }

    @Test
    public void testRenameClass() throws RemoteException {
        alice.fileM.newJavaProject(PROJECT1);
        alice.fileM.newClass(PROJECT1, PKG1, CLS1);
        alice.pEV.selectClass(PROJECT1, PKG1, CLS1);
        alice.refactorM.renameClass(CLS2);
        assertFalse(alice.fileM.existsClassNoGUI(PROJECT1, PKG1, CLS1));
        assertTrue(alice.fileM.existsClassNoGUI(PROJECT1, PKG1, CLS2));
    }

    @Test
    public void testRenameFile() throws RemoteException {
        alice.fileM.newProject(PROJECT1);
        alice.fileM.newFolder(PROJECT1, FOLDER1);
        alice.fileM.newFile(PROJECT1, FOLDER1, FILE1);
        alice.pEV.selectFile(PROJECT1, FOLDER1, FILE1);
        alice.refactorM.renameFile(FILE2);
        assertFalse(alice.fileM.existsFileNoGUI(PROJECT1, FOLDER1, FILE1));
        assertTrue(alice.fileM.existsFileNoGUI(PROJECT1, FOLDER1, FILE2));
    }

    @Test
    public void testRenameFolder() throws RemoteException {
        alice.fileM.newProject(PROJECT1);
        alice.fileM.newFolder(PROJECT1, FOLDER1);
        alice.pEV.selectFolder(PROJECT1, FOLDER1);
        alice.refactorM.renameFolder(FOLDER2);
        assertFalse(alice.fileM.existsFolderNoGUI(PROJECT1, FOLDER1));
        assertTrue(alice.fileM.existsFolderNoGUI(PROJECT1, FOLDER2));
    }

    @Test
    public void testRenamePackage() throws RemoteException {
        alice.fileM.newJavaProject(PROJECT1);
        alice.fileM.newPackage(PROJECT1, PKG1);
        alice.pEV.selectPkg(PROJECT1, PKG1);
        alice.refactorM.renamePkg(PKG2);
        assertFalse(alice.fileM.existsPkgNoGUI(PROJECT1, PKG1));
        assertTrue(alice.fileM.existsPkgNoGUI(PROJECT1, PKG2));
    }

    @Test
    public void testShareProjectWithSVN() throws RemoteException {
        alice.fileM.newJavaProject(PROJECT1);
        assertFalse(alice.team.isProjectManagedBySVN(PROJECT1));
        alice.team.shareProjectWithSVNUsingSpecifiedFolderName(
            VIEW_PACKAGE_EXPLORER, PROJECT1, SVN_REPOSITORY_URL,
            SVN_PROJECT_PATH);
        assertTrue(alice.team.isProjectManagedBySVN(PROJECT1));
    }

    /**
     * Create a project, rename it, see if rename worked, delete all projects.
     */
    @Test
    public void testRenameProject() throws Exception {
        alice.fileM.newJavaProject(PROJECT1);

        assertTrue(alice.fileM.existsProjectNoGUI(PROJECT1));
        assertFalse(alice.fileM.existsProjectNoGUI(PROJECT2));
        alice.pEV.selectProject(PROJECT1);
        alice.refactorM.renameJavaProject(PROJECT2);

        assertFalse(alice.fileM.existsProjectNoGUI(PROJECT1));
        assertTrue(alice.fileM.existsProjectNoGUI(PROJECT2));
    }
}
