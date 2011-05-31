package de.fu_berlin.inf.dpp.stf.test.filefolderoperations;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.BOB;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.CARL;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.StfTestCase;
import de.fu_berlin.inf.dpp.stf.client.util.Constants;
import de.fu_berlin.inf.dpp.stf.client.util.Util;

public class FolderOperationsTest extends StfTestCase {

    /**
     * Preconditions:
     * <ol>
     * <li>ALICE (Host, Write Access), aclice share a java project with BOB and
     * CARL.</li>
     * <li>BOB (Read-Only Access)</li>
     * <li>CARL (Read-Only Access)</li>
     * </ol>
     * 
     * @throws RemoteException
     * @throws InterruptedException
     */
    @BeforeClass
    public static void runBeforeClass() throws RemoteException,
        InterruptedException {
        initTesters(ALICE, BOB, CARL);
        setUpWorkbench();
        setUpSaros();
        Util.setUpSessionWithAJavaProjectAndAClass(ALICE, BOB, CARL);
    }

    @Before
    public void runBeforeEveryTest() throws RemoteException {
        if (!ALICE.superBot().views().packageExplorerView()
            .selectPkg(Constants.PROJECT1, Constants.PKG1).existsWithRegex(Constants.CLS1))
            ALICE.superBot().views().packageExplorerView().tree().newC()
                .cls(Constants.PROJECT1, Constants.PKG1, Constants.CLS1);
        if (!ALICE.superBot().views().packageExplorerView()
            .selectProject(Constants.PROJECT1).existsWithRegex(Constants.FOLDER1))
            ALICE.superBot().views().packageExplorerView()
                .selectProject(Constants.PROJECT1).newC().folder(Constants.FOLDER1);
    }

    /**
     * Steps:
     * <ol>
     * <li>ALICE rename the folder "FOLDER1" to "newFolderName"</li>
     * </ol>
     * 
     * Result:
     * <ol>
     * <li>the folder'name are renamed by BOB too</li>
     * </ol>
     * 
     * @throws RemoteException
     */
    @Test
    public void testRenameFolder() throws RemoteException {
        final String newFolderName = Constants.FOLDER1 + "New";
        ALICE.superBot().views().packageExplorerView()
            .selectFolder(Constants.PROJECT1, Constants.FOLDER1).refactor().rename(newFolderName);

        BOB.superBot().views().packageExplorerView()
            .waitUntilFolderExists(Constants.PROJECT1, newFolderName);
        assertTrue(BOB.superBot().views().packageExplorerView()
            .selectProject(Constants.PROJECT1).existsWithRegex(newFolderName));
        assertFalse(BOB.superBot().views().packageExplorerView()
            .selectProject(Constants.PROJECT1).exists(Constants.FOLDER1));
    }
}
