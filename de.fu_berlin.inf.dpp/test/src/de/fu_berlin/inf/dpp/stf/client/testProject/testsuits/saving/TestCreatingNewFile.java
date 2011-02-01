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
        setUpWorkbenchs();
        setUpSaros();
        carl.fileM.newProject(PROJECT1);

        /*
         * carl build session with bob, and alice simultaneously
         */
        carl.buildSessionDoneConcurrently(PROJECT1,
            TypeOfShareProject.SHARE_PROJECT, TypeOfCreateProject.NEW_PROJECT,
            bob, alice);

    }

    @After
    public void runAfterEveryTest() throws RemoteException {
        deleteFolders(FOLDER1, FOLDER2);
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
        carl.fileM.newFolder(FOLDER1, PROJECT1);
        carl.fileM.newFile(PROJECT1, FOLDER1, FILE1);
        alice.fileM.waitUntilFileExisted(PROJECT1, FOLDER1, FILE1);
        assertTrue(alice.fileM.existsFile(PROJECT1, FOLDER1, FILE1));
        bob.fileM.waitUntilFileExisted(PROJECT1, FOLDER1, FILE1);
        assertTrue(bob.fileM.existsFile(PROJECT1, FOLDER1, FILE1));
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

        carl.fileM.newFolder(FOLDER1, PROJECT1);
        carl.fileM.newFile(PROJECT1, FOLDER1, FILE1);
        alice.workbench.sleep(500);
        assertFalse(alice.fileM.existsFile(PROJECT1, FOLDER1, FILE1));
        bob.workbench.sleep(500);
        assertFalse(bob.fileM.existsFile(PROJECT1, FOLDER1, FILE1));

        // if (!carl.sarosSessionV.isFollowingBuddy(alice.getBaseJid()))
        // carl.sarosSessionV.followThisBuddyGUI(alice.jid);
        // if (!bob.sarosSessionV.isFollowingBuddy(alice.getBaseJid()))
        // bob.sarosSessionV.followThisBuddyGUI(alice.jid);
        alice.followedBy(carl, bob);

        alice.fileM.newFolder(PROJECT1, FOLDER2);
        alice.fileM.newFile(PROJECT1, FOLDER2, FILE2);

        carl.fileM.waitUntilFileExisted(PROJECT1, FOLDER2, FILE2);
        assertTrue(carl.fileM.existsFile(PROJECT1, FOLDER2, FILE2));
        bob.fileM.waitUntilFileExisted(PROJECT1, FOLDER2, FILE2);
        assertTrue(bob.fileM.existsFile(PROJECT1, FOLDER2, FILE2));

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
