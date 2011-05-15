package de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.fileFolderOperations;

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

import de.fu_berlin.inf.dpp.stf.client.testProject.helpers.MakeOperationConcurrently;
import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.STFTest;
import de.fu_berlin.inf.dpp.stf.client.tester.AbstractTester;
import de.fu_berlin.inf.dpp.stf.client.tester.TesterConfiguration;

public class TestSVNStateUpdates extends STFTest {
    /**
     * Preconditions:
     * <ol>
     * <li>Alice (Host, Write Access)</li>
     * <li>Bob (Read-Only Access)</li>
     * <li>Alice and Bob both have the project {@link STFTest#SVN_PROJECT_COPY},
     * which is checked out from SVN:<br>
     * repository: {@link STFTest#SVN_REPOSITORY_URL}<br>
     * path: {@link STFTest#SVN_PROJECT_PATH}
     * </ol>
     * 
     * @throws RemoteException
     */
    @BeforeClass
    public static void runBeforeClass() throws Exception {
        initTesters(TypeOfTester.ALICE, TypeOfTester.BOB);
        setUpWorkbench();
        setUpSaros();

        List<Callable<Void>> initTasks = new ArrayList<Callable<Void>>();
        for (final AbstractTester t : activeTesters) {
            initTasks.add(new Callable<Void>() {
                public Void call() throws Exception {
                    if (!t.superBot().views().packageExplorerView().tree()
                        .existsWithRegex(SVN_PROJECT_COPY)) {
                        t.superBot().views().packageExplorerView().tree()
                            .newC().javaProject(SVN_PROJECT_COPY);
                        t.superBot()
                            .views()
                            .packageExplorerView()
                            .selectProject(SVN_PROJECT_COPY)
                            .team()
                            .shareProjectUsingSpecifiedFolderName(
                                SVN_REPOSITORY_URL, SVN_PROJECT_PATH);

                    }
                    return null;
                }
            });
        }
        MakeOperationConcurrently.workAll(initTasks);

    }

    /**
     * Preconditions:
     * <ol>
     * <li>Alice and Bob copied {@link STFTest#SVN_PROJECT_COPY} to
     * {@link STFTest#SVN_PROJECT}.</li>
     * </ol>
     * Only SVN_PROJECT is used in the tests. Copying from SVN_PROJECT_COPY is
     * faster than checking out the project for every test.
     * 
     * @throws RemoteException
     */

    @Before
    public void runBeforeEveryTest() throws Exception {
        List<Callable<Void>> initTasks = new ArrayList<Callable<Void>>();
        for (final AbstractTester tester : activeTesters) {
            initTasks.add(new Callable<Void>() {
                public Void call() throws Exception {
                    tester.remoteBot().resetWorkbench();
                    tester.superBot().views().packageExplorerView()
                        .selectProject(SVN_PROJECT_COPY).copy();
                    tester.superBot().views().packageExplorerView().tree()
                        .paste(SVN_PROJECT);
                    assertTrue(tester.superBot().views().packageExplorerView()
                        .tree().existsWithRegex(SVN_PROJECT));
                    assertTrue(tester.superBot().views().packageExplorerView()
                        .isProjectManagedBySVN(SVN_PROJECT));
                    assertTrue(tester.superBot().views().packageExplorerView()
                        .selectPkg("stf_test_project", "pkg")
                        .existsWithRegex("Test.java"));
                    return null;

                }
            });
        }
        // copyProject is not thread safe :-/
        int numberOfThreads = 1;
        MakeOperationConcurrently.workAll(initTasks, numberOfThreads);

        buildSessionSequentially(SVN_PROJECT,
            TypeOfCreateProject.EXIST_PROJECT, alice, bob);
        alice.superBot().views().sarosView()
            .waitUntilIsInviteeInSession(bob.superBot());
    }

    @After
    public void runAfterEveryTest() throws Exception {
        leaveSessionHostFirst(alice);
        if (bob.superBot().views().packageExplorerView().tree()
            .existsWithRegex(SVN_PROJECT))
            bob.superBot().views().packageExplorerView()
                .selectJavaProject(SVN_PROJECT).delete();

        if (alice.superBot().views().packageExplorerView().tree()
            .existsWithRegex(SVN_PROJECT))
            alice.superBot().views().packageExplorerView()
                .selectJavaProject(SVN_PROJECT).delete();
    }

    @AfterClass
    public static void runAfterClass() throws RemoteException {
        if (TesterConfiguration.DEVELOPMODE) {
            // don't delete SVN_PROJECT_COPY
            alice.superBot().views().sarosView().disconnect();
            bob.superBot().views().sarosView().disconnect();
        } else {
            resetSaros();
        }
    }

    /**
     * Steps:
     * <ol>
     * <li>Alice grants Bob write access.</li>
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
    public void testGrantWriteAccessAndRenameClass() throws Exception {

        assertTrue(alice.superBot().views().sarosView()
            .selectParticipant(bob.getJID()).hasWriteAccess());
        bob.superBot().views().packageExplorerView()
            .selectClass(SVN_PROJECT, SVN_PKG, SVN_CLS1).refactor()
            .rename("Asdf");

        alice.superBot().views().packageExplorerView()
            .waitUntilClassExists(SVN_PROJECT, SVN_PKG, "Asdf");
        assertTrue(alice.superBot().views().packageExplorerView()
            .selectPkg(SVN_PROJECT, SVN_PKG)
            .existsWithRegex("Asdf" + SUFFIX_JAVA));
    }

    /**
     * Steps:
     * <ol>
     * <li>Alice grants Bob write access.</li>
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
    public void testGrantWriteAccessAndMoveClass() throws Exception {

        assertTrue(alice.superBot().views().sarosView()
            .selectParticipant(bob.getJID()).hasWriteAccess());
        bob.superBot().views().packageExplorerView().tree().newC()
            .pkg(SVN_PROJECT, "new_package");
        alice.superBot().views().packageExplorerView()
            .waitUntilPkgExists(SVN_PROJECT, "new_package");
        bob.remoteBot().sleep(1000);
        bob.superBot().views().packageExplorerView()
            .selectClass(SVN_PROJECT, SVN_PKG, SVN_CLS1).refactor()
            .moveClassTo(SVN_PROJECT, "new_package");

        alice.superBot().views().packageExplorerView()
            .waitUntilClassExists(SVN_PROJECT, "new_package", SVN_CLS1);
        assertTrue(alice.superBot().views().packageExplorerView()
            .selectPkg(SVN_PROJECT, "new_package")
            .existsWithRegex(SVN_CLS1 + SUFFIX_JAVA));
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
        alice.superBot().views().packageExplorerView().tree().team()
            .switchProject(SVN_PROJECT, SVN_PROJECT_URL_SWITCHED);
        alice.superBot().views().packageExplorerView()
            .waitUntilUrlIsSame(SVN_CLS1_FULL_PATH, SVN_CLS1_SWITCHED_URL);

        bob.superBot().views().packageExplorerView()
            .waitUntilWindowSarosRunningVCSOperationClosed();
        bob.superBot().views().packageExplorerView()
            .waitUntilUrlIsSame(SVN_CLS1_FULL_PATH, SVN_CLS1_SWITCHED_URL);

        assertEquals(SVN_CLS1_SWITCHED_URL, bob.superBot().views()
            .packageExplorerView().getURLOfRemoteResource(SVN_CLS1_FULL_PATH));
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
        alice.superBot().views().packageExplorerView()
            .selectProject(SVN_PROJECT).team().disConnect();
        bob.superBot().views().packageExplorerView()
            .waitUntilProjectNotInSVN(SVN_PROJECT);
        assertFalse(bob.superBot().views().packageExplorerView()
            .isProjectManagedBySVN(SVN_PROJECT));
        alice.superBot().views().packageExplorerView()
            .selectProject(SVN_PROJECT).team()
            .shareProjectConfiguredWithSVNInfos(STFTest.SVN_REPOSITORY_URL);
        bob.superBot().views().packageExplorerView()
            .waitUntilWindowSarosRunningVCSOperationClosed();
        bob.superBot().views().packageExplorerView()
            .waitUntilProjectInSVN(SVN_PROJECT);
        assertTrue(bob.superBot().views().packageExplorerView()
            .isProjectManagedBySVN(SVN_PROJECT));
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
        alice.superBot().views().packageExplorerView()
            .selectProject(SVN_PROJECT).team().update("115");
        bob.superBot().views().packageExplorerView()
            .waitUntilWindowSarosRunningVCSOperationClosed();
        assertTrue(alice
            .superBot()
            .views()
            .packageExplorerView()
            .getURLOfRemoteResource(SVN_PROJECT)
            .equals(
                bob.superBot().views().packageExplorerView()
                    .getURLOfRemoteResource(SVN_PROJECT)));
        alice.superBot().views().packageExplorerView()
            .selectProject(SVN_PROJECT).team().update("116");
        bob.superBot().views().packageExplorerView()
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
        alice.superBot().views().packageExplorerView()
            .selectClass(SVN_PROJECT, SVN_PKG, SVN_CLS1).team().update("102");
        bob.superBot().views().packageExplorerView()
            .waitUntilWindowSarosRunningVCSOperationClosed();

        assertTrue(alice.superBot().views().packageExplorerView()
            .getRevision(STFTest.SVN_CLS1_FULL_PATH).equals("102"));
        bob.superBot().views().packageExplorerView()
            .waitUntilRevisionIsSame(STFTest.SVN_CLS1_FULL_PATH, "102");

        assertTrue(bob.superBot().views().packageExplorerView()
            .getRevision(STFTest.SVN_CLS1_FULL_PATH).equals("102"));

        bob.superBot().views().packageExplorerView()
            .waitUntilRevisionIsSame(SVN_PROJECT, "116");

        assertTrue(bob.superBot().views().packageExplorerView()
            .getRevision(SVN_PROJECT).equals("116"));
        alice.superBot().views().packageExplorerView()
            .selectClass(SVN_PROJECT, SVN_PKG, SVN_CLS1).team().update("116");

        bob.superBot().views().packageExplorerView()
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
        alice.superBot().views().packageExplorerView()
            .selectFile(SVN_CLS1_FULL_PATH).delete();
        bob.superBot().views().packageExplorerView()
            .waitUntilClassNotExists(SVN_PROJECT, SVN_PKG, SVN_CLS1);
        assertFalse(bob.superBot().views().packageExplorerView()
            .selectPkg("stf_test_project", "pkg").existsWithRegex("Test.java"));
        alice.superBot().views().packageExplorerView()
            .selectProject(SVN_PROJECT).team().revert();
        bob.superBot().views().packageExplorerView()
            .waitUntilClassExists(SVN_PROJECT, SVN_PKG, SVN_CLS1);
        assertTrue(bob.superBot().views().packageExplorerView()
            .selectPkg("stf_test_project", "pkg").existsWithRegex("Test.java"));
    }
}
