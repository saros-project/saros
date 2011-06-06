package de.fu_berlin.inf.dpp.stf.test.saving;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.BOB;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.CARL;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.DAVE;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.EDNA;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.NO;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.SHELL_SAVE_RESOURCE;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.SUFFIX_JAVA;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.rmi.AccessException;
import java.rmi.RemoteException;

import org.eclipse.core.runtime.CoreException;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.StfTestCase;
import de.fu_berlin.inf.dpp.stf.client.util.Util;
import de.fu_berlin.inf.dpp.stf.test.Constants;

public class UserWithWriteAccessResetsFilesTest extends StfTestCase {

    /**
     * Preconditions:
     * <ol>
     * 
     * <li>ALICE (Host, Write Access, all files are closed)</li>
     * <li>BOB (Read-Only Access)</li>
     * <li>CARL (Read-Only Access)</li>
     * <li>DAVE (Read-Only in Follow-Mode)</li>
     * <li>EDNA (Read-Only in Follow-Mode)</li>
     * </ol>
     * 
     * @throws AccessException
     * @throws RemoteException
     */
    @BeforeClass
    public static void runBeforeClass() throws RemoteException {
        initTesters(ALICE, BOB, CARL, DAVE, EDNA);
        setUpWorkbench();
        setUpSaros();
        Util.setUpSessionWithAJavaProjectAndAClass(Constants.PROJECT1,
            Constants.PKG1, Constants.CLS1, ALICE, BOB, CARL, DAVE, EDNA);
    }

    /**
     * Steps:
     * <ol>
     * <li>Alice edits a file without saving</li>
     * <li>Alice closes the file and declines saving the file</li>
     * <li>BOB1_fu opens the file with an external text editor(don't need to do)
     * </li>
     * </ol>
     * 
     * Result:
     * <ol>
     * <li></li>
     * <li>Dave and Edna verify that the dirty flag of the file disappears and
     * that the conent is the same as Carl</li>
     * <li>BOB verifies that the content of the file is the same as CARL</li>
     * </ol>
     * 
     * @throws CoreException
     * @throws IOException
     * 
     *             FIXME: this test don't work under OS MAC, because there are
     *             no popUp window "save source".
     */
    @Test
    public void testAliceResetsFile() throws IOException, CoreException {
        DAVE.superBot().views().sarosView().selectParticipant(ALICE.getJID())
            .followParticipant();
        EDNA.superBot().views().sarosView().selectParticipant(ALICE.getJID())
            .followParticipant();
        ALICE.superBot().views().packageExplorerView()
            .selectClass(Constants.PROJECT1, Constants.PKG1, Constants.CLS1)
            .open();
        ALICE.remoteBot().editor(Constants.CLS1_SUFFIX)
            .setTextWithoutSave(Constants.CP1);
        ALICE.remoteBot().editor(Constants.CLS1 + SUFFIX_JAVA)
            .closeWithoutSave();

        DAVE.remoteBot().shell(SHELL_SAVE_RESOURCE).confirm(NO);
        EDNA.remoteBot().shell(SHELL_SAVE_RESOURCE).confirm(NO);

        // String contentOfDave = DAVE.superBot().views().packageExplorerView()
        // .getFileContent(getClassPath(PROJECT1, PKG1, CLS1));
        String contentOfAlice = ALICE
            .superBot()
            .views()
            .packageExplorerView()
            .getFileContent(
                Util.getClassPath(Constants.PROJECT1, Constants.PKG1,
                    Constants.CLS1));

        String contentOfEdna = EDNA
            .superBot()
            .views()
            .packageExplorerView()
            .getFileContent(
                Util.getClassPath(Constants.PROJECT1, Constants.PKG1,
                    Constants.CLS1));

        String contentOfBob = BOB
            .superBot()
            .views()
            .packageExplorerView()
            .getFileContent(
                Util.getClassPath(Constants.PROJECT1, Constants.PKG1,
                    Constants.CLS1));

        // assertTrue(contentOfAlice.equals(contentOfDave));
        assertTrue(contentOfAlice.equals(contentOfEdna));
        assertTrue(contentOfAlice.equals(contentOfBob));
    }
}
