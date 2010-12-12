package de.fu_berlin.inf.dpp.stf.client.test.testcases.saving;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.rmi.AccessException;
import java.rmi.RemoteException;

import org.eclipse.core.runtime.CoreException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.test.helpers.STFTest;

public class TestDriverResetsFiles extends STFTest {

    /**
     * Preconditions:
     * <ol>
     * 
     * <li>alice1_fu (Host, Driver, all files are closed)</li>
     * <li>bob1_fu (Observer)</li>
     * <li>carl1_fu (Observer)</li>
     * <li>dave1_fu (Observer in Follow-Mode)</li>
     * <li>edna1_fu (Observer in Follow-Mode)</li>
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
        setUpWorkbenchs();
        setUpSaros();
        setUpSession(alice, bob, carl, dave, edna);
    }

    @AfterClass
    public static void runAfterClass() throws RemoteException,
        InterruptedException {
        alice.leaveSessionHostFirstDone(bob, carl, dave, edna);
    }

    @Before
    public void runBeforeEveryTest() {
        //
    }

    @After
    public void runAfterEveryTest() {
        //
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
     */

    @Test
    public void testAliceResetsFile() throws IOException, CoreException {
        dave.sessionV.followThisUserGUI(alice.jid);
        edna.sessionV.followThisUserGUI(alice.jid);
        alice.editor.setTextInJavaEditorWithoutSave(CP1, PROJECT1, PKG1, CLS1);

        alice.editor.closejavaEditorWithoutSave(CLS1);
        dave.editor.confirmWindowSaveSource(NO);
        edna.editor.confirmWindowSaveSource(NO);

        String contentOfAlice = alice.state.getClassContent(PROJECT1, PKG1,
            CLS1);
        String contentOfDave = dave.state.getClassContent(PROJECT1, PKG1, CLS1);

        String contentOfEdna = edna.state.getClassContent(PROJECT1, PKG1, CLS1);

        String contentOfBob = bob.state.getClassContent(PROJECT1, PKG1, CLS1);

        assertTrue(contentOfAlice.equals(contentOfDave));
        assertTrue(contentOfAlice.equals(contentOfEdna));
        assertTrue(contentOfAlice.equals(contentOfBob));
    }
}
