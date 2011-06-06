package de.fu_berlin.inf.dpp.stf.test.saving;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.BOB;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.CARL;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.rmi.RemoteException;

import org.eclipse.core.runtime.CoreException;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.StfTestCase;
import de.fu_berlin.inf.dpp.stf.client.util.Util;
import de.fu_berlin.inf.dpp.stf.test.Constants;

public class CreatingNewFileTest extends StfTestCase {

    @BeforeClass
    public static void runBeforeClass() throws RemoteException {
        initTesters(ALICE, BOB, CARL);
        setUpWorkbench();
        setUpSaros();
        Util.setUpSessionWithAJavaProjectAndAClass(Constants.PROJECT1,
            Constants.PKG1, Constants.CLS1, CARL, BOB, ALICE);
    }

    @After
    public void runAfterEveryTest() throws RemoteException {
        deleteFoldersByActiveTesters(Constants.FOLDER1, Constants.FOLDER2);
    }

    /**
     * Steps:
     * <ol>
     * <li>CARL creates a new file.</li>
     * </ol>
     * 
     * Result:
     * <ol>
     * <li>ALICE and BOB should see the new file in the package explorer.</li>
     * </ol>
     * 
     * @throws CoreException
     * @throws IOException
     */

    @Test
    public void testCarlCreateANewFile() throws IOException, CoreException {
        CARL.superBot().views().packageExplorerView()
            .selectProject(Constants.PROJECT1).newC().folder(Constants.FOLDER1);
        CARL.superBot().views().packageExplorerView()
            .selectFolder(Constants.PROJECT1, Constants.FOLDER1).newC()
            .file(Constants.FILE1);
        ALICE
            .superBot()
            .views()
            .packageExplorerView()
            .waitUntilFileExists(Constants.PROJECT1, Constants.FOLDER1,
                Constants.FILE1);
        assertTrue(ALICE.superBot().views().packageExplorerView()
            .selectFolder(Constants.PROJECT1, Constants.FOLDER1)
            .existsWithRegex(Constants.FILE1));
        BOB.superBot()
            .views()
            .packageExplorerView()
            .waitUntilFileExists(Constants.PROJECT1, Constants.FOLDER1,
                Constants.FILE1);
        assertTrue(BOB.superBot().views().packageExplorerView()
            .selectFolder(Constants.PROJECT1, Constants.FOLDER1)
            .existsWithRegex(Constants.FILE1));
    }

    /**
     * Steps:
     * <ol>
     * <li>CARL restrict to read-only access to ALICE.</li>
     * <li>CARL creates a new file named "myFile.xml"</li>
     * <li>BOB and CARL activate "Follow-Mode"</li>
     * <li>ALICE creates new XML file myFile2.xml and edits it with the Eclipse
     * XML View</li>
     * </ol>
     * 
     * Result:
     * <ol>
     * <li></li>
     * <li>ALICE1_fu and BOB1_fu should not find the file "myFile.xml"</li>
     * <li></li>
     * <li>BOB and CARL should see the newly created XML file and the changes
     * made by ALICE</li>
     * </ol>
     * 
     * @throws CoreException
     * @throws IOException
     * @throws InterruptedException
     */

    @Test
    public void testCarlGrantWriteAccess() throws IOException, CoreException,
        InterruptedException {

        CARL.superBot().views().sarosView().selectParticipant(ALICE.getJID())
            .restrictToReadOnlyAccess();
        assertFalse(CARL.superBot().views().sarosView()
            .selectParticipant(ALICE.getJID()).hasWriteAccess());

        CARL.superBot().views().packageExplorerView()
            .selectProject(Constants.PROJECT1).newC().folder(Constants.FOLDER1);
        CARL.superBot().views().packageExplorerView()
            .selectFolder(Constants.PROJECT1, Constants.FOLDER1).newC()
            .file(Constants.FILE1);
        Util.waitsUntilTransferedDataIsArrived(ALICE);

        assertTrue(ALICE.superBot().views().packageExplorerView()
            .selectProject(Constants.PROJECT1)
            .existsWithRegex(Constants.FOLDER1));
        Util.waitsUntilTransferedDataIsArrived(BOB);
        assertTrue(BOB.superBot().views().packageExplorerView()
            .selectProject(Constants.PROJECT1)
            .existsWithRegex(Constants.FOLDER1));

        CARL.superBot().views().sarosView().selectParticipant(ALICE.getJID())
            .grantWriteAccess();
        Util.setFollowMode(ALICE, CARL, BOB);

        ALICE.superBot().views().packageExplorerView()
            .selectProject(Constants.PROJECT1).newC().folder(Constants.FOLDER2);
        ALICE.superBot().views().packageExplorerView()
            .selectFolder(Constants.PROJECT1, Constants.FOLDER2).newC()
            .file(Constants.FILE2);

        CARL.superBot()
            .views()
            .packageExplorerView()
            .waitUntilFileExists(Constants.PROJECT1, Constants.FOLDER2,
                Constants.FILE2);
        assertTrue(CARL.superBot().views().packageExplorerView()
            .selectFolder(Constants.PROJECT1, Constants.FOLDER2)
            .existsWithRegex(Constants.FILE2));
        BOB.superBot()
            .views()
            .packageExplorerView()
            .waitUntilFileExists(Constants.PROJECT1, Constants.FOLDER2,
                Constants.FILE2);
        assertTrue(BOB.superBot().views().packageExplorerView()
            .selectFolder(Constants.PROJECT1, Constants.FOLDER2)
            .existsWithRegex(Constants.FILE2));

        ALICE.remoteBot().editor(Constants.FILE2).setTexWithSave(Constants.CP1);

        String file2ContentOfAlice = ALICE.remoteBot().editor(Constants.FILE2)
            .getText();

        CARL.remoteBot().editor(Constants.FILE2)
            .waitUntilIsTextSame(file2ContentOfAlice);
        String file2ContentOfCarl = CARL.remoteBot().editor(Constants.FILE2)
            .getText();
        assertTrue(file2ContentOfAlice.equals(file2ContentOfCarl));

        BOB.remoteBot().editor(Constants.FILE2)
            .waitUntilIsTextSame(file2ContentOfAlice);
        String file2ContentOfBob = BOB.remoteBot().editor(Constants.FILE2)
            .getText();
        assertTrue(file2ContentOfAlice.equals(file2ContentOfBob));

    }
}
