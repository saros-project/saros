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

import de.fu_berlin.inf.dpp.stf.client.ConfigTester;
import de.fu_berlin.inf.dpp.stf.client.Tester;
import de.fu_berlin.inf.dpp.stf.client.testProject.helpers.MakeOperationConcurrently;
import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.STFTest;

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
        for (final Tester t : activeTesters) {
            initTasks.add(new Callable<Void>() {
                public Void call() throws Exception {
                    if (!t.sarosBot().state()
                        .existsProjectNoGUI(SVN_PROJECT_COPY)) {
                        t.sarosBot().file().newJavaProject(SVN_PROJECT_COPY);
                        t.sarosBot()
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
        for (final Tester tester : activeTesters) {
            initTasks.add(new Callable<Void>() {
                public Void call() throws Exception {
                    tester.bot().resetWorkbench();
                    tester.sarosBot().packageExplorerView()
                        .selectProject(SVN_PROJECT_COPY).copy();
                    tester.sarosBot().packageExplorerView().tree()
                        .paste(SVN_PROJECT);
                    assertTrue(tester.sarosBot().state()
                        .existsProjectNoGUI(SVN_PROJECT));
                    assertTrue(tester.sarosBot().state()
                        .isProjectManagedBySVN(SVN_PROJECT));
                    assertTrue(tester.sarosBot().state()
                        .existsFileNoGUI(SVN_CLS1_FULL_PATH));
                    return null;
                }
            });
        }
        // copyProject is not thread safe :-/
        int numberOfThreads = 1;
        MakeOperationConcurrently.workAll(initTasks, numberOfThreads);

        buildSessionSequentially(SVN_PROJECT, CM_SHARE_PROJECT,
            TypeOfCreateProject.EXIST_PROJECT, alice, bob);
        alice.sarosBot().condition()
            .waitUntilIsInviteeInSession(bob.sarosBot());
    }

    @After
    public void runAfterEveryTest() throws Exception {
        leaveSessionHostFirst();
        if (bob.sarosBot().state().existsProjectNoGUI(SVN_PROJECT))
            bob.noBot().deleteProjectNoGUI(SVN_PROJECT);

        if (alice.sarosBot().state().existsProjectNoGUI(SVN_PROJECT))
            alice.noBot().deleteProjectNoGUI(SVN_PROJECT);
    }

    @AfterClass
    public static void runAfterClass() throws RemoteException {
        if (ConfigTester.DEVELOPMODE) {
            // don't delete SVN_PROJECT_COPY
            alice.sarosBot().buddiesView().disconnect();
            bob.sarosBot().buddiesView().disconnect();
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

        assertTrue(bob.sarosBot().state().hasWriteAccessNoGUI());
        bob.sarosBot().packageExplorerView()
            .selectClass(SVN_PROJECT, SVN_PKG, SVN_CLS1).refactor()
            .rename("Asdf");

        alice.sarosBot().condition()
            .waitUntilClassExists(SVN_PROJECT, SVN_PKG, "Asdf");
        assertTrue(alice.sarosBot().state()
            .existsClassNoGUI(SVN_PROJECT, SVN_PKG, "Asdf"));
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

        assertTrue(bob.sarosBot().state().hasWriteAccessNoGUI());
        bob.sarosBot().file().newPackage(SVN_PROJECT, "new_package");
        alice.sarosBot().condition()
            .waitUntilPkgExists(SVN_PROJECT, "new_package");
        bob.bot().sleep(1000);
        bob.sarosBot().packageExplorerView()
            .selectClass(SVN_PROJECT, SVN_PKG, SVN_CLS1).refactor()
            .moveClassTo(SVN_PROJECT, "new_package");

        alice.sarosBot().condition()
            .waitUntilClassExists(SVN_PROJECT, "new_package", SVN_CLS1);
        assertTrue(alice.sarosBot().state()
            .existsClassNoGUI(SVN_PROJECT, "new_package", SVN_CLS1));
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
        alice.noBot().switchProject(SVN_PROJECT, SVN_PROJECT_URL_SWITCHED);
        alice.sarosBot().condition()
            .waitUntilUrlIsSame(SVN_CLS1_FULL_PATH, SVN_CLS1_SWITCHED_URL);

        bob.sarosBot().condition()
            .waitUntilWindowSarosRunningVCSOperationClosed();
        bob.sarosBot().condition()
            .waitUntilUrlIsSame(SVN_CLS1_FULL_PATH, SVN_CLS1_SWITCHED_URL);

        assertEquals(SVN_CLS1_SWITCHED_URL, bob.sarosBot().state()
            .getURLOfRemoteResource(SVN_CLS1_FULL_PATH));
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
        alice.sarosBot().packageExplorerView().selectProject(SVN_PROJECT)
            .team().disConnect();
        bob.sarosBot().condition().waitUntilProjectNotInSVN(SVN_PROJECT);
        assertFalse(bob.sarosBot().state().isProjectManagedBySVN(SVN_PROJECT));
        alice.sarosBot().packageExplorerView().selectProject(SVN_PROJECT)
            .team()
            .shareProjectConfiguredWithSVNInfos(STFTest.SVN_REPOSITORY_URL);
        bob.sarosBot().condition()
            .waitUntilWindowSarosRunningVCSOperationClosed();
        bob.sarosBot().condition().waitUntilProjectInSVN(SVN_PROJECT);
        assertTrue(bob.sarosBot().state().isProjectManagedBySVN(SVN_PROJECT));
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
        alice.sarosBot().packageExplorerView().selectProject(SVN_PROJECT)
            .team().update("115");
        bob.sarosBot().condition()
            .waitUntilWindowSarosRunningVCSOperationClosed();
        assertTrue(alice.sarosBot().state().getURLOfRemoteResource(SVN_PROJECT)
            .equals(bob.sarosBot().state().getURLOfRemoteResource(SVN_PROJECT)));
        alice.sarosBot().packageExplorerView().selectProject(SVN_PROJECT)
            .team().update("116");
        bob.sarosBot().condition()
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
        alice.sarosBot().packageExplorerView()
            .selectClass(SVN_PROJECT, SVN_PKG, SVN_CLS1).team().update("102");
        bob.sarosBot().condition()
            .waitUntilWindowSarosRunningVCSOperationClosed();

        assertTrue(alice.sarosBot().state()
            .getRevision(STFTest.SVN_CLS1_FULL_PATH).equals("102"));
        bob.sarosBot().condition()
            .waitUntilRevisionIsSame(STFTest.SVN_CLS1_FULL_PATH, "102");

        assertTrue(bob.sarosBot().state()
            .getRevision(STFTest.SVN_CLS1_FULL_PATH).equals("102"));

        bob.sarosBot().condition().waitUntilRevisionIsSame(SVN_PROJECT, "116");

        assertTrue(bob.sarosBot().state().getRevision(SVN_PROJECT)
            .equals("116"));
        alice.sarosBot().packageExplorerView()
            .selectClass(SVN_PROJECT, SVN_PKG, SVN_CLS1).team().update("116");

        bob.sarosBot().condition()
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
        alice.noBot().deleteProjectNoGUI(STFTest.SVN_CLS1_FULL_PATH);
        bob.sarosBot().condition()
            .waitUntilClassNotExists(SVN_PROJECT, SVN_PKG, SVN_CLS1);
        assertFalse(bob.sarosBot().state()
            .existsFileNoGUI(STFTest.SVN_CLS1_FULL_PATH));
        alice.sarosBot().packageExplorerView().selectProject(SVN_PROJECT)
            .team().revert();
        bob.sarosBot().condition()
            .waitUntilClassExists(SVN_PROJECT, SVN_PKG, SVN_CLS1);
        assertTrue(bob.sarosBot().state()
            .existsFileNoGUI(STFTest.SVN_CLS1_FULL_PATH));
    }

}
