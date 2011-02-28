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
        carl.sarosBot().file().newFolder(PROJECT1, FOLDER1);
        carl.sarosBot().file().newFile(PROJECT1, FOLDER1, FILE1);
        alice.sarosBot().condition()
            .waitUntilFileExists(PROJECT1, FOLDER1, FILE1);
        assertTrue(alice.sarosBot().state()
            .existsFileNoGUI(PROJECT1, FOLDER1, FILE1));
        bob.sarosBot().condition()
            .waitUntilFileExists(PROJECT1, FOLDER1, FILE1);
        assertTrue(bob.sarosBot().state()
            .existsFileNoGUI(PROJECT1, FOLDER1, FILE1));
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

        carl.sarosBot().sessionView().selectBuddy(carl.jid)
            .restrictToReadOnlyAccess();
        assertFalse(carl.sarosBot().state().hasWriteAccessNoGUI());
        assertTrue(alice.sarosBot().state().hasWriteAccessNoGUI());

        carl.sarosBot().file().newFolder(PROJECT1, FOLDER1);
        carl.sarosBot().file().newFile(PROJECT1, FOLDER1, FILE1);
        waitsUntilTransferedDataIsArrived(alice);
        assertFalse(alice.sarosBot().state()
            .existsFileNoGUI(PROJECT1, FOLDER1, FILE1));
        waitsUntilTransferedDataIsArrived(bob);
        assertFalse(bob.sarosBot().state()
            .existsFileNoGUI(PROJECT1, FOLDER1, FILE1));

        setFollowMode(alice, carl, bob);

        alice.sarosBot().file().newFolder(PROJECT1, FOLDER2);
        alice.sarosBot().file().newFile(PROJECT1, FOLDER2, FILE2);

        carl.sarosBot().condition()
            .waitUntilFileExists(PROJECT1, FOLDER2, FILE2);
        assertTrue(carl.sarosBot().state()
            .existsFileNoGUI(PROJECT1, FOLDER2, FILE2));
        bob.sarosBot().condition()
            .waitUntilFileExists(PROJECT1, FOLDER2, FILE2);
        assertTrue(bob.sarosBot().state()
            .existsFileNoGUI(PROJECT1, FOLDER2, FILE2));

        alice.bot().editor(FILE2).setTexWithSave(CP1);

        String file2ContentOfAlice = alice.bot().editor(FILE2).getText();
        carl.bot().editor(FILE2).waitUntilIsTextSame(file2ContentOfAlice);
        String file2ContentOfCarl = carl.bot().editor(FILE2).getText();
        assertTrue(file2ContentOfAlice.equals(file2ContentOfCarl));

        bob.bot().editor(FILE2).waitUntilIsTextSame(file2ContentOfAlice);
        String file2ContentOfBob = bob.bot().editor(FILE2).getText();
        assertTrue(file2ContentOfAlice.equals(file2ContentOfBob));

    }
}
