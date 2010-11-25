package de.fu_berlin.inf.dpp.stf.client.test.testcases.saving;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.rmi.AccessException;
import java.rmi.RemoteException;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.Musician;
import de.fu_berlin.inf.dpp.stf.client.MusicianConfigurationInfos;
import de.fu_berlin.inf.dpp.stf.client.test.helpers.InitMusician;
import de.fu_berlin.inf.dpp.stf.client.test.helpers.STFTest;

public class TestCreatingNewFile extends STFTest {

    /**
     * Preconditions:
     * <ol>
     * <li>carl (Host, Driver), carl share a java project with alice and bob.</li>
     * <li>alice (Observer)</li>
     * <li>bob (Observer)</li>
     * </ol>
     * 
     * @throws AccessException
     * @throws RemoteException
     * @throws InterruptedException
     */
    @BeforeClass
    public static void initMusican() throws AccessException, RemoteException,
        InterruptedException {
        /*
         * initialize the musicians simultaneously
         */
        List<Musician> musicians = InitMusician.initMusiciansConcurrently(
            MusicianConfigurationInfos.PORT_ALICE,
            MusicianConfigurationInfos.PORT_BOB,
            MusicianConfigurationInfos.PORT_CARL);
        alice = musicians.get(0);
        bob = musicians.get(1);
        carl = musicians.get(2);

        carl.pEV.newProject(PROJECT1);

        /*
         * carl build session with bob, and alice simultaneously
         */
        carl.buildSessionConcurrently(PROJECT1, CONTEXT_MENU_SHARE_PROJECT,
            bob, alice);

    }

    /**
     * make sure, all opened xmppConnects, popup windows and editor should be
     * closed. make sure, all existed projects should be deleted.
     * 
     * @throws RemoteException
     */
    @AfterClass
    public static void resetSaros() throws RemoteException {
        bob.workbench.resetSaros();
        alice.workbench.resetSaros();
        carl.workbench.resetSaros();
    }

    /**
     * make sure,all opened pop up windows and editor should be closed.
     * 
     * @throws RemoteException
     */
    @After
    public void cleanUp() throws RemoteException {
        bob.workbench.resetWorkbench();
        alice.workbench.resetWorkbench();
        carl.workbench.resetWorkbench();
        if (bob.pEV.isFolderExist(PROJECT1, FOLDER1))
            bob.pEV.deleteFolder(PROJECT1, FOLDER1);
        if (bob.pEV.isFolderExist(PROJECT1, FOLDER2))
            bob.pEV.deleteFolder(PROJECT1, FOLDER2);
        if (alice.pEV.isFolderExist(PROJECT1, FOLDER1))
            alice.pEV.deleteFolder(PROJECT1, FOLDER1);
        if (alice.pEV.isFolderExist(PROJECT1, FOLDER2))
            alice.pEV.deleteFolder(PROJECT1, FOLDER2);
        if (carl.pEV.isFolderExist(PROJECT1, FOLDER1))
            carl.pEV.deleteFolder(PROJECT1, FOLDER1);
        if (carl.pEV.isFolderExist(PROJECT1, FOLDER2))
            carl.pEV.deleteFolder(PROJECT1, FOLDER2);

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
        carl.pEV.newFolder(FOLDER1, PROJECT1);
        carl.pEV.newFile(PROJECT1, FOLDER1, FILE1);
        alice.pEV.waitUntilFileExist(PROJECT1, FOLDER1, FILE1);
        assertTrue(alice.pEV.isFileExist(getPath(PROJECT1, FOLDER1, FILE1)));
        bob.pEV.waitUntilFileExist(PROJECT1, FOLDER1, FILE1);
        assertTrue(bob.pEV.isFileExist(getPath(PROJECT1, FOLDER1, FILE1)));
    }

    /**
     * Steps:
     * <ol>
     * <li>carl assigns exclusive driver role to alice.</li>
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
     */

    @Test
    public void testCarlGiveExclusiveDriverRole() throws IOException,
        CoreException {
        carl.sessionV.giveExclusiveDriverRole(alice.state);

        assertFalse(carl.state.isDriver(carl.jid));
        assertTrue(alice.state.isDriver(alice.jid));
        assertTrue(carl.state.isDriver(alice.jid));
        assertTrue(bob.state.isDriver(alice.jid));

        carl.pEV.newFolder(FOLDER1, PROJECT1);
        carl.pEV.newFile(PROJECT1, FOLDER1, FILE1);
        alice.basic.sleep(500);
        assertFalse(alice.pEV.isFileExist(getPath(PROJECT1, FOLDER1, FILE1)));
        bob.basic.sleep(500);
        assertFalse(bob.pEV.isFileExist(getPath(PROJECT1, FOLDER1, FILE1)));

        if (!carl.state.isFollowingUser(alice.getBaseJid()))
            carl.sessionV.followThisUser(alice.state);
        if (!bob.state.isFollowingUser(alice.getBaseJid()))
            bob.sessionV.followThisUser(alice.state);

        alice.pEV.newFolder(PROJECT1, FOLDER2);
        alice.pEV.newFile(PROJECT1, FOLDER2, FILE2);

        carl.pEV.waitUntilFileExist(PROJECT1, FOLDER2, FILE2);
        assertTrue(carl.pEV.isFileExist(getPath(PROJECT1, FOLDER2, FILE2)));
        bob.pEV.waitUntilFileExist(PROJECT1, FOLDER2, FILE2);
        assertTrue(bob.pEV.isFileExist(getPath(PROJECT1, FOLDER2, FILE2)));

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
