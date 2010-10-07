package de.fu_berlin.inf.dpp.stf.test.saving;

import java.rmi.AccessException;
import java.rmi.RemoteException;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.sarosswtbot.BotConfiguration;
import de.fu_berlin.inf.dpp.stf.sarosswtbot.Musician;
import de.fu_berlin.inf.dpp.stf.test.InitMusician;

public class TestDriverSavesFiles {
    private static final String PROJECT = BotConfiguration.PROJECTNAME;
    private static final String PKG = BotConfiguration.PACKAGENAME;

    private static final String CLS = BotConfiguration.CLASSNAME;
    private static final String CLS2 = BotConfiguration.CLASSNAME2;
    private static final String CLS3 = BotConfiguration.CLASSNAME3;

    private static final String CP = BotConfiguration.CONTENTPATH;
    private static final String CP2 = BotConfiguration.CONTENTPATH2;
    private static final String CP3 = BotConfiguration.CONTENTPATH3;

    private static Musician alice;
    private static Musician bob;
    private static Musician carl;
    private static Musician dave;
    private static Musician edna;

    /**
     * Preconditions:
     * <ol>
     * <li>Alice (Host, Driver)</li>
     * <li>Bob (Observer)</li>
     * <li>Carl (Observer)</li>
     * <li>Dave (Observer in Follow-Mode)</li>
     * <li>Edna (Observer in Follow-Mode)</li>
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
            BotConfiguration.PORT_CARL, BotConfiguration.PORT_DAVE,
            BotConfiguration.PORT_EDNA);
        alice = musicians.get(0);
        bob = musicians.get(1);
        carl = musicians.get(2);
        dave = musicians.get(3);
        edna = musicians.get(4);

        alice.bot.newJavaProjectWithClass(PROJECT, PKG, CLS);

        /*
         * build session with bob and carl simultaneously
         */
        // alice.buildSessionConcurrently(PROJECT,
        // SarosConstant.CONTEXT_MENU_SHARE_PROJECT, bob, carl, dave, edna);
    }

    /**
     * make sure, all opened xmppConnects, popup windows and editor should be
     * closed. make sure, all existed projects should be deleted. if you need
     * some more after class condition for your tests, please add it.
     * 
     * @throws RemoteException
     */
    @AfterClass
    public static void resetSaros() throws RemoteException {
        bob.bot.resetSaros();
        carl.bot.resetSaros();
        dave.bot.resetSaros();
        edna.bot.resetSaros();
        alice.bot.resetSaros();
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
        carl.bot.resetWorkbench();
        dave.bot.resetWorkbench();
        edna.bot.resetWorkbench();
        alice.bot.resetWorkbench();
    }

    /**
     * <ol>
     * <li>Alice makes changes in multiple files without saving the files.</li>
     * <li>Bob opens one of the edited files in an external editor.</li>
     * <li>Carl opens one of the edited files with the Eclipse editor.</li>
     * <li>Carl closes the file in Eclipse.</li>
     * <li>Alice makes more changes to the file that was opened by Carl and
     * saves it.</li>
     * <li>Alice closes a changed (dirty) file and confirms that it will saved.</li>
     * </ol>
     * 
     * Result:
     * <ol>
     * <li>Dave and Edna check that the modified files have a dirty flag (*).</li>
     * <li>Bob should see no changes from Alice in the external opened file.</li>
     * <li>Carl should see the changes from Alice and the dirty flag on the
     * file.</li>
     * <li>Dave and Edna verify that the dirty flag disappears. Bob verifies
     * that the file content has changed with an external editor. Carl opens the
     * file in Eclipse, verifies the correct content and closes the file.</li>
     * <li>Dave and Edna verify that the dirty flag disappears. Bob verifies
     * that the file content has changed with an external editor. Carl opens the
     * file in Eclipse, verifies the correct content and closes the file.</li>
     * </ol>
     * 
     * @throws RemoteException
     */

    @Test
    public void test1() throws RemoteException {

    }

    /**
     * please describe the test using the following form and change the
     * test'name. /** Steps:
     * <ol>
     * <li>step1.</li>
     * <li>step2.</li>
     * <li>step3.</li>
     * </ol>
     * 
     * Result:
     * <ol>
     * <li>result1.</li>
     * <li>result2.</li>
     * <li>result3.</li>
     * </ol>
     * 
     * @throws RemoteException
     */
    @Test
    public void test2() throws RemoteException {

    }
}
