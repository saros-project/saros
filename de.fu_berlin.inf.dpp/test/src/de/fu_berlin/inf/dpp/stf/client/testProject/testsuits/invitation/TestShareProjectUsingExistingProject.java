package de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.invitation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.STFTest;

public class TestShareProjectUsingExistingProject extends STFTest {

    /**
     * Preconditions:
     * <ol>
     * <li>Alice (Host, Write Access)</li>
     * <li>Bob (Read-Only Access)</li>
     * </ol>
     * 
     * @throws RemoteException
     */

    @BeforeClass
    public static void runBeforeClass() throws RemoteException {
        initTesters(TypeOfTester.ALICE, TypeOfTester.BOB);
        setUpWorkbench();
        setUpSaros();
    }

    @Before
    public void runBeforeEveryTest() throws RemoteException {
        alice.sarosBot().views().packageExplorerView().tree().newC()
            .javaProjectWithClasses(PROJECT1, PKG1, CLS1);
        bob.sarosBot().views().packageExplorerView().tree().newC()
            .javaProjectWithClasses(PROJECT1, PKG1, CLS2);
    }

    @After
    public void runAfterEveryTest() throws RemoteException,
        InterruptedException {
        leaveSessionHostFirst(alice);
        deleteAllProjectsByActiveTesters();

    }

    @Test
    public void shareProjectUsingExistingProject() throws RemoteException {
        assertFalse(bob.sarosBot().views().packageExplorerView()
            .selectPkg(PROJECT1, PKG1).existsWithRegex(CLS1_SUFFIX));
        assertTrue(bob.sarosBot().views().packageExplorerView()
            .selectPkg(PROJECT1, PKG1).existsWithRegex(CLS2));
        buildSessionSequentially(PROJECT1, TypeOfCreateProject.EXIST_PROJECT,
            alice, bob);
        bob.sarosBot().condition().waitUntilClassExists(PROJECT1, PKG1, CLS1);
        assertTrue(bob.sarosBot().views().packageExplorerView()
            .selectPkg(PROJECT1, PKG1).existsWithRegex(CLS1));
        assertFalse(bob.sarosBot().views().packageExplorerView()
            .selectPkg(PROJECT1, PKG1).existsWithRegex(CLS2));
    }

    @Test
    public void shareProjectUsingExistProjectWithCopyAfterCancelLocalChange()
        throws RemoteException {
        assertFalse(bob.sarosBot().views().packageExplorerView()
            .selectPkg(PROJECT1, PKG1).existsWithRegex(CLS1_SUFFIX));
        assertTrue(bob.sarosBot().views().packageExplorerView()
            .selectPkg(PROJECT1, PKG1).existsWithRegex(CLS2));

        buildSessionSequentially(

            PROJECT1,
            TypeOfCreateProject.EXIST_PROJECT_WITH_COPY_AFTER_CANCEL_LOCAL_CHANGE,
            alice, bob);
        // assertTrue(bob.sarosC.isWIndowSessionInvitationActive());
        // bob.sarosC
        // .confirmProjectSharingWizardUsingExistProjectWithCopy(PROJECT1);

        assertTrue(bob.sarosBot().views().packageExplorerView().tree()
            .existsWithRegex(PROJECT1));
        assertTrue(bob.sarosBot().views().packageExplorerView()
            .selectPkg(PROJECT1, PKG1).existsWithRegex(CLS2));
        assertTrue(bob.sarosBot().views().packageExplorerView().tree()
            .existsWithRegex(PROJECT1_NEXT));
        assertTrue(bob.sarosBot().views().packageExplorerView()
            .selectPkg(PROJECT1_NEXT, PKG1).existsWithRegex(CLS1));
        bob.noBot().deleteProjectNoGUI(PROJECT1_NEXT);
    }

    @Test
    public void testShareProjectUsingExistingProjectWithCopy()
        throws RemoteException {
        buildSessionSequentially(PROJECT1,
            TypeOfCreateProject.EXIST_PROJECT_WITH_COPY, alice, bob);
        assertTrue(bob.sarosBot().views().packageExplorerView().tree()
            .existsWithRegex(PROJECT1));
        assertTrue(bob.sarosBot().views().packageExplorerView()
            .selectPkg(PROJECT1, PKG1).existsWithRegex(CLS2));
        assertTrue(bob.sarosBot().views().packageExplorerView().tree()
            .existsWithRegex(PROJECT1_NEXT));
        assertTrue(bob.sarosBot().views().packageExplorerView()
            .selectPkg(PROJECT1_NEXT, PKG1).existsWithRegex(CLS1));
        bob.noBot().deleteProjectNoGUI(PROJECT1_NEXT);

    }
}
