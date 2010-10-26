package de.fu_berlin.inf.dpp.stf.test.saving;

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

import de.fu_berlin.inf.dpp.stf.sarosswtbot.BotConfiguration;
import de.fu_berlin.inf.dpp.stf.sarosswtbot.Musician;
import de.fu_berlin.inf.dpp.stf.sarosswtbot.SarosConstant;
import de.fu_berlin.inf.dpp.stf.test.InitMusician;

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

        carl.bot.newProject(PROJECT);

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
        bob.bot.resetSaros();
        alice.bot.resetSaros();
        carl.bot.resetSaros();
    }

    /**
     * make sure,all opened pop up windows and editor should be closed.
     * 
     * @throws RemoteException
     */
    @After
    public void cleanUp() throws RemoteException {
        bob.bot.resetWorkbench();
        alice.bot.resetWorkbench();
        carl.bot.resetWorkbench();
        if (bob.bot.isFolderExist(PROJECT, FOLDER))
            bob.bot.deleteFolder(PROJECT, FOLDER);
        if (bob.bot.isFolderExist(PROJECT, FOLDER2))
            bob.bot.deleteFolder(PROJECT, FOLDER2);
        if (alice.bot.isFolderExist(PROJECT, FOLDER))
            alice.bot.deleteFolder(PROJECT, FOLDER);
        if (alice.bot.isFolderExist(PROJECT, FOLDER2))
            alice.bot.deleteFolder(PROJECT, FOLDER2);
        if (carl.bot.isFolderExist(PROJECT, FOLDER))
            carl.bot.deleteFolder(PROJECT, FOLDER);
        if (carl.bot.isFolderExist(PROJECT, FOLDER2))
            carl.bot.deleteFolder(PROJECT, FOLDER2);

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
        carl.bot.newFolder(PROJECT, FOLDER);
        carl.bot.newFile(PROJECT, FOLDER, FILE);
        alice.bot.waitUntilFileExist(PROJECT, FOLDER, FILE);
        assertTrue(alice.bot.isFileExist(PROJECT, FOLDER, FILE));
        bob.bot.waitUntilFileExist(PROJECT, FOLDER, FILE);
        assertTrue(bob.bot.isFileExist(PROJECT, FOLDER, FILE));
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
        carl.bot.giveExclusiveDriverRole(alice.getPlainJid());

        assertFalse(carl.state.isDriver(carl.jid));
        assertTrue(alice.state.isDriver(alice.jid));
        assertTrue(carl.state.isDriver(alice.jid));
        assertTrue(bob.state.isDriver(alice.jid));

        carl.bot.newFolder(PROJECT, FOLDER);
        carl.bot.newFile(PROJECT, FOLDER, FILE);
        alice.bot.sleep(500);
        assertFalse(alice.bot.isFileExist(PROJECT, FOLDER, FILE));
        bob.bot.sleep(500);
        assertFalse(bob.bot.isFileExist(PROJECT, FOLDER, FILE));

        if (!carl.state.isFollowingUser(alice.getPlainJid()))
            carl.bot.followUser(alice.state, alice.jid);
        if (!bob.state.isFollowingUser(alice.getPlainJid()))
            bob.bot.followUser(alice.state, alice.jid);

        alice.bot.newFolder(PROJECT, FOLDER2);
        alice.bot.newFile(PROJECT, FOLDER2, FILE2);

        carl.bot.waitUntilFileExist(PROJECT, FOLDER2, FILE2);
        assertTrue(carl.bot.isFileExist(PROJECT, FOLDER2, FILE2));
        bob.bot.waitUntilFileExist(PROJECT, FOLDER2, FILE2);
        assertTrue(bob.bot.isFileExist(PROJECT, FOLDER2, FILE2));

        alice.bot.setTextInEditorWithSave(CP, PROJECT, FOLDER2, FILE2);

        String file2ContentOfAlice = alice.bot.getTextOfEditor(PROJECT,
            FOLDER2, FILE2);
        carl.bot.waitUntilEditorContentSame(file2ContentOfAlice, PROJECT,
            FOLDER2, FILE2);
        String file2ContentOfCarl = carl.bot.getTextOfEditor(PROJECT, FOLDER2,
            FILE2);
        assertTrue(file2ContentOfAlice.equals(file2ContentOfCarl));

        bob.bot.waitUntilEditorContentSame(file2ContentOfAlice, PROJECT,
            FOLDER2, FILE2);
        String file2ContentOfBob = bob.bot.getTextOfEditor(PROJECT, FOLDER2,
            FILE2);
        assertTrue(file2ContentOfAlice.equals(file2ContentOfBob));

    }
}
