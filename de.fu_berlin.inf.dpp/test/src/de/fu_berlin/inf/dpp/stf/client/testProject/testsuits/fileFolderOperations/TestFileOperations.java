package de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.fileFolderOperations;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.rmi.RemoteException;

import org.eclipse.core.runtime.CoreException;
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
        setUpSessionWithAJavaProjectAndAClass(alice, bob, carl);
        setFollowMode(alice, carl);
    }

    @Before
    public void runBeforeEveryTest() throws RemoteException {

        resetSharedProject(alice);

    }

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

        assertTrue(bob.sarosBot().packageExplorerView()
            .existsClassNoGUI(PROJECT1, PKG1, CLS1));
        alice.sarosBot().packageExplorerView()
            .selectClass(PROJECT1, PKG1, CLS1);
        alice.sarosBot().refactor().renameClass(CLS2);

        bob.sarosBot().packageExplorerView()
            .waitUntilClassExists(PROJECT1, PKG1, CLS2);
        assertFalse(bob.sarosBot().packageExplorerView()
            .existsClassNoGUI(PROJECT1, PKG1, CLS1));
        assertTrue(bob.sarosBot().packageExplorerView()
            .existsClassNoGUI(PROJECT1, PKG1, CLS2));

        carl.sarosBot().packageExplorerView()
            .waitUntilClassExists(PROJECT1, PKG1, CLS2);
        assertFalse(carl.sarosBot().packageExplorerView()
            .existsClassNoGUI(PROJECT1, PKG1, CLS1));
        assertTrue(carl.sarosBot().packageExplorerView()
            .existsClassNoGUI(PROJECT1, PKG1, CLS2));
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
        alice.sarosBot().edit().deleteClassNoGUI(PROJECT1, PKG1, CLS1);
        bob.sarosBot().packageExplorerView()
            .waitUntilClassNotExists(PROJECT1, PKG1, CLS1);
        assertFalse(bob.sarosBot().packageExplorerView()
            .existsClassNoGUI(PROJECT1, PKG1, CLS1));
        carl.sarosBot().packageExplorerView()
            .waitUntilClassNotExists(PROJECT1, PKG1, CLS1);
        assertFalse(carl.sarosBot().packageExplorerView()
            .existsClassNoGUI(PROJECT1, PKG1, CLS1));
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
        alice.sarosBot().file().newPackage(PROJECT1, PKG2);
        bob.sarosBot().packageExplorerView().waitUntilPkgExists(PROJECT1, PKG2);
        carl.sarosBot().packageExplorerView()
            .waitUntilPkgExists(PROJECT1, PKG2);
        assertTrue(bob.sarosBot().packageExplorerView()
            .existsPkgNoGUI(PROJECT1, PKG2));
        assertTrue(carl.sarosBot().packageExplorerView()
            .existsPkgNoGUI(PROJECT1, PKG2));

        alice.sarosBot().file().newClass(PROJECT1, PKG2, CLS1);
        bob.sarosBot().packageExplorerView()
            .waitUntilClassExists(PROJECT1, PKG2, CLS1);
        carl.sarosBot().packageExplorerView()
            .waitUntilClassExists(PROJECT1, PKG2, CLS1);
        assertTrue(bob.sarosBot().packageExplorerView()
            .existsClassNoGUI(PROJECT1, PKG2, CLS1));
        assertTrue(carl.sarosBot().packageExplorerView()
            .existsClassNoGUI(PROJECT1, PKG2, CLS1));

        alice.bot().editor(CLS1_SUFFIX).setTexWithSave(CP1);
        String clsContentOfAlice = alice.noBot().getFileContent(
            getClassPath(PROJECT1, PKG2, CLS1));
        carl.noBot().waitUntilFileContentSame(clsContentOfAlice,
            getClassPath(PROJECT1, PKG2, CLS1));
        bob.noBot().waitUntilFileContentSame(clsContentOfAlice,
            getClassPath(PROJECT1, PKG2, CLS1));
        String clsContentOfBob = bob.noBot().getFileContent(
            getClassPath(PROJECT1, PKG2, CLS1));
        String clsContentOfCarl = carl.noBot().getFileContent(
            getClassPath(PROJECT1, PKG2, CLS1));
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
        alice.sarosBot().file().newPackage(PROJECT1, PKG2);
        alice.sarosBot().file().newClass(PROJECT1, PKG2, CLS2);
        alice.sarosBot().packageExplorerView()
            .selectClass(PROJECT1, PKG2, CLS2);
        alice.sarosBot().refactor().moveClassTo(PROJECT1, PKG1);
        bob.sarosBot().packageExplorerView()
            .waitUntilClassExists(PROJECT1, PKG1, CLS2);
        carl.sarosBot().packageExplorerView()
            .waitUntilClassExists(PROJECT1, PKG1, CLS2);
        assertTrue(bob.sarosBot().packageExplorerView()
            .existsClassNoGUI(PROJECT1, PKG1, CLS2));
        assertFalse(bob.sarosBot().packageExplorerView()
            .existsClassNoGUI(PROJECT1, PKG2, CLS2));
        assertTrue(carl.sarosBot().packageExplorerView()
            .existsClassNoGUI(PROJECT1, PKG1, CLS2));
        assertFalse(carl.sarosBot().packageExplorerView()
            .existsClassNoGUI(PROJECT1, PKG2, CLS2));
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
        alice.sarosBot().packageExplorerView().selectPkg(PROJECT1, PKG1);
        alice.sarosBot().refactor().renamePkg(PKG2);
        bob.sarosBot().packageExplorerView().waitUntilPkgExists(PROJECT1, PKG2);
        bob.sarosBot().packageExplorerView()
            .waitUntilPkgNotExists(PROJECT1, PKG1);
        assertFalse(bob.sarosBot().packageExplorerView()
            .existsPkgNoGUI(PROJECT1, PKG1));
        assertTrue(bob.sarosBot().packageExplorerView()
            .existsPkgNoGUI(PROJECT1, PKG2));

        carl.sarosBot().packageExplorerView()
            .waitUntilPkgExists(PROJECT1, PKG2);
        carl.sarosBot().packageExplorerView()
            .waitUntilPkgNotExists(PROJECT1, PKG1);
        assertFalse(carl.sarosBot().packageExplorerView()
            .existsPkgNoGUI(PROJECT1, PKG1));
        assertTrue(carl.sarosBot().packageExplorerView()
            .existsPkgNoGUI(PROJECT1, PKG2));
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
        alice.sarosBot().edit().deletePkgNoGUI(PROJECT1, PKG1);
        bob.sarosBot().packageExplorerView()
            .waitUntilPkgNotExists(PROJECT1, PKG1);
        carl.sarosBot().packageExplorerView()
            .waitUntilPkgNotExists(PROJECT1, PKG1);
        assertFalse(bob.sarosBot().packageExplorerView()
            .existsPkgNoGUI(PROJECT1, PKG1));
        assertFalse(carl.sarosBot().packageExplorerView()
            .existsPkgNoGUI(PROJECT1, PKG1));
    }
}
