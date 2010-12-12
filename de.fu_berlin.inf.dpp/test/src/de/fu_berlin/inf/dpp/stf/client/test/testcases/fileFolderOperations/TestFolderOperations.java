package de.fu_berlin.inf.dpp.stf.client.test.testcases.fileFolderOperations;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.test.helpers.STFTest;

public class TestFolderOperations extends STFTest {

    /**
     * Preconditions:
     * <ol>
     * <li>alice (Host, Driver), aclice share a java project with bob and carl.</li>
     * <li>bob (Observer)</li>
     * <li>carl (Observer)</li>
     * </ol>
     * 
     * @throws RemoteException
     * @throws InterruptedException
     */
    @BeforeClass
    public static void runBeforeClass() throws RemoteException,
        InterruptedException {
        initTesters(TypeOfTester.ALICE, TypeOfTester.BOB, TypeOfTester.CARL);
        setUpWorkbenchs();
        setUpSaros();
        setUpSession(alice, bob, carl);
    }

    @AfterClass
    public static void runAfterClass() throws RemoteException,
        InterruptedException {
        alice.leaveSessionHostFirstDone(bob, carl);
    }

    @Before
    public void runBeforeEveryTest() throws RemoteException {
        if (!alice.pEV.isClassExist(PROJECT1, PKG1, CLS1))
            alice.pEV.newClass(PROJECT1, PKG1, CLS1);
        if (!alice.pEV.isFolderExist(PROJECT1, FOLDER1))
            alice.pEV.newFolder(FOLDER1, PROJECT1);
    }

    @After
    public void runAfterEveryTest() throws RemoteException {
        alice.addBuddyGUIDone(bob);
        bob.addBuddyGUIDone(alice);
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

        alice.pEV.renameFolder(newFolderName, PROJECT1, FOLDER1);
        bob.pEV.waitUntilFolderExist(PROJECT1, newFolderName);
        assertTrue(bob.pEV.isFolderExist(PROJECT1, newFolderName));
        assertFalse(bob.pEV.isFolderExist(PROJECT1, FOLDER1));
    }
}
