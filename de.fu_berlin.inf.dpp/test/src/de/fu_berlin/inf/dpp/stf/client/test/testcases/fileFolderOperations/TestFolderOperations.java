package de.fu_berlin.inf.dpp.stf.client.test.testcases.fileFolderOperations;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.Musician;
import de.fu_berlin.inf.dpp.stf.client.test.helpers.InitMusician;
import de.fu_berlin.inf.dpp.stf.server.BotConfiguration;
import de.fu_berlin.inf.dpp.stf.server.SarosConstant;

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

        alice.mainMenu.newJavaProjectWithClass(PROJECT, PKG, CLS);
        alice.buildSessionConcurrently(PROJECT,
            SarosConstant.CONTEXT_MENU_SHARE_PROJECT, carl, bob);
    }

    @Before
    public void setup() throws RemoteException {
        if (!alice.state.existsClass(PROJECT, PKG, CLS))
            alice.mainMenu.newClass(PROJECT, PKG, CLS);
        if (!alice.state.isFolderExist(PROJECT, FOLDER))
            alice.mainMenu.newFolder(PROJECT, FOLDER);
        bob.workbench.resetWorkbench();
        carl.workbench.resetWorkbench();
        alice.workbench.resetWorkbench();
    }

    @After
    public void cleanUp() throws RemoteException {
        bob.workbench.resetWorkbench();
        carl.workbench.resetWorkbench();
        alice.workbench.resetWorkbench();
    }

    @AfterClass
    public static void resetSaros() throws RemoteException {
        bob.workbench.resetSaros();
        carl.workbench.resetSaros();
        alice.workbench.resetSaros();
    }

    @Test
    public void testRenameFolder() throws RemoteException {
        final String newFolderName = FOLDER + "New";

        alice.pEV.renameFolder(PROJECT, FOLDER, newFolderName);
        bob.state.waitUntilFolderExist(PROJECT, newFolderName);
        assertTrue(bob.state.isFolderExist(PROJECT, newFolderName));
        assertFalse(bob.state.isFolderExist(PROJECT, FOLDER));
    }
}
