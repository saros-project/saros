package de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.fileFolderOperations;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.rmi.RemoteException;

import org.eclipse.core.runtime.CoreException;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.STFTest;

public class TestFileOperations extends STFTest {

    /**
     * Preconditions:
     * <ol>
     * <li>alice (Host, Write Access), aclice share a java project with bob and
     * carl.</li>
     * <li>bob (Read-Only Access)</li>
     * <li>carl (Read-Only Access)</li>
     * </ol>
     * 
     * @throws RemoteException
     * @throws InterruptedException
     */
    @BeforeClass
    public static void runBeforeClass() throws RemoteException,
        InterruptedException {
        initTesters(TypeOfTester.ALICE, TypeOfTester.BOB, TypeOfTester.CARL);
        setUpWorkbench();
        setUpSaros();

    }

    @Before
    public void runBeforeEveryMethod() throws RemoteException,
        InterruptedException {
        /*
         * NOTE: The session sharing by Version 11.3.25.DEVEL is not stable,
         * sometime the invitation process can not be completed, so it's
         * possible that all tests are failed.
         */
        setUpSessionWithAJavaProjectAndAClass(alice, bob, carl);
        setFollowMode(alice, carl);
    }

    @After
    public void runAfterEveryMethod() throws RemoteException,
        InterruptedException {
        leaveSessionHostFirst(alice);
        deleteAllProjectsByActiveTesters();
    }

    // @After
    // public void runBeforeEveryTest() throws RemoteException {
    // alice.bot().saveAllEditors();
    // bob.bot().saveAllEditors();
    // carl.bot().saveAllEditors();
    // resetSharedProject(alice);
    // }

    /**
     * Steps:
     * <ol>
     * <li>alice rename the class "CLS1" to "CLS2"</li>
     * </ol>
     * 
     * Result:
     * <ol>
     * <li>the class'name are renamed by bob and carl too</li>
     * </ol>
     * 
     * @throws RemoteException
     */
    @Test
    public void testRenameFile() throws RemoteException {

        assertTrue(bob.superBot().views().packageExplorerView()
            .selectPkg(PROJECT1, PKG1).exists(CLS1_SUFFIX));
        alice.superBot().views().packageExplorerView()
            .selectClass(PROJECT1, PKG1, CLS1).refactor().rename(CLS2);

        bob.superBot().views().packageExplorerView()
            .waitUntilClassExists(PROJECT1, PKG1, CLS2);
        assertFalse(bob.superBot().views().packageExplorerView()
            .selectPkg(PROJECT1, PKG1).exists(CLS1_SUFFIX));
        assertTrue(bob.superBot().views().packageExplorerView()
            .selectPkg(PROJECT1, PKG1).exists(CLS2_SUFFIX));

        carl.superBot().views().packageExplorerView()
            .waitUntilClassExists(PROJECT1, PKG1, CLS2);
        assertFalse(carl.superBot().views().packageExplorerView()
            .selectPkg(PROJECT1, PKG1).exists(CLS1_SUFFIX));
        assertTrue(carl.superBot().views().packageExplorerView()
            .selectPkg(PROJECT1, PKG1).exists(CLS2_SUFFIX));
    }

    /**
     * Steps:
     * <ol>
     * <li>alice delete the class "CLS1"</li>
     * </ol>
     * 
     * Result:
     * <ol>
     * <li>the class are deleted by bob and carl too</li>
     * </ol>
     * 
     * @throws RemoteException
     */
    @Test
    public void testDeleteFile() throws RemoteException {
        alice.superBot().views().packageExplorerView()
            .selectClass(PROJECT1, PKG1, CLS1).delete();
        bob.superBot().views().packageExplorerView()
            .waitUntilClassNotExists(PROJECT1, PKG1, CLS1);
        assertFalse(bob.superBot().views().packageExplorerView()
            .selectPkg(PROJECT1, PKG1).exists(CLS1_SUFFIX));
        carl.superBot().views().packageExplorerView()
            .waitUntilClassNotExists(PROJECT1, PKG1, CLS1);
        assertFalse(carl.superBot().views().packageExplorerView()
            .selectPkg(PROJECT1, PKG1).exists(CLS1_SUFFIX));
    }

    /**
     * Steps:
     * <ol>
     * <li>alice create a new package "PKG2"</li>
     * <li>alice create a new class "CLS1" under package "PKG2"</li>
     * <li>alice set text in the class "CLS1"</li>
     * </ol>
     * 
     * Result:
     * <ol>
     * <li>the package "PKG2" are created by bob and carl too</li>
     * <li>the new class "CLS1" are created by bob and carl too</li>
     * <li>carl and bob should see the change by alice</li>
     * </ol>
     * 
     * @throws RemoteException
     */
    @Test
    public void testNewPkgAndClass() throws CoreException, IOException {
        alice.superBot().views().packageExplorerView().tree().newC()
            .pkg(PROJECT1, PKG2);
        bob.superBot().views().packageExplorerView()
            .waitUntilPkgExists(PROJECT1, PKG2);
        carl.superBot().views().packageExplorerView()
            .waitUntilPkgExists(PROJECT1, PKG2);
        assertTrue(bob.superBot().views().packageExplorerView()
            .selectSrc(PROJECT1).exists(PKG2));
        assertTrue(carl.superBot().views().packageExplorerView()
            .selectSrc(PROJECT1).exists(PKG2));

        alice.superBot().views().packageExplorerView().tree().newC()
            .cls(PROJECT1, PKG2, CLS1);
        bob.superBot().views().packageExplorerView()
            .waitUntilClassExists(PROJECT1, PKG2, CLS1);
        carl.superBot().views().packageExplorerView()
            .waitUntilClassExists(PROJECT1, PKG2, CLS1);
        assertTrue(bob.superBot().views().packageExplorerView()
            .selectPkg(PROJECT1, PKG2).exists(CLS1_SUFFIX));
        assertTrue(carl.superBot().views().packageExplorerView()
            .selectPkg(PROJECT1, PKG2).exists(CLS1_SUFFIX));

        alice.remoteBot().editor(CLS1_SUFFIX).setTexWithSave(CP1);
        String clsContentOfAlice = alice.superBot().views()
            .packageExplorerView()
            .getFileContent(getClassPath(PROJECT1, PKG2, CLS1));
        carl.superBot()
            .views()
            .packageExplorerView()
            .waitUntilFileContentSame(clsContentOfAlice,
                getClassPath(PROJECT1, PKG2, CLS1));
        bob.superBot()
            .views()
            .packageExplorerView()
            .waitUntilFileContentSame(clsContentOfAlice,
                getClassPath(PROJECT1, PKG2, CLS1));
        String clsContentOfBob = bob.superBot().views().packageExplorerView()
            .getFileContent(getClassPath(PROJECT1, PKG2, CLS1));
        String clsContentOfCarl = carl.superBot().views().packageExplorerView()
            .getFileContent(getClassPath(PROJECT1, PKG2, CLS1));
        assertTrue(clsContentOfBob.equals(clsContentOfAlice));
        assertTrue(clsContentOfCarl.equals(clsContentOfAlice));
    }

    /**
     * Steps:
     * <ol>
     * <li>alice create a new package "PKG2" and under it create a new class
     * "CLS2"</li>
     * <li>alice move the class "CLS2" to the package "PKG1"</li>
     * </ol>
     * 
     * Result:
     * <ol>
     * <li></li>
     * <li>the class "CLS2" should be moved into the package "PKG1" by carl and
     * bob</li>
     * </ol>
     * 
     * @throws RemoteException
     */
    @Test
    public void testMoveClass() throws RemoteException {
        alice.superBot().views().packageExplorerView().tree().newC()
            .pkg(PROJECT1, PKG2);
        alice.superBot().views().packageExplorerView().tree().newC()
            .cls(PROJECT1, PKG2, CLS2);
        alice.superBot().views().packageExplorerView()
            .selectClass(PROJECT1, PKG2, CLS2).refactor()
            .moveClassTo(PROJECT1, PKG1);

        bob.superBot().views().packageExplorerView()
            .waitUntilClassExists(PROJECT1, PKG1, CLS2);
        carl.superBot().views().packageExplorerView()
            .waitUntilClassExists(PROJECT1, PKG1, CLS2);
        assertTrue(bob.superBot().views().packageExplorerView()
            .selectPkg(PROJECT1, PKG1).exists(CLS2_SUFFIX));
        assertFalse(bob.superBot().views().packageExplorerView()
            .selectPkg(PROJECT1, PKG2).exists(CLS2_SUFFIX));
        assertTrue(carl.superBot().views().packageExplorerView()
            .selectPkg(PROJECT1, PKG1).exists(CLS2_SUFFIX));
        assertFalse(carl.superBot().views().packageExplorerView()
            .selectPkg(PROJECT1, PKG2).exists(CLS2_SUFFIX));
    }

    /**
     * Steps:
     * <ol>
     * <li>alice rename the package "PKG1" to "PKG2"</li>
     * </ol>
     * 
     * Result:
     * <ol>
     * <li>the package should be renamed by carl and bob</li>
     * </ol>
     * 
     * @throws RemoteException
     */
    @Test
    public void testRenamePkg() throws RemoteException {
        alice.superBot().views().packageExplorerView()
            .selectPkg(PROJECT1, PKG1).refactor().rename(PKG2);

        bob.superBot().views().packageExplorerView()
            .waitUntilPkgExists(PROJECT1, PKG2);
        bob.superBot().views().packageExplorerView()
            .waitUntilPkgNotExists(PROJECT1, PKG1);
        assertFalse(bob.superBot().views().packageExplorerView()
            .selectJavaProject(PROJECT1).exists(PKG1));
        assertTrue(bob.superBot().views().packageExplorerView()
            .selectSrc(PROJECT1).exists(PKG2));

        carl.superBot().views().packageExplorerView()
            .waitUntilPkgExists(PROJECT1, PKG2);
        carl.superBot().views().packageExplorerView()
            .waitUntilPkgNotExists(PROJECT1, PKG1);
        assertFalse(carl.superBot().views().packageExplorerView()
            .selectProject(PROJECT1).exists(PKG1));
        assertTrue(carl.superBot().views().packageExplorerView()
            .selectSrc(PROJECT1).exists(PKG2));
    }

    /**
     * Steps:
     * <ol>
     * <li>alice delete the package "PKG1"</li>
     * </ol>
     * 
     * Result:
     * <ol>
     * <li>the package should be deleted by carl and bob</li>
     * </ol>
     * 
     * @throws RemoteException
     */
    @Test
    public void testDeletePkg() throws RemoteException {
        alice.superBot().views().packageExplorerView()
            .selectPkg(PROJECT1, PKG1).delete();
        bob.superBot().views().packageExplorerView()
            .waitUntilPkgNotExists(PROJECT1, PKG1);
        carl.superBot().views().packageExplorerView()
            .waitUntilPkgNotExists(PROJECT1, PKG1);
        assertFalse(bob.superBot().views().packageExplorerView()
            .selectJavaProject(PROJECT1).exists(PKG1));
        assertFalse(carl.superBot().views().packageExplorerView()
            .selectJavaProject(PROJECT1).exists(PKG1));
    }
}
