package de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.saving;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.rmi.RemoteException;

import org.eclipse.core.runtime.CoreException;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.STFTest;

public class TestCreatingNewFile extends STFTest {

    @BeforeClass
    public static void runBeforeClass() throws RemoteException,
        InterruptedException {
        initTesters(TypeOfTester.ALICE, TypeOfTester.BOB, TypeOfTester.CARL);
        setUpWorkbench();
        setUpSaros();
        setUpSessionWithAJavaProjectAndAClass(carl, bob, alice);
    }

    @After
    public void runAfterEveryTest() throws RemoteException {
        deleteFoldersByActiveTesters(FOLDER1, FOLDER2);
    }

    /**
     * Steps:
     * <ol>
     * <li>carl creates a new file.</li>
     * </ol>
     * 
     * Result:
     * <ol>
     * <li>alice and bob should see the new file in the package explorer.</li>
     * </ol>
     * 
     * @throws CoreException
     * @throws IOException
     */

    @Test
    public void testCarlCreateANewFile() throws IOException, CoreException {
        carl.fileM.newFolder(PROJECT1, FOLDER1);
        carl.fileM.newFile(PROJECT1, FOLDER1, FILE1);
        alice.fileM.waitUntilFileExists(PROJECT1, FOLDER1, FILE1);
        assertTrue(alice.fileM.existsFileNoGUI(PROJECT1, FOLDER1, FILE1));
        bob.fileM.waitUntilFileExists(PROJECT1, FOLDER1, FILE1);
        assertTrue(bob.fileM.existsFileNoGUI(PROJECT1, FOLDER1, FILE1));
    }

    /**
     * Steps:
     * <ol>
     * <li>carl grants write access to alice.</li>
     * <li>carl creates a new file named "myFile.xml"</li>
     * <li>bob and carl activate "Follow-Mode"</li>
     * <li>alice creates new XML file myFile2.xml and edits it with the Eclipse
     * XML View</li>
     * </ol>
     * 
     * Result:
     * <ol>
     * <li></li>
     * <li>alice1_fu and bob1_fu should not find the file "myFile.xml"</li>
     * <li></li>
     * <li>bob and carl should see the newly created XML file and the changes
     * made by alice</li>
     * </ol>
     * 
     * @throws CoreException
     * @throws IOException
     * @throws InterruptedException
     */

    @Test
    public void testCarlGrantWriteAccess() throws IOException, CoreException,
        InterruptedException {

        carl.sarosSessionV.restrictToReadOnlyAccess(carl.jid);
        assertFalse(carl.sarosSessionV.hasWriteAccessNoGUI());
        assertTrue(alice.sarosSessionV.hasWriteAccessNoGUI());

        carl.fileM.newFolder(PROJECT1, FOLDER1);
        carl.fileM.newFile(PROJECT1, FOLDER1, FILE1);
        waitsUntilTransferedDataIsArrived(alice);
        assertFalse(alice.fileM.existsFileNoGUI(PROJECT1, FOLDER1, FILE1));
        waitsUntilTransferedDataIsArrived(bob);
        assertFalse(bob.fileM.existsFileNoGUI(PROJECT1, FOLDER1, FILE1));

        setFollowMode(alice, carl, bob);

        alice.fileM.newFolder(PROJECT1, FOLDER2);
        alice.fileM.newFile(PROJECT1, FOLDER2, FILE2);

        carl.fileM.waitUntilFileExists(PROJECT1, FOLDER2, FILE2);
        assertTrue(carl.fileM.existsFileNoGUI(PROJECT1, FOLDER2, FILE2));
        bob.fileM.waitUntilFileExists(PROJECT1, FOLDER2, FILE2);
        assertTrue(bob.fileM.existsFileNoGUI(PROJECT1, FOLDER2, FILE2));

        alice.editor.setTextInEditorWithSave(CP1, PROJECT1, FOLDER2, FILE2);

        String file2ContentOfAlice = alice.editor.getTextOfEditor(PROJECT1,
            FOLDER2, FILE2);
        carl.editor.waitUntilEditorContentSame(file2ContentOfAlice, PROJECT1,
            FOLDER2, FILE2);
        String file2ContentOfCarl = carl.editor.getTextOfEditor(PROJECT1,
            FOLDER2, FILE2);
        assertTrue(file2ContentOfAlice.equals(file2ContentOfCarl));

        bob.editor.waitUntilEditorContentSame(file2ContentOfAlice, PROJECT1,
            FOLDER2, FILE2);
        String file2ContentOfBob = bob.editor.getTextOfEditor(PROJECT1,
            FOLDER2, FILE2);
        assertTrue(file2ContentOfAlice.equals(file2ContentOfBob));

    }
}
