package de.fu_berlin.inf.dpp.stf.test.stf.menubar;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.StfTestCase;
import de.fu_berlin.inf.dpp.stf.test.Constants;

public class MenuRefactorTest extends StfTestCase {

    @BeforeClass
    public static void runBeforeClass() throws RemoteException {
        initTesters(ALICE);
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
        ALICE.superBot().views().packageExplorerView().tree().newC()
            .javaProject(Constants.PROJECT1);
        ALICE.superBot().views().packageExplorerView().tree().newC()
            .cls(Constants.PROJECT1, Constants.PKG1, Constants.CLS1);
        ALICE.superBot().views().packageExplorerView().tree().newC()
            .pkg(Constants.PROJECT1, Constants.PKG2);
        ALICE.superBot().views().packageExplorerView()
            .selectClass(Constants.PROJECT1, Constants.PKG1, Constants.CLS1).refactor()
            .moveClassTo(Constants.PROJECT1, Constants.PKG2);
        assertFalse(ALICE.superBot().views().packageExplorerView()
            .selectPkg(Constants.PROJECT1, Constants.PKG1).exists(Constants.CLS1_SUFFIX));

        assertTrue(ALICE.superBot().views().packageExplorerView()
            .selectPkg(Constants.PROJECT1, Constants.PKG2).exists(Constants.CLS1_SUFFIX));
    }

    @Test
    @Ignore("Need to fix: this mehtod throws the WidgetNotFoundException by runing all tests, but running this mehtod alone, you will not get Exeption.")
    public void testRenameClass() throws RemoteException {

        ALICE.superBot().views().packageExplorerView().tree().newC()
            .javaProject(Constants.PROJECT1);
        ALICE.superBot().views().packageExplorerView().tree().newC()
            .cls(Constants.PROJECT1, Constants.PKG1, Constants.CLS1);
        ALICE.superBot().views().packageExplorerView()
            .selectClass(Constants.PROJECT1, Constants.PKG1, Constants.CLS1).refactor().rename(Constants.CLS2);

        assertFalse(ALICE.superBot().views().packageExplorerView()
            .selectPkg(Constants.PROJECT1, Constants.PKG1).exists(Constants.CLS1_SUFFIX));
        assertTrue(ALICE.superBot().views().packageExplorerView()
            .selectPkg(Constants.PROJECT1, Constants.PKG1).exists(Constants.CLS2_SUFFIX));
    }

    @Test
    public void testRenameFile() throws RemoteException {
        ALICE.superBot().views().packageExplorerView().tree().newC()
            .project(Constants.PROJECT1);
        ALICE.superBot().views().packageExplorerView().selectProject(Constants.PROJECT1)
            .newC().folder(Constants.FOLDER1);
        ALICE.superBot().views().packageExplorerView()
            .selectFolder(Constants.PROJECT1, Constants.FOLDER1).newC().file(Constants.FILE1);
        ALICE.superBot().views().packageExplorerView()
            .selectFile(Constants.PROJECT1, Constants.FOLDER1, Constants.FILE1).refactor().rename(Constants.FILE2);

        assertFalse(ALICE.superBot().views().packageExplorerView()
            .selectFolder(Constants.PROJECT1, Constants.FOLDER1).exists(Constants.FILE1));
        assertTrue(ALICE.superBot().views().packageExplorerView()
            .selectFolder(Constants.PROJECT1, Constants.FOLDER1).exists(Constants.FILE2));
    }

    @Test
    public void testRenameFolder() throws RemoteException {
        ALICE.superBot().views().packageExplorerView().tree().newC()
            .project(Constants.PROJECT1);
        ALICE.superBot().views().packageExplorerView().selectProject(Constants.PROJECT1)
            .newC().folder(Constants.FOLDER1);
        ALICE.superBot().views().packageExplorerView()
            .selectFolder(Constants.PROJECT1, Constants.FOLDER1).refactor().rename(Constants.FOLDER2);

        assertFalse(ALICE.superBot().views().packageExplorerView()
            .selectProject(Constants.PROJECT1).exists(Constants.FOLDER1));
        assertTrue(ALICE.superBot().views().packageExplorerView()
            .selectProject(Constants.PROJECT1).exists(Constants.FOLDER2));
    }

    @Test
    public void testRenamePackage() throws RemoteException {
        ALICE.superBot().views().packageExplorerView().tree().newC()
            .javaProject(Constants.PROJECT1);
        ALICE.superBot().views().packageExplorerView().tree().newC()
            .pkg(Constants.PROJECT1, Constants.PKG1);
        ALICE.superBot().views().packageExplorerView()
            .selectPkg(Constants.PROJECT1, Constants.PKG1).refactor().rename(Constants.PKG2);

        ALICE.remoteBot().sleep(500);
        assertFalse(ALICE.superBot().views().packageExplorerView()
            .selectProject(Constants.PROJECT1).exists(Constants.PKG1));
        assertTrue(ALICE.superBot().views().packageExplorerView()
            .selectSrc(Constants.PROJECT1).exists(Constants.PKG2));
    }

    @Test
    @Ignore("need to fix")
    public void testShareProjectWithSVN() throws RemoteException {
        ALICE.superBot().views().packageExplorerView().tree().newC()
            .javaProject(Constants.PROJECT1);
        assertFalse(ALICE.superBot().views().packageExplorerView()
            .isProjectManagedBySVN(Constants.PROJECT1));
        ALICE
            .superBot()
            .views()
            .packageExplorerView()
            .selectProject(Constants.PROJECT1)
            .team()
            .shareProjectUsingSpecifiedFolderName(Constants.SVN_REPOSITORY_URL,
                Constants.SVN_PROJECT_PATH);
        assertTrue(ALICE.superBot().views().packageExplorerView()
            .isProjectManagedBySVN(Constants.PROJECT1));
    }

    /**
     * Create a project, rename it, see if rename worked, delete all projects.
     */
    @Test
    public void testRenameProject() throws Exception {
        ALICE.superBot().views().packageExplorerView().tree().newC()
            .javaProject(Constants.PROJECT1);

        assertTrue(ALICE.superBot().views().packageExplorerView().tree()
            .exists(Constants.PROJECT1));
        assertFalse(ALICE.superBot().views().packageExplorerView().tree()
            .exists(Constants.PROJECT2));
        ALICE.superBot().views().packageExplorerView()
            .selectJavaProject(Constants.PROJECT1).refactor().rename(Constants.PROJECT2);

        assertFalse(ALICE.superBot().views().packageExplorerView().tree()
            .exists(Constants.PROJECT1));
        assertTrue(ALICE.superBot().views().packageExplorerView().tree()
            .exists(Constants.PROJECT2));
    }
}
