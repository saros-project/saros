package de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.fileFolderOperations;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.STFTest;

public class TestFolderOperations extends STFTest {

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
    }

    @Before
    public void runBeforeEveryTest() throws RemoteException {
        if (!alice.superBot().views().packageExplorerView()
            .selectPkg(PROJECT1, PKG1).existsWithRegex(CLS1))
            alice.superBot().views().packageExplorerView().tree().newC()
                .cls(PROJECT1, PKG1, CLS1);
        if (!alice.superBot().views().packageExplorerView()
            .selectProject(PROJECT1).existsWithRegex(FOLDER1))
            alice.superBot().views().packageExplorerView()
                .selectProject(PROJECT1).newC().folder(FOLDER1);
    }

    /**
     * Steps:
     * <ol>
     * <li>alice rename the folder "FOLDER1" to "newFolderName"</li>
     * </ol>
     * 
     * Result:
     * <ol>
     * <li>the folder'name are renamed by bob too</li>
     * </ol>
     * 
     * @throws RemoteException
     */
    @Test
    public void testRenameFolder() throws RemoteException {
        final String newFolderName = FOLDER1 + "New";
        alice.superBot().views().packageExplorerView()
            .selectFolder(PROJECT1, FOLDER1).refactor().rename(newFolderName);

        bob.superBot().views().packageExplorerView()
            .waitUntilFolderExists(PROJECT1, newFolderName);
        assertTrue(bob.superBot().views().packageExplorerView()
            .selectProject(PROJECT1).existsWithRegex(newFolderName));
        assertFalse(bob.superBot().views().packageExplorerView()
            .selectProject(PROJECT1).exists(FOLDER1));
    }
}
