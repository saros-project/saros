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
import de.fu_berlin.inf.dpp.stf.client.test.helpers.InitMusician;
import de.fu_berlin.inf.dpp.stf.server.BotConfiguration;
import de.fu_berlin.inf.dpp.stf.server.SarosConstant;

public class TestCreatingNewFile {
    private static final String PROJECT = BotConfiguration.PROJECTNAME;

    private static final String FOLDER = BotConfiguration.FOLDERNAME;
    private static final String FOLDER2 = BotConfiguration.FOLDERNAME2;
    private static final String FILE = BotConfiguration.FILENAME;
    private static final String FILE2 = BotConfiguration.FILENAME2;

    private static final String CP = BotConfiguration.CONTENTPATH;

    private static Musician alice;
    private static Musician bob;
    private static Musician carl;

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
            BotConfiguration.PORT_ALICE, BotConfiguration.PORT_BOB,
            BotConfiguration.PORT_CARL);
        alice = musicians.get(0);
        bob = musicians.get(1);
        carl = musicians.get(2);

        carl.mainMenu.newProject(PROJECT);

        /*
         * build session with bob, and alice simultaneously
         */
        carl.buildSessionConcurrently(PROJECT,
            SarosConstant.CONTEXT_MENU_SHARE_PROJECT, bob, alice);

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
        if (bob.state.isFolderExist(PROJECT, FOLDER))
            bob.state.deleteFolder(PROJECT, FOLDER);
        if (bob.state.isFolderExist(PROJECT, FOLDER2))
            bob.state.deleteFolder(PROJECT, FOLDER2);
        if (alice.state.isFolderExist(PROJECT, FOLDER))
            alice.state.deleteFolder(PROJECT, FOLDER);
        if (alice.state.isFolderExist(PROJECT, FOLDER2))
            alice.state.deleteFolder(PROJECT, FOLDER2);
        if (carl.state.isFolderExist(PROJECT, FOLDER))
            carl.state.deleteFolder(PROJECT, FOLDER);
        if (carl.state.isFolderExist(PROJECT, FOLDER2))
            carl.state.deleteFolder(PROJECT, FOLDER2);

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
        carl.mainMenu.newFolder(PROJECT, FOLDER);
        carl.mainMenu.newFile(PROJECT, FOLDER, FILE);
        alice.state.waitUntilFileExist(PROJECT, FOLDER, FILE);
        assertTrue(alice.state.isFileExist(PROJECT, FOLDER, FILE));
        bob.state.waitUntilFileExist(PROJECT, FOLDER, FILE);
        assertTrue(bob.state.isFileExist(PROJECT, FOLDER, FILE));
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

        carl.mainMenu.newFolder(PROJECT, FOLDER);
        carl.mainMenu.newFile(PROJECT, FOLDER, FILE);
        alice.basic.sleep(500);
        assertFalse(alice.state.isFileExist(PROJECT, FOLDER, FILE));
        bob.basic.sleep(500);
        assertFalse(bob.state.isFileExist(PROJECT, FOLDER, FILE));

        if (!carl.state.isFollowingUser(alice.getBaseJid()))
            carl.sessionV.followThisUser(alice.state);
        if (!bob.state.isFollowingUser(alice.getBaseJid()))
            bob.sessionV.followThisUser(alice.state);

        alice.mainMenu.newFolder(PROJECT, FOLDER2);
        alice.mainMenu.newFile(PROJECT, FOLDER2, FILE2);

        carl.state.waitUntilFileExist(PROJECT, FOLDER2, FILE2);
        assertTrue(carl.state.isFileExist(PROJECT, FOLDER2, FILE2));
        bob.state.waitUntilFileExist(PROJECT, FOLDER2, FILE2);
        assertTrue(bob.state.isFileExist(PROJECT, FOLDER2, FILE2));

        alice.editor
            .setTextInEditorWithSave(CP, PROJECT, FOLDER2, FILE2);

        String file2ContentOfAlice = alice.editor.getTextOfEditor(
            PROJECT, FOLDER2, FILE2);
        carl.editor.waitUntilEditorContentSame(file2ContentOfAlice,
            PROJECT, FOLDER2, FILE2);
        String file2ContentOfCarl = carl.editor.getTextOfEditor(PROJECT,
            FOLDER2, FILE2);
        assertTrue(file2ContentOfAlice.equals(file2ContentOfCarl));

        bob.editor.waitUntilEditorContentSame(file2ContentOfAlice,
            PROJECT, FOLDER2, FILE2);
        String file2ContentOfBob = bob.editor.getTextOfEditor(PROJECT,
            FOLDER2, FILE2);
        assertTrue(file2ContentOfAlice.equals(file2ContentOfBob));

    }
}
