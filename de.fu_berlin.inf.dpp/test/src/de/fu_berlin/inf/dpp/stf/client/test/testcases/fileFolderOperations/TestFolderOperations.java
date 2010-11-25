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

    /**
     * Preconditions:
     * <ol>
     * <li>alice (Host, Driver), aclice share a java project with bob and carl.</li>
     * <li>bob (Observer)</li>
     * <li>carl (Observer)</li>
     * </ol>
     * 
     * @throws RemoteException
     * @throws InterruptedException
     */
    @BeforeClass
    public static void initMusicians() throws RemoteException,
        InterruptedException {
        /* initialize the musicians simultaneously */
        List<Musician> musicians = InitMusician.initAliceBobCarlConcurrently();
        alice = musicians.get(0);
        bob = musicians.get(1);
        carl = musicians.get(2);

        /* alice build session with bob, and carl simultaneously */
        alice.pEV.newJavaProjectWithClass(PROJECT1, PKG1, CLS1);
        alice.buildSessionConcurrently(PROJECT1, CONTEXT_MENU_SHARE_PROJECT,
            carl, bob);
    }

    /**
     * <ol>
     * <li>make sure, before every test there are only a package PKG1 and a
     * class CLS1 under it in the shared project.</li>
     * <li>make sure,all opened popup windows and editor should be closed.</li>
     * 
     * @throws RemoteException
     */
    @Before
    public void setup() throws RemoteException {
        if (!alice.pEV.isClassExist(PROJECT1, PKG1, CLS1))
            alice.pEV.newClass(PROJECT1, PKG1, CLS1);
        if (!alice.pEV.isFolderExist(PROJECT1, FOLDER1))
            alice.pEV.newFolder(FOLDER1, PROJECT1);
        bob.workbench.resetWorkbench();
        carl.workbench.resetWorkbench();
        alice.workbench.resetWorkbench();
    }

    /**
     * make sure,all opened popup windows and editor should be closed. if you
     * need some more after condition for your tests, please add it.
     * 
     * @throws RemoteException
     */
    @After
    public void cleanUp() throws RemoteException {
        bob.workbench.resetWorkbench();
        carl.workbench.resetWorkbench();
        alice.workbench.resetWorkbench();
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
        carl.workbench.resetSaros();
        alice.workbench.resetSaros();
    }

    /**
     * Steps:
     * <ol>
     * <li>alice rename the folder "FOLDER1" to "newFolderName"</li>
     * </ol>
     * 
     * Result:
     * <ol>
     * <li>the folder'name are renamed by bob too</li>
     * </ol>
     * 
     * @throws RemoteException
     */
    @Test
    public void testRenameFolder() throws RemoteException {
        final String newFolderName = FOLDER1 + "New";

        alice.pEV.renameFolder(newFolderName, PROJECT1, FOLDER1);
        bob.pEV.waitUntilFolderExist(PROJECT1, newFolderName);
        assertTrue(bob.pEV.isFolderExist(PROJECT1, newFolderName));
        assertFalse(bob.pEV.isFolderExist(PROJECT1, FOLDER1));
    }
}
