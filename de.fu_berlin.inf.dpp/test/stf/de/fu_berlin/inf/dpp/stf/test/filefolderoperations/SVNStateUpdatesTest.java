package de.fu_berlin.inf.dpp.stf.test.filefolderoperations;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.BOB;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.StfTestCase;
import de.fu_berlin.inf.dpp.stf.client.tester.AbstractTester;
import de.fu_berlin.inf.dpp.stf.client.util.Util;
import de.fu_berlin.inf.dpp.stf.shared.Constants.TypeOfCreateProject;
import de.fu_berlin.inf.dpp.stf.test.Constants;

public class SVNStateUpdatesTest extends StfTestCase {
    /**
     * Preconditions:
     * <ol>
     * <li>Alice (Host, Write Access)</li>
     * <li>Bob (Read-Only Access)</li>
     * <li>Alice and Bob both have the project which is checked out from SVN:<br>
     * repository: {@link Constants#SVN_REPOSITORY_URL}<br>
     * path: {@link Constants#SVN_PROJECT_PATH}
     * </ol>
     * 
     * @throws RemoteException
     */
    @BeforeClass
    public static void runBeforeClass() throws Exception {
        initTesters(ALICE, BOB);
        setUpWorkbench();
        setUpSaros();

        List<Callable<Void>> initTasks = new ArrayList<Callable<Void>>();
        for (final AbstractTester t : getCurrentTesters()) {
            initTasks.add(new Callable<Void>() {
                public Void call() throws Exception {
                    if (!t.superBot().views().packageExplorerView().tree()
                        .existsWithRegex(Constants.SVN_PROJECT_COPY)) {
                        t.superBot().views().packageExplorerView().tree()
                            .newC().javaProject(Constants.SVN_PROJECT_COPY);
                        t.superBot()
                            .views()
                            .packageExplorerView()
                            .selectProject(Constants.SVN_PROJECT_COPY)
                            .team()
                            .shareProjectUsingSpecifiedFolderName(
                                Constants.SVN_REPOSITORY_URL,
                                Constants.SVN_PROJECT_PATH);

                    }
                    return null;
                }
            });
        }
        Util.workAll(initTasks);

    }

    /**
     * Preconditions:
     * <ol>
     * <li>Alice and Bob copied {@link Constants#SVN_PROJECT_COPY} to
     * {@link Constants#SVN_PROJECT}.</li>
     * </ol>
     * Only SVN_PROJECT is used in the tests. Copying from SVN_PROJECT_COPY is
     * faster than checking out the project for every test.
     * 
     * @throws RemoteException
     */

    @Before
    public void runBeforeEveryTest() throws Exception {
        List<Callable<Void>> initTasks = new ArrayList<Callable<Void>>();
        for (final AbstractTester tester : getCurrentTesters()) {
            initTasks.add(new Callable<Void>() {
                public Void call() throws Exception {
                    tester.remoteBot().resetWorkbench();
                    tester.superBot().views().packageExplorerView()
                        .selectProject(Constants.SVN_PROJECT_COPY).copy();
                    tester.superBot().views().packageExplorerView().tree()
                        .paste(Constants.SVN_PROJECT);
                    assertTrue(tester.superBot().views().packageExplorerView()
                        .tree().existsWithRegex(Constants.SVN_PROJECT));
                    assertTrue(tester.superBot().views().packageExplorerView()
                        .isProjectManagedBySVN(Constants.SVN_PROJECT));
                    assertTrue(tester.superBot().views().packageExplorerView()
                        .selectPkg("stf_test_project", "pkg")
                        .existsWithRegex("Test.java"));
                    return null;

                }
            });
        }
        // copyProject is not thread safe :-/
        int numberOfThreads = 1;
        Util.workAll(initTasks, numberOfThreads);

        Util.buildSessionSequentially(Constants.SVN_PROJECT,
            TypeOfCreateProject.EXIST_PROJECT, ALICE, BOB);
        ALICE.superBot().views().sarosView()
            .waitUntilIsInviteeInSession(BOB.superBot());
    }

    @After
    public void runAfterEveryTest() throws Exception {
        leaveSessionHostFirst(ALICE);
        if (BOB.superBot().views().packageExplorerView().tree()
            .existsWithRegex(Constants.SVN_PROJECT))
            BOB.superBot().views().packageExplorerView()
                .selectJavaProject(Constants.SVN_PROJECT).delete();

        if (ALICE.superBot().views().packageExplorerView().tree()
            .existsWithRegex(Constants.SVN_PROJECT))
            ALICE.superBot().views().packageExplorerView()
                .selectJavaProject(Constants.SVN_PROJECT).delete();
    }

    @AfterClass
    public static void runAfterClass() throws RemoteException {
        // if (TesterConfiguration.DEVELOPMODE) {
        // // don't delete SVN_PROJECT_COPY
        // ALICE.superBot().views().sarosView().disconnect();
        // BOB.superBot().views().sarosView().disconnect();
        // } else {
        resetSaros();
        // }
    }

    /**
     * Steps:
     * <ol>
     * <li>Alice grants Bob write access.</li>
     * <li>Bob renames the file {@link Constants#SVN_CLS1} to "Asdf".</li>
     * </ol>
     * Result:
     * <ol>
     * <li>The file {@link Constants#SVN_CLS1} in Alice's copy gets renamed to
     * "Asdf".</li>
     * </ol>
     * 
     * @throws RemoteException
     */
    @Test
    public void testGrantWriteAccessAndRenameClass() throws Exception {

        assertTrue(ALICE.superBot().views().sarosView()
            .selectParticipant(BOB.getJID()).hasWriteAccess());
        BOB.superBot()
            .views()
            .packageExplorerView()
            .selectClass(Constants.SVN_PROJECT, Constants.SVN_PKG,
                Constants.SVN_CLS1).refactor().rename("Asdf");

        ALICE
            .superBot()
            .views()
            .packageExplorerView()
            .waitUntilClassExists(Constants.SVN_PROJECT, Constants.SVN_PKG,
                "Asdf");
        assertTrue(ALICE.superBot().views().packageExplorerView()
            .selectPkg(Constants.SVN_PROJECT, Constants.SVN_PKG)
            .existsWithRegex("Asdf.java"));
    }

    /**
     * Steps:
     * <ol>
     * <li>Alice grants Bob write access.</li>
     * <li>Bob creates new package "new_package".</li>
     * <li>Bob moves the file {@link Constants#SVN_CLS1} to "new_package".</li>
     * </ol>
     * Result:
     * <ol>
     * <li>The package "new_package" gets created in Alice's copy.</li>
     * <li>The file {@link Constants#SVN_CLS1} in Alice's copy gets moved to
     * "new_package".</li>
     * </ol>
     * 
     * @throws RemoteException
     */
    @Test
    public void testGrantWriteAccessAndMoveClass() throws Exception {

        assertTrue(ALICE.superBot().views().sarosView()
            .selectParticipant(BOB.getJID()).hasWriteAccess());
        BOB.superBot().views().packageExplorerView().tree().newC()
            .pkg(Constants.SVN_PROJECT, "new_package");
        ALICE.superBot().views().packageExplorerView()
            .waitUntilPkgExists(Constants.SVN_PROJECT, "new_package");
        BOB.remoteBot().sleep(1000);
        BOB.superBot()
            .views()
            .packageExplorerView()
            .selectClass(Constants.SVN_PROJECT, Constants.SVN_PKG,
                Constants.SVN_CLS1).refactor()
            .moveClassTo(Constants.SVN_PROJECT, "new_package");

        ALICE
            .superBot()
            .views()
            .packageExplorerView()
            .waitUntilClassExists(Constants.SVN_PROJECT, "new_package",
                Constants.SVN_CLS1);
        assertTrue(ALICE.superBot().views().packageExplorerView()
            .selectPkg(Constants.SVN_PROJECT, "new_package")
            .existsWithRegex(Constants.SVN_CLS1 + ".java"));
    }

    /**
     * Steps:
     * <ol>
     * <li>Alice switches SVN_PROJECT to SVN_PROJECT_URL_SWITCHED.</li>
     * </ol>
     * Result:
     * <ol>
     * <li>Bob's copy of SVN_PROJECT gets switched to SVN_PROJECT_URL_SWITCHED.</li>
     * </ol>
     * 
     * @throws RemoteException
     */
    @Test
    public void testSwitch() throws Exception {
        ALICE
            .superBot()
            .views()
            .packageExplorerView()
            .tree()
            .team()
            .switchProject(Constants.SVN_PROJECT,
                Constants.SVN_PROJECT_URL_SWITCHED);
        ALICE
            .superBot()
            .views()
            .packageExplorerView()
            .waitUntilUrlIsSame(Constants.SVN_CLS1_FULL_PATH,
                Constants.SVN_CLS1_SWITCHED_URL);

        BOB.superBot().views().packageExplorerView()
            .waitUntilWindowSarosRunningVCSOperationClosed();
        BOB.superBot()
            .views()
            .packageExplorerView()
            .waitUntilUrlIsSame(Constants.SVN_CLS1_FULL_PATH,
                Constants.SVN_CLS1_SWITCHED_URL);

        assertEquals(Constants.SVN_CLS1_SWITCHED_URL,
            BOB.superBot().views().packageExplorerView()
                .getURLOfRemoteResource(Constants.SVN_CLS1_FULL_PATH));
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
        ALICE.superBot().views().packageExplorerView()
            .selectProject(Constants.SVN_PROJECT).team().disConnect();
        BOB.superBot().views().packageExplorerView()
            .waitUntilProjectNotInSVN(Constants.SVN_PROJECT);
        assertFalse(BOB.superBot().views().packageExplorerView()
            .isProjectManagedBySVN(Constants.SVN_PROJECT));
        ALICE.superBot().views().packageExplorerView()
            .selectProject(Constants.SVN_PROJECT).team()
            .shareProjectConfiguredWithSVNInfos(Constants.SVN_REPOSITORY_URL);
        BOB.superBot().views().packageExplorerView()
            .waitUntilWindowSarosRunningVCSOperationClosed();
        BOB.superBot().views().packageExplorerView()
            .waitUntilProjectInSVN(Constants.SVN_PROJECT);
        assertTrue(BOB.superBot().views().packageExplorerView()
            .isProjectManagedBySVN(Constants.SVN_PROJECT));
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
        ALICE.superBot().views().packageExplorerView()
            .selectProject(Constants.SVN_PROJECT).team().update("115");
        BOB.superBot().views().packageExplorerView()
            .waitUntilWindowSarosRunningVCSOperationClosed();
        assertTrue(ALICE
            .superBot()
            .views()
            .packageExplorerView()
            .getURLOfRemoteResource(Constants.SVN_PROJECT)
            .equals(
                BOB.superBot().views().packageExplorerView()
                    .getURLOfRemoteResource(Constants.SVN_PROJECT)));
        ALICE.superBot().views().packageExplorerView()
            .selectProject(Constants.SVN_PROJECT).team().update("116");
        BOB.superBot().views().packageExplorerView()
            .waitUntilWindowSarosRunningVCSOperationClosed();
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
        ALICE
            .superBot()
            .views()
            .packageExplorerView()
            .selectClass(Constants.SVN_PROJECT, Constants.SVN_PKG,
                Constants.SVN_CLS1).team().update("102");
        BOB.superBot().views().packageExplorerView()
            .waitUntilWindowSarosRunningVCSOperationClosed();

        assertTrue(ALICE.superBot().views().packageExplorerView()
            .getRevision(Constants.SVN_CLS1_FULL_PATH).equals("102"));
        BOB.superBot().views().packageExplorerView()
            .waitUntilRevisionIsSame(Constants.SVN_CLS1_FULL_PATH, "102");

        assertTrue(BOB.superBot().views().packageExplorerView()
            .getRevision(Constants.SVN_CLS1_FULL_PATH).equals("102"));

        BOB.superBot().views().packageExplorerView()
            .waitUntilRevisionIsSame(Constants.SVN_PROJECT, "116");

        assertTrue(BOB.superBot().views().packageExplorerView()
            .getRevision(Constants.SVN_PROJECT).equals("116"));
        ALICE
            .superBot()
            .views()
            .packageExplorerView()
            .selectClass(Constants.SVN_PROJECT, Constants.SVN_PKG,
                Constants.SVN_CLS1).team().update("116");

        BOB.superBot().views().packageExplorerView()
            .waitUntilWindowSarosRunningVCSOperationClosed();
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
        ALICE.superBot().views().packageExplorerView()
            .selectFile(Constants.SVN_CLS1_FULL_PATH).delete();
        BOB.superBot()
            .views()
            .packageExplorerView()
            .waitUntilClassNotExists(Constants.SVN_PROJECT, Constants.SVN_PKG,
                Constants.SVN_CLS1);
        assertFalse(BOB.superBot().views().packageExplorerView()
            .selectPkg("stf_test_project", "pkg").existsWithRegex("Test.java"));
        ALICE.superBot().views().packageExplorerView()
            .selectProject(Constants.SVN_PROJECT).team().revert();
        BOB.superBot()
            .views()
            .packageExplorerView()
            .waitUntilClassExists(Constants.SVN_PROJECT, Constants.SVN_PKG,
                Constants.SVN_CLS1);
        assertTrue(BOB.superBot().views().packageExplorerView()
            .selectPkg("stf_test_project", "pkg").existsWithRegex("Test.java"));
    }
}
