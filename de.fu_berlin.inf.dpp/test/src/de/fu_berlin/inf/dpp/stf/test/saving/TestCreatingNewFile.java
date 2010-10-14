package de.fu_berlin.inf.dpp.stf.test.saving;

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
    private static final String FILE = BotConfiguration.FILENAME;

    private static final String PKG = BotConfiguration.PACKAGENAME;

    private static final String CLS = BotConfiguration.CLASSNAME;
    private static final String CLS2 = BotConfiguration.CLASSNAME2;
    private static final String CLS3 = BotConfiguration.CLASSNAME3;

    private static final String CP = BotConfiguration.CONTENTPATH;
    private static final String CP2_change = BotConfiguration.CONTENTCHANGEPATH2;

    private static Musician alice;
    private static Musician bob;
    private static Musician carl;

    /**
     * Preconditions:
     * <ol>
     * <li>carl (Host, Driver)</li>
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

        carl.bot.newJavaProjectWithClass(PROJECT, PKG, CLS);

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
     * make sure,all opened popup windows and editor should be closed. if you
     * need some more after condition for your tests, please add it.
     * 
     * @throws RemoteException
     */
    @After
    public void cleanUp() throws RemoteException {
        bob.bot.resetWorkbench();
        alice.bot.resetWorkbench();
        carl.bot.resetWorkbench();
    }

    /**
     * Steps:
     * <ol>
     * <li>alice creates a new file.</li>
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
    public void testExistDirtyFlagByDaveAndEdnaDuringAlicMakeChange()
        throws IOException, CoreException {
        // carl.bot.newClass(project, pkg, name);
        carl.bot.newFolder(PROJECT, FOLDER);
        carl.bot.newFile(FILE, PROJECT, FOLDER);
        alice.bot.waitUntilFileExist(FILE, PROJECT, FOLDER);
        assertTrue(alice.bot.isFileExist(FILE, PROJECT, FOLDER));
        bob.bot.waitUntilFileExist(FILE, PROJECT, FOLDER);
        assertTrue(bob.bot.isFileExist(FILE, PROJECT, FOLDER));
    }

}
