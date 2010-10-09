package de.fu_berlin.inf.dpp.stf.test.fileFolderOperations;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.sarosswtbot.BotConfiguration;
import de.fu_berlin.inf.dpp.stf.sarosswtbot.Musician;
import de.fu_berlin.inf.dpp.stf.sarosswtbot.SarosConstant;
import de.fu_berlin.inf.dpp.stf.test.InitMusician;

public class TestFolderOperations {

    private static Musician alice;
    private static Musician bob;
    private static Musician carl;

    private static final String PKG = BotConfiguration.PACKAGENAME;
    private static final String PKG2 = BotConfiguration.PACKAGENAME2;
    private static final String PROJECT = BotConfiguration.PROJECTNAME;
    private static final String FOLDER = BotConfiguration.FOLDERNAME;
    private static final String CLS = BotConfiguration.CLASSNAME;

    @BeforeClass
    public static void initMusicians() throws RemoteException,
        InterruptedException {
        List<Musician> musicians = InitMusician.initAliceBobCarlConcurrently();
        alice = musicians.get(0);
        bob = musicians.get(1);
        carl = musicians.get(2);

        alice.bot.newJavaProjectWithClass(PROJECT, PKG, CLS);
        alice.buildSessionConcurrently(PROJECT,
            SarosConstant.CONTEXT_MENU_SHARE_PROJECT, carl, bob);
    }

    @Before
    public void setup() throws RemoteException {
        if (!alice.bot.isClassExist(PROJECT, PKG, CLS))
            alice.bot.newClass(PROJECT, PKG, CLS);
        if (!alice.bot.isFolderExist(PROJECT, FOLDER))
            alice.bot.newFolder(PROJECT, FOLDER);
        bob.bot.resetWorkbench();
        carl.bot.resetWorkbench();
        alice.bot.resetWorkbench();
    }

    @After
    public void cleanUp() throws RemoteException {
        bob.bot.resetWorkbench();
        carl.bot.resetWorkbench();
        alice.bot.resetWorkbench();
    }

    @AfterClass
    public static void resetSaros() throws RemoteException {
        bob.bot.resetSaros();
        carl.bot.resetSaros();
        alice.bot.resetSaros();
    }

    @Test
    public void testRenameFolder() throws RemoteException {
        final String newFolderName = FOLDER + "New";

        alice.bot.renameFolder(PROJECT, FOLDER, newFolderName);
        bob.bot.waitUntilFolderExist(PROJECT, newFolderName);
        assertTrue(bob.bot.isFolderExist(PROJECT, newFolderName));
        assertFalse(bob.bot.isFolderExist(PROJECT, FOLDER));
    }
}
