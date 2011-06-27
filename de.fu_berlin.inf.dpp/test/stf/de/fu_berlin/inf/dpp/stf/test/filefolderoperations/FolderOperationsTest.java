package de.fu_berlin.inf.dpp.stf.test.filefolderoperations;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.BOB;
import static org.junit.Assert.fail;

import java.rmi.RemoteException;

import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.annotation.TestLink;
import de.fu_berlin.inf.dpp.stf.client.StfTestCase;
import de.fu_berlin.inf.dpp.stf.client.util.Util;
import de.fu_berlin.inf.dpp.stf.shared.Constants.TypeOfCreateProject;
import de.fu_berlin.inf.dpp.stf.test.Constants;

@TestLink(id = "Saros-43_folder_operations")
public class FolderOperationsTest extends StfTestCase {

    /**
     * Preconditions:
     * <ol>
     * <li>ALICE (Host, Write Access), Alice share a java project with BOB and
     * CARL.</li>
     * <li>BOB (Read-Only Access)</li>
     * </ol>
     * 
     * @throws RemoteException
     * @throws InterruptedException
     */
    @BeforeClass
    public static void runBeforeClass() throws RemoteException,
        InterruptedException {
        initTesters(ALICE, BOB);
        setUpWorkbench();
        setUpSaros();
    }

    @Test
    public void testRenameFolder() throws RemoteException {

        ALICE.superBot().views().packageExplorerView().tree().newC()
            .project("foo");
        ALICE.superBot().internal()
            .createFile("foo", "test/foo.txt", 1024 * 1024 * 6, true);

        Util.buildSessionSequentially("foo", TypeOfCreateProject.NEW_PROJECT,
            ALICE, BOB);

        ALICE.remoteBot().sleep(30000);
        fail();
        Util.setUpSessionWithAJavaProjectAndAClass(Constants.PROJECT1,
            Constants.PKG1, Constants.CLS1, ALICE, BOB);

    }
}
