package de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.saving;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.rmi.AccessException;
import java.rmi.RemoteException;

import org.eclipse.core.runtime.CoreException;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.STFTest;

public class TestUserWithWriteAccessResetsFiles extends STFTest {

    /**
     * Preconditions:
     * <ol>
     * 
     * <li>alice (Host, Write Access, all files are closed)</li>
     * <li>bob (Read-Only Access)</li>
     * <li>carl (Read-Only Access)</li>
     * <li>dave (Read-Only in Follow-Mode)</li>
     * <li>edna (Read-Only in Follow-Mode)</li>
     * </ol>
     * 
     * @throws AccessException
     * @throws RemoteException
     * @throws InterruptedException
     */
    @BeforeClass
    public static void runBeforeClass() throws RemoteException,
        InterruptedException {
        initTesters(TypeOfTester.ALICE, TypeOfTester.BOB, TypeOfTester.CARL,
            TypeOfTester.DAVE, TypeOfTester.EDNA);
        setUpWorkbench();
        setUpSaros();
        setUpSessionWithAJavaProjectAndAClass(alice, bob, carl, dave, edna);
    }

    /**
     * Steps:
     * <ol>
     * <li>Alice edits a file without saving</li>
     * <li>Alice closes the file and declines saving the file</li>
     * <li>bob1_fu opens the file with an external text editor(don't need to do)
     * </li>
     * </ol>
     * 
     * Result:
     * <ol>
     * <li></li>
     * <li>Dave and Edna verify that the dirty flag of the file disappears and
     * that the conent is the same as Carl</li>
     * <li>bob verifies that the content of the file is the same as carl</li>
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
        dave.sarosBot().views().sessionView().selectBuddy(alice.jid)
            .followThisBuddy();
        edna.sarosBot().views().sessionView().selectBuddy(alice.jid)
            .followThisBuddy();
        alice.sarosBot().views().packageExplorerView()
            .selectClass(PROJECT1, PKG1, CLS1).open();
        alice.bot().editor(CLS1_SUFFIX).setTextWithoutSave(CP1);
        alice.bot().editor(CLS1 + SUFFIX_JAVA).closeWithoutSave();

        dave.bot().shell(SHELL_SAVE_RESOURCE).confirm(NO);
        edna.bot().shell(SHELL_SAVE_RESOURCE).confirm(NO);

        String contentOfAlice = alice.noBot().getFileContent(
            getClassPath(PROJECT1, PKG1, CLS1));
        String contentOfDave = dave.noBot().getFileContent(
            getClassPath(PROJECT1, PKG1, CLS1));

        String contentOfEdna = edna.noBot().getFileContent(
            getClassPath(PROJECT1, PKG1, CLS1));

        String contentOfBob = bob.noBot().getFileContent(
            getClassPath(PROJECT1, PKG1, CLS1));

        assertTrue(contentOfAlice.equals(contentOfDave));
        assertTrue(contentOfAlice.equals(contentOfEdna));
        assertTrue(contentOfAlice.equals(contentOfBob));
    }
}
