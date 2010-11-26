package de.fu_berlin.inf.dpp.stf.client.test.testcases.fileFolderOperations;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.MusicianConfigurationInfos;
import de.fu_berlin.inf.dpp.stf.client.test.helpers.InitMusician;
import de.fu_berlin.inf.dpp.stf.client.test.helpers.STFTest;

public class TestSVNStateUpdates extends STFTest {
    /**
     * Preconditions:
     * <ol>
     * <li>Alice (Host, Driver)</li>
     * <li>Bob (Observer)</li>
     * <li>Alice has the project {@link STFTest#SVN_PROJECT_COPY}, which is
     * checked out from SVN:<br>
     * repository: {@link STFTest#SVN_REPOSITORY_URL}<br>
     * path: {@link STFTest#SVN_PROJECT_PATH}
     * </ol>
     * 
     * @throws RemoteException
     */
    @BeforeClass
    public static void beforeClass() throws RemoteException {
        alice = InitMusician.newAlice();
        bob = InitMusician.newBob();
        if (!alice.pEV.isProjectExist(SVN_PROJECT_COPY)) {
            alice.pEV.newJavaProject(SVN_PROJECT_COPY);
            alice.pEV.shareProjectWithSVNUsingSpecifiedFolderName(
                SVN_PROJECT_COPY, SVN_REPOSITORY_URL, SVN_PROJECT_PATH);
        }
    }

    /**
     * Preconditions:
     * <ol>
     * <li>Alice copied {@link STFTest#SVN_PROJECT_COPY} to
     * {@link STFTest#SVN_PROJECT}.</li>
     * </ol>
     * Only SVN_PROJECT is used in the tests. Copying from SVN_PROJECT_COPY is
     * faster than checking out the project for every test.
     * 
     * @throws RemoteException
     */
    @Before
    public void before() throws RemoteException {
        alice.pEV.copyProject(SVN_PROJECT, SVN_PROJECT_COPY);
        assertTrue(alice.pEV.isProjectExist(SVN_PROJECT));
        assertTrue(alice.pEV.isProjectManagedBySVN(SVN_PROJECT));
        assertTrue(alice.pEV.isFileExist(SVN_CLS1_FULL_PATH));
    }

    @After
    public void after() throws RemoteException {
        bob.sessionV.leaveTheSessionByPeer();
        bob.workbench.resetWorkbench();
        if (alice.pEV.isProjectExist(SVN_PROJECT))
            alice.pEV.deleteProject(SVN_PROJECT);
        bob.state.deleteAllProjects();

        alice.sessionV.leaveTheSessionByHost();
        alice.workbench.resetWorkbench();
    }

    @AfterClass
    public static void afterClass() throws RemoteException {
        bob.workbench.resetSaros();
        if (MusicianConfigurationInfos.DEVELOPMODE) {
            alice.rosterV.disconnect();
            // don't delete SVN_PROJECT_COPY
        } else {
            alice.workbench.resetSaros();
        }
    }

    /**
     * Steps:
     * <ol>
     * <li>Alice makes Bob exclusive driver.</li>
     * <li>Bob renames the file {@link STFTest#SVN_CLS1} to "Asdf".</li>
     * </ol>
     * Result:
     * <ol>
     * <li>The file {@link STFTest#SVN_CLS1} in Alice's copy gets renamed to
     * "Asdf".</li>
     * </ol>
     * 
     * @throws RemoteException
     */
    @Test
    public void testChangeDriverAndRenameClass() throws Exception {
        alice.shareProjectWithDone(SVN_PROJECT,
            CONTEXT_MENU_SHARE_PROJECT_WITH_VCS, bob);
        alice.sessionV.waitUntilSessionOpenBy(bob.state);
        alice.sessionV.giveExclusiveDriverRole(bob.state);
        bob.pEV.renameClass("Asdf", SVN_PROJECT, SVN_PKG, SVN_CLS1);

        alice.pEV.waitUntilFileExist(SVN_PROJECT, "src", SVN_PKG, "Asdf.java");
        assertTrue(alice.pEV.isClassExist(SVN_PROJECT, SVN_PKG, "Asdf"));
    }

    /**
     * Steps:
     * <ol>
     * <li>Alice makes Bob exclusive driver.</li>
     * <li>Bob creates new package "new_package".</li>
     * <li>Bob moves the file {@link STFTest#SVN_CLS1} to "new_package".</li>
     * </ol>
     * Result:
     * <ol>
     * <li>The package "new_package" gets created in Alice's copy.</li>
     * <li>The file {@link STFTest#SVN_CLS1} in Alice's copy gets moved to
     * "new_package".</li>
     * </ol>
     * 
     * @throws RemoteException
     */
    @Test
    public void testChangeDriverAndMoveClass() throws Exception {
        alice.shareProjectWithDone(SVN_PROJECT,
            CONTEXT_MENU_SHARE_PROJECT_WITH_VCS, bob);
        alice.sessionV.waitUntilSessionOpenBy(bob.state);
        alice.sessionV.giveExclusiveDriverRole(bob.state);

        bob.pEV.newPackage(SVN_PROJECT, "new_package");
        alice.pEV.waitUntilFolderExist(SVN_PROJECT, "src", "new_package");

        bob.pEV.moveClassTo(SVN_PROJECT, SVN_PKG, SVN_CLS1, SVN_PROJECT,
            "new_package");

        alice.pEV.waitUntilClassExist(SVN_PROJECT, "new_package", SVN_CLS1);
        assertTrue(alice.pEV.isClassExist(SVN_PROJECT, "new_package", SVN_CLS1));
    }

    /**
     * Steps:
     * <ol>
     * <li>Alice switches to branch "testing".</li>
     * </ol>
     * Result:
     * <ol>
     * <li>Make sure Bob is switched to branch "testing".</li>
     * </ol>
     * 
     * @throws RemoteException
     */
    @Test
    @Ignore
    public void testSwitch() throws RemoteException {
        alice.pEV.switchProject(SVN_PROJECT, SVN_TAG_URL);
        bob.pEV.waitUntilWindowSarosRunningVCSOperationClosed();
        assertTrue(alice.pEV.getURLOfRemoteResource(SVN_CLS_PATH).equals(
            bob.pEV.getURLOfRemoteResource(SVN_CLS_PATH)));
    }

    /**
     * Steps:
     * <ol>
     * <li>Alice disconnects project "test" from SVN.</li>
     * </ol>
     * Result:
     * <ol>
     * <li>Make sure Bob is disconnected.</li>
     * </ol>
     * 
     * @throws RemoteException
     */
    @Test
    @Ignore
    public void testDisconnectAndConnect() throws RemoteException {
        alice.pEV.disConnect(SVN_PROJECT);
        bob.pEV.waitUntilProjectNotInSVN(SVN_PROJECT);
        assertFalse(bob.pEV.isProjectManagedBySVN(SVN_PROJECT));
        alice.pEV.shareProjectWithSVNWhichIsConfiguredWithSVNInfos(SVN_PROJECT,
            SVN_URL);
        bob.pEV.waitUntilWindowSarosRunningVCSOperationClosed();
        bob.pEV.waitUntilProjectInSVN(SVN_PROJECT);
        assertTrue(bob.pEV.isProjectManagedBySVN(SVN_PROJECT));
    }

    /**
     * Steps:
     * <ol>
     * <li>Alice updates the entire project to the older revision Y (< HEAD)..</li>
     * </ol>
     * Result:
     * <ol>
     * <li>Bob's revision of "test" is Y</li>
     * </ol>
     * 
     * @throws RemoteException
     */
    @Test
    @Ignore
    public void testUpdate() throws RemoteException {
        alice.pEV.updateProject(SVN_PROJECT, "115");
        bob.pEV.waitUntilWindowSarosRunningVCSOperationClosed();
        assertTrue(alice.pEV.getURLOfRemoteResource(SVN_PROJECT).equals(
            bob.pEV.getURLOfRemoteResource(SVN_PROJECT)));
        alice.pEV.updateProject(SVN_PROJECT, "116");
        bob.pEV.waitUntilWindowSarosRunningVCSOperationClosed();
    }

    /**
     * Steps:
     * <ol>
     * <li>Alice updates the file Main.java to the older revision Y (< HEAD)</li>
     * </ol>
     * Result:
     * <ol>
     * <li>Bob's revision of file "src/main/Main.java" is Y and Bob's revision
     * of project "test" is HEAD.</li>
     * </ol>
     * 
     * @throws RemoteException
     */
    @Test
    @Ignore
    public void testUpdateSingleFile() throws RemoteException {
        alice.pEV.updateClass(SVN_PROJECT, SVN_PKG, SVN_CLS1, "102");
        bob.pEV.waitUntilWindowSarosRunningVCSOperationClosed();
        assertTrue(alice.pEV.getRevision(SVN_CLS_PATH).equals("102"));
        bob.pEV.waitUntilRevisionIsSame(SVN_CLS_PATH, "102");
        assertTrue(bob.pEV.getRevision(SVN_CLS_PATH).equals("102"));
        bob.pEV.waitUntilRevisionIsSame(SVN_PROJECT, "116");
        assertTrue(bob.pEV.getRevision(SVN_PROJECT).equals("116"));
        alice.pEV.updateClass(SVN_PROJECT, SVN_PKG, SVN_CLS1, "116");
        bob.pEV.waitUntilWindowSarosRunningVCSOperationClosed();
    }

    /**
     * Steps:
     * <ol>
     * <li>Alice deletes the file SVN_CLS_PATH</li>
     * <li>Alice reverts the project</li>
     * </ol>
     * Result:
     * <ol>
     * <li>Bob has no file SVN_CLS_PATH</li>
     * <li>Bob has the file SVN_CLS_PATH</li>
     * </ol>
     * 
     * @throws RemoteException
     */
    @Test
    @Ignore
    public void testRevert() throws RemoteException {
        alice.pEV.deleteProject(SVN_CLS_PATH);
        bob.pEV.waitUntilClassNotExist(SVN_PROJECT, SVN_PKG, SVN_CLS1);
        assertFalse(bob.pEV.isFileExist(SVN_CLS_PATH));
        alice.pEV.revertProject(SVN_PROJECT);
        bob.pEV.waitUntilClassExist(SVN_PROJECT, SVN_PKG, SVN_CLS1);
        assertTrue(bob.pEV.isFileExist(SVN_CLS_PATH));
    }

}
