package de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.testSTF.menuBar;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Ignore;
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
        alice.superBot().views().packageExplorerView().tree().newC()
            .javaProject(PROJECT1);
        alice.superBot().views().packageExplorerView().tree().newC()
            .cls(PROJECT1, PKG1, CLS1);
        alice.superBot().views().packageExplorerView().tree().newC()
            .pkg(PROJECT1, PKG2);
        alice.superBot().views().packageExplorerView()
            .selectClass(PROJECT1, PKG1, CLS1).refactor()
            .moveClassTo(PROJECT1, PKG2);
        assertFalse(alice.superBot().views().packageExplorerView()
            .selectPkg(PROJECT1, PKG1).exists(CLS1_SUFFIX));

        assertTrue(alice.superBot().views().packageExplorerView()
            .selectPkg(PROJECT1, PKG2).exists(CLS1_SUFFIX));
    }

    @Test
    @Ignore("Need to fix: this mehtod throws the WidgetNotFoundException by runing all tests, but running this mehtod alone, you will not get Exeption.")
    public void testRenameClass() throws RemoteException {

        alice.superBot().views().packageExplorerView().tree().newC()
            .javaProject(PROJECT1);
        alice.superBot().views().packageExplorerView().tree().newC()
            .cls(PROJECT1, PKG1, CLS1);
        alice.superBot().views().packageExplorerView()
            .selectClass(PROJECT1, PKG1, CLS1).refactor().rename(CLS2);

        assertFalse(alice.superBot().views().packageExplorerView()
            .selectPkg(PROJECT1, PKG1).exists(CLS1_SUFFIX));
        assertTrue(alice.superBot().views().packageExplorerView()
            .selectPkg(PROJECT1, PKG1).exists(CLS2_SUFFIX));
    }

    @Test
    public void testRenameFile() throws RemoteException {
        alice.superBot().views().packageExplorerView().tree().newC()
            .project(PROJECT1);
        alice.superBot().views().packageExplorerView().selectProject(PROJECT1)
            .newC().folder(FOLDER1);
        alice.superBot().views().packageExplorerView()
            .selectFolder(PROJECT1, FOLDER1).newC().file(FILE1);
        alice.superBot().views().packageExplorerView()
            .selectFile(PROJECT1, FOLDER1, FILE1).refactor().rename(FILE2);

        assertFalse(alice.superBot().views().packageExplorerView()
            .selectFolder(PROJECT1, FOLDER1).exists(FILE1));
        assertTrue(alice.superBot().views().packageExplorerView()
            .selectFolder(PROJECT1, FOLDER1).exists(FILE2));
    }

    @Test
    public void testRenameFolder() throws RemoteException {
        alice.superBot().views().packageExplorerView().tree().newC()
            .project(PROJECT1);
        alice.superBot().views().packageExplorerView().selectProject(PROJECT1)
            .newC().folder(FOLDER1);
        alice.superBot().views().packageExplorerView()
            .selectFolder(PROJECT1, FOLDER1).refactor().rename(FOLDER2);

        assertFalse(alice.superBot().views().packageExplorerView()
            .selectProject(PROJECT1).exists(FOLDER1));
        assertTrue(alice.superBot().views().packageExplorerView()
            .selectProject(PROJECT1).exists(FOLDER2));
    }

    @Test
    public void testRenamePackage() throws RemoteException {
        alice.superBot().views().packageExplorerView().tree().newC()
            .javaProject(PROJECT1);
        alice.superBot().views().packageExplorerView().tree().newC()
            .pkg(PROJECT1, PKG1);
        alice.superBot().views().packageExplorerView()
            .selectPkg(PROJECT1, PKG1).refactor().rename(PKG2);

        alice.remoteBot().sleep(500);
        assertFalse(alice.superBot().views().packageExplorerView()
            .selectProject(PROJECT1).exists(PKG1));
        assertTrue(alice.superBot().views().packageExplorerView()
            .selectSrc(PROJECT1).exists(PKG2));
    }

    @Test
    @Ignore("need to fix")
    public void testShareProjectWithSVN() throws RemoteException {
        alice.superBot().views().packageExplorerView().tree().newC()
            .javaProject(PROJECT1);
        assertFalse(alice.superBot().views().packageExplorerView()
            .isProjectManagedBySVN(PROJECT1));
        alice
            .superBot()
            .views()
            .packageExplorerView()
            .selectProject(PROJECT1)
            .team()
            .shareProjectUsingSpecifiedFolderName(SVN_REPOSITORY_URL,
                SVN_PROJECT_PATH);
        assertTrue(alice.superBot().views().packageExplorerView()
            .isProjectManagedBySVN(PROJECT1));
    }

    /**
     * Create a project, rename it, see if rename worked, delete all projects.
     */
    @Test
    public void testRenameProject() throws Exception {
        alice.superBot().views().packageExplorerView().tree().newC()
            .javaProject(PROJECT1);

        assertTrue(alice.superBot().views().packageExplorerView().tree()
            .exists(PROJECT1));
        assertFalse(alice.superBot().views().packageExplorerView().tree()
            .exists(PROJECT2));
        alice.superBot().views().packageExplorerView()
            .selectJavaProject(PROJECT1).refactor().rename(PROJECT2);

        assertFalse(alice.superBot().views().packageExplorerView().tree()
            .exists(PROJECT1));
        assertTrue(alice.superBot().views().packageExplorerView().tree()
            .exists(PROJECT2));
    }
}
