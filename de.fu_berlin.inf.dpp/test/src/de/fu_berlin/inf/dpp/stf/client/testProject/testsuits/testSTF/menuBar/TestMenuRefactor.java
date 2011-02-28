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
        alice.sarosBot().file().newJavaProject(PROJECT1);
        alice.sarosBot().file().newClass(PROJECT1, PKG1, CLS1);
        alice.sarosBot().file().newPackage(PROJECT1, PKG2);
        alice.sarosBot().packageExplorerView()
            .selectClass(PROJECT1, PKG1, CLS1).refactor()
            .moveClassTo(PROJECT1, PKG2);
        assertFalse(alice.sarosBot().state()
            .existsClassNoGUI(PROJECT1, PKG1, CLS1));
        assertTrue(alice.sarosBot().state()
            .existsClassNoGUI(PROJECT1, PKG2, CLS1));
    }

    @Test
    public void testRenameClass() throws RemoteException {
        alice.sarosBot().file().newJavaProject(PROJECT1);
        alice.sarosBot().file().newClass(PROJECT1, PKG1, CLS1);
        alice.sarosBot().packageExplorerView()
            .selectClass(PROJECT1, PKG1, CLS1).refactor().rename(CLS2);

        assertFalse(alice.sarosBot().state()
            .existsClassNoGUI(PROJECT1, PKG1, CLS1));
        assertTrue(alice.sarosBot().state()
            .existsClassNoGUI(PROJECT1, PKG1, CLS2));
    }

    @Test
    public void testRenameFile() throws RemoteException {
        alice.sarosBot().file().newProject(PROJECT1);
        alice.sarosBot().file().newFolder(PROJECT1, FOLDER1);
        alice.sarosBot().file().newFile(PROJECT1, FOLDER1, FILE1);
        alice.sarosBot().packageExplorerView()
            .selectFile(PROJECT1, FOLDER1, FILE1).refactor().rename(FILE2);

        assertFalse(alice.sarosBot().state()
            .existsFileNoGUI(PROJECT1, FOLDER1, FILE1));
        assertTrue(alice.sarosBot().state()
            .existsFileNoGUI(PROJECT1, FOLDER1, FILE2));
    }

    @Test
    public void testRenameFolder() throws RemoteException {
        alice.sarosBot().file().newProject(PROJECT1);
        alice.sarosBot().file().newFolder(PROJECT1, FOLDER1);
        alice.sarosBot().packageExplorerView().selectFolder(PROJECT1, FOLDER1)
            .refactor().rename(FOLDER2);

        assertFalse(alice.sarosBot().state()
            .existsFolderNoGUI(PROJECT1, FOLDER1));
        assertTrue(alice.sarosBot().state()
            .existsFolderNoGUI(PROJECT1, FOLDER2));
    }

    @Test
    public void testRenamePackage() throws RemoteException {
        alice.sarosBot().file().newJavaProject(PROJECT1);
        alice.sarosBot().file().newPackage(PROJECT1, PKG1);
        alice.sarosBot().packageExplorerView().selectPkg(PROJECT1, PKG1)
            .refactor().rename(PKG2);

        alice.bot().sleep(500);
        assertFalse(alice.sarosBot().state().existsPkgNoGUI(PROJECT1, PKG1));
        assertTrue(alice.sarosBot().state().existsPkgNoGUI(PROJECT1, PKG2));
    }

    @Test
    public void testShareProjectWithSVN() throws RemoteException {
        alice.sarosBot().file().newJavaProject(PROJECT1);
        assertFalse(alice.sarosBot().state().isProjectManagedBySVN(PROJECT1));
        alice
            .sarosBot()
            .packageExplorerView()
            .selectProject(PROJECT1)
            .team()
            .shareProjectUsingSpecifiedFolderName(SVN_REPOSITORY_URL,
                SVN_PROJECT_PATH);
        assertTrue(alice.sarosBot().state().isProjectManagedBySVN(PROJECT1));
    }

    /**
     * Create a project, rename it, see if rename worked, delete all projects.
     */
    @Test
    public void testRenameProject() throws Exception {
        alice.sarosBot().file().newJavaProject(PROJECT1);

        assertTrue(alice.sarosBot().state().existsProjectNoGUI(PROJECT1));
        assertFalse(alice.sarosBot().state().existsProjectNoGUI(PROJECT2));
        alice.sarosBot().packageExplorerView().selectJavaProject(PROJECT1)
            .refactor().rename(PROJECT2);

        assertFalse(alice.sarosBot().state().existsProjectNoGUI(PROJECT1));
        assertTrue(alice.sarosBot().state().existsProjectNoGUI(PROJECT2));
    }
}
