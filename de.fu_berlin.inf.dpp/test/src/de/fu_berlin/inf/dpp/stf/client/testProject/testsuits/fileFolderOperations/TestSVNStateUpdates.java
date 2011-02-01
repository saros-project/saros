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

import de.fu_berlin.inf.dpp.stf.client.Tester;
import de.fu_berlin.inf.dpp.stf.client.testProject.helpers.MakeOperationConcurrently;
import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.ConfigTester;
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
        setUpWorkbenchs();
        setUpSaros();

        List<Callable<Void>> initTasks = new ArrayList<Callable<Void>>();
        for (final Tester t : activeTesters) {
            initTasks.add(new Callable<Void>() {
                public Void call() throws Exception {
                    if (!t.fileM.existsProject(SVN_PROJECT_COPY)) {
                        t.fileM.newJavaProject(SVN_PROJECT_COPY);
                        t.team.shareProjectWithSVNUsingSpecifiedFolderName(
                            SVN_PROJECT_COPY, SVN_REPOSITORY_URL,
                            SVN_PROJECT_PATH);

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
                    tester.workbench.resetWorkbench();
                    tester.pEV.selectProject(SVN_PROJECT_COPY);
                    tester.editM.copyProject(SVN_PROJECT);
                    assertTrue(tester.fileM.existsProject(SVN_PROJECT));
                    assertTrue(tester.team.isProjectManagedBySVN(SVN_PROJECT));
                    assertTrue(tester.fileM.existsFile(SVN_CLS1_FULL_PATH));
                    return null;
                }
            });
        }
        // copyProject is not thread safe :-/
        int numberOfThreads = 1;
        MakeOperationConcurrently.workAll(initTasks, numberOfThreads);

        alice.buildSessionDoneSequentially(SVN_PROJECT,
            TypeOfShareProject.SHARE_PROJECT,
            TypeOfCreateProject.EXIST_PROJECT, bob);
        alice.sarosSessionV.waitUntilInviteeIsInSession(bob.sarosSessionV);
    }

    @After
    public void runAfterEveryTest() throws Exception {
        alice.leaveSessionHostFirstDone(bob);
        if (bob.fileM.existsProject(SVN_PROJECT))
            bob.editM.deleteProjectNoGUI(SVN_PROJECT);

        if (alice.fileM.existsProject(SVN_PROJECT))
            alice.editM.deleteProjectNoGUI(SVN_PROJECT);
    }

    @AfterClass
    public static void runAfterClass() throws RemoteException {
        if (ConfigTester.DEVELOPMODE) {
            // don't delete SVN_PROJECT_COPY
            alice.sarosBuddiesV.disconnectGUI();
            bob.sarosBuddiesV.disconnectGUI();
        } else {
            alice.workbench.resetSaros();
            bob.workbench.resetSaros();
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

        assertTrue(bob.sarosSessionV.hasWriteAccess());
        bob.pEV.selectClass(SVN_PROJECT, SVN_PKG, SVN_CLS1);
        bob.refactorM.renameClass("Asdf");
        alice.fileM.waitUntilClassExisted(SVN_PROJECT, SVN_PKG, "Asdf");
        assertTrue(alice.fileM.existsClass(SVN_PROJECT, SVN_PKG, "Asdf"));
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

        assertTrue(bob.sarosSessionV.hasWriteAccess());
        bob.fileM.newPackage(SVN_PROJECT, "new_package");
        alice.fileM.waitUntilPkgExisted(SVN_PROJECT, "new_package");
        bob.workbench.sleep(1000);
        bob.pEV.selectClass(SVN_PROJECT, SVN_PKG, SVN_CLS1);

        bob.refactorM.moveClassTo(SVN_PROJECT, "new_package");
        alice.fileM.waitUntilClassExisted(SVN_PROJECT, "new_package", SVN_CLS1);
        assertTrue(alice.fileM
            .existsClass(SVN_PROJECT, "new_package", SVN_CLS1));
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
        alice.team.switchProject(SVN_PROJECT, SVN_PROJECT_URL_SWITCHED);
        alice.team
            .waitUntilUrlIsSame(SVN_CLS1_FULL_PATH, SVN_CLS1_SWITCHED_URL);

        bob.team.waitUntilWindowSarosRunningVCSOperationClosed();
        bob.team.waitUntilUrlIsSame(SVN_CLS1_FULL_PATH, SVN_CLS1_SWITCHED_URL);

        assertEquals(SVN_CLS1_SWITCHED_URL,
            bob.team.getURLOfRemoteResource(SVN_CLS1_FULL_PATH));
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
        alice.team.disConnect(SVN_PROJECT);
        bob.team.waitUntilProjectNotInSVN(SVN_PROJECT);
        assertFalse(bob.team.isProjectManagedBySVN(SVN_PROJECT));
        alice.team.shareProjectWithSVNWhichIsConfiguredWithSVNInfos(
            SVN_PROJECT, STFTest.SVN_REPOSITORY_URL);
        bob.team.waitUntilWindowSarosRunningVCSOperationClosed();
        bob.team.waitUntilProjectInSVN(SVN_PROJECT);
        assertTrue(bob.team.isProjectManagedBySVN(SVN_PROJECT));
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
        alice.team.updateProject(SVN_PROJECT, "115");
        bob.team.waitUntilWindowSarosRunningVCSOperationClosed();
        assertTrue(alice.team.getURLOfRemoteResource(SVN_PROJECT).equals(
            bob.team.getURLOfRemoteResource(SVN_PROJECT)));
        alice.team.updateProject(SVN_PROJECT, "116");
        bob.team.waitUntilWindowSarosRunningVCSOperationClosed();
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
        alice.team.updateClass(SVN_PROJECT, SVN_PKG, SVN_CLS1, "102");
        bob.team.waitUntilWindowSarosRunningVCSOperationClosed();
        assertTrue(alice.team.getRevision(STFTest.SVN_CLS1_FULL_PATH).equals(
            "102"));
        bob.team.waitUntilRevisionIsSame(STFTest.SVN_CLS1_FULL_PATH, "102");
        assertTrue(bob.team.getRevision(STFTest.SVN_CLS1_FULL_PATH).equals(
            "102"));
        bob.team.waitUntilRevisionIsSame(SVN_PROJECT, "116");
        assertTrue(bob.team.getRevision(SVN_PROJECT).equals("116"));
        alice.team.updateClass(SVN_PROJECT, SVN_PKG, SVN_CLS1, "116");
        bob.team.waitUntilWindowSarosRunningVCSOperationClosed();
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
        alice.editM.deleteProjectNoGUI(STFTest.SVN_CLS1_FULL_PATH);
        bob.fileM.waitUntilClassNotExist(SVN_PROJECT, SVN_PKG, SVN_CLS1);
        assertFalse(bob.fileM.existsFile(STFTest.SVN_CLS1_FULL_PATH));
        alice.team.revertProject(SVN_PROJECT);
        bob.fileM.waitUntilClassExisted(SVN_PROJECT, SVN_PKG, SVN_CLS1);
        assertTrue(bob.fileM.existsFile(STFTest.SVN_CLS1_FULL_PATH));
    }

}
