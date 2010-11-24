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
import de.fu_berlin.inf.dpp.stf.client.test.helpers.STFTest;

public class TestFolderOperations extends STFTest {

    @BeforeClass
    public static void initMusicians() throws RemoteException,
        InterruptedException {
        List<Musician> musicians = InitMusician.initAliceBobCarlConcurrently();
        alice = musicians.get(0);
        bob = musicians.get(1);
        carl = musicians.get(2);

        alice.pEV.newJavaProjectWithClass(PROJECT1, PKG1, CLS1);
        alice.buildSessionConcurrently(PROJECT1, CONTEXT_MENU_SHARE_PROJECT,
            carl, bob);
    }

    @Before
    public void setup() throws RemoteException {
        if (!alice.pEV.isFileExist(getClassPath(PROJECT1, PKG1, CLS1)))
            alice.pEV.newClass(PROJECT1, PKG1, CLS1);
        if (!alice.pEV.isFolderExist(PROJECT1, FOLDER1))
            alice.pEV.newFolder(FOLDER1, PROJECT1);
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
        final String newFolderName = FOLDER1 + "New";

        alice.pEV.renameFolder(newFolderName, PROJECT1, FOLDER1);
        bob.pEV.waitUntilFolderExist(PROJECT1, newFolderName);
        assertTrue(bob.pEV.isFolderExist(PROJECT1, newFolderName));
        assertFalse(bob.pEV.isFolderExist(PROJECT1, FOLDER1));
    }
}
