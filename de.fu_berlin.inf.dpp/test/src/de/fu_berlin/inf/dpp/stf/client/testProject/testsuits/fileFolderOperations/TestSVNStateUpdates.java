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
     * <li>Alice (Host, Driver)</li>
     * <li>Bob (Observer)</li>
     * <li>Alice and Bob both have the project {@link STFTest#SVN_PROJECT_COPY},
     * which is checked out from SVN:<br>
     * repository: {@link STFTest#SVN_REPOSITORY_URL}<br>
     * path: {@link STFTest#SVN_PROJECT_PATH}
     * </ol>
     * 
     * @throws RemoteException
     */
    @BeforeClass
    public static void beforeClass() throws Exception {
        initTesters(TypeOfTester.ALICE, TypeOfTester.BOB);
        setUpWorkbenchs();
        setUpSaros();

        List<Callable<Void>> initTasks = new ArrayList<Callable<Void>>();
        for (final Tester musician : activeTesters) {
            initTasks.add(new Callable<Void>() {
                public Void call() throws Exception {
                    if (!musician.file.existsProject(SVN_PROJECT_COPY)) {
                        musician.file.newJavaProject(SVN_PROJECT_COPY);
                        musician.pEV
                            .shareProjectWithSVNUsingSpecifiedFolderName(
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
    @Override
    @Before
    public void before() throws Exception {
        super.before();
        List<Callable<Void>> initTasks = new ArrayList<Callable<Void>>();
        for (final Tester musician : activeTesters) {
            initTasks.add(new Callable<Void>() {
                public Void call() throws Exception {
                    musician.pEV.copyProject(SVN_PROJECT, SVN_PROJECT_COPY);
                    assertTrue(musician.file.existsProject(SVN_PROJECT));
                    assertTrue(musician.pEV.isProjectManagedBySVN(SVN_PROJECT));
                    assertTrue(musician.file.existsFile(SVN_CLS1_FULL_PATH));
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
        alice.sessionV.waitUntilSessionOpenBy(bob.sessionV);
    }

    @Override
    @After
    public void after() throws Exception {
        super.after();
        alice.leaveSessionHostFirstDone(bob);
        if (bob.file.existsProject(SVN_PROJECT))
            bob.edit.deleteProject(SVN_PROJECT);

        if (alice.file.existsProject(SVN_PROJECT))
            alice.edit.deleteProject(SVN_PROJECT);
    }

    @AfterClass
    public static void afterClass() throws RemoteException {
        if (ConfigTester.DEVELOPMODE) {
            // don't delete SVN_PROJECT_COPY
            alice.rosterV.disconnectGUI();
            bob.rosterV.disconnectGUI();
        } else {
            alice.workbench.resetSaros();
            bob.workbench.resetSaros();
        }
    }

    @Test
    public void asdf() {
        //
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
        alice.sessionV.giveExclusiveDriverRoleGUI(bob.sessionV);
        assertTrue(bob.sessionV.isDriver());
        bob.refactor.renameClass("Asdf", SVN_PROJECT, SVN_PKG, SVN_CLS1);

        alice.file.waitUntilClassExisted(SVN_PROJECT, SVN_PKG, "Asdf");
        assertTrue(alice.file.existsClass(SVN_PROJECT, SVN_PKG, "Asdf"));
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
        alice.sessionV.giveExclusiveDriverRoleGUI(bob.sessionV);
        assertTrue(bob.sessionV.isExclusiveDriver());

        bob.file.newPackage(SVN_PROJECT, "new_package");
        alice.file.waitUntilPkgExisted(SVN_PROJECT, "new_package");

        bob.refactor.moveClassTo(SVN_PROJECT, SVN_PKG, SVN_CLS1, SVN_PROJECT,
            "new_package");

        alice.file.waitUntilClassExisted(SVN_PROJECT, "new_package", SVN_CLS1);
        assertTrue(alice.file.existsClass(SVN_PROJECT, "new_package", SVN_CLS1));
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
        alice.pEV.switchProject(SVN_PROJECT, SVN_PROJECT_URL_SWITCHED);
        alice.pEV.waitUntilUrlIsSame(SVN_CLS1_FULL_PATH, SVN_CLS1_SWITCHED_URL);

        bob.pEV.waitUntilWindowSarosRunningVCSOperationClosed();
        bob.pEV.waitUntilUrlIsSame(SVN_CLS1_FULL_PATH, SVN_CLS1_SWITCHED_URL);

        assertEquals(SVN_CLS1_SWITCHED_URL,
            bob.pEV.getURLOfRemoteResource(SVN_CLS1_FULL_PATH));
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
            STFTest.SVN_REPOSITORY_URL);
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
        assertTrue(alice.pEV.getRevision(STFTest.SVN_CLS1_FULL_PATH).equals(
            "102"));
        bob.pEV.waitUntilRevisionIsSame(STFTest.SVN_CLS1_FULL_PATH, "102");
        assertTrue(bob.pEV.getRevision(STFTest.SVN_CLS1_FULL_PATH)
            .equals("102"));
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
        alice.edit.deleteProject(STFTest.SVN_CLS1_FULL_PATH);
        bob.file.waitUntilClassNotExist(SVN_PROJECT, SVN_PKG, SVN_CLS1);
        assertFalse(bob.file.existsFile(STFTest.SVN_CLS1_FULL_PATH));
        alice.pEV.revertProject(SVN_PROJECT);
        bob.file.waitUntilClassExisted(SVN_PROJECT, SVN_PKG, SVN_CLS1);
        assertTrue(bob.file.existsFile(STFTest.SVN_CLS1_FULL_PATH));
    }

}
