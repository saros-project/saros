package de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.invitation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.STFTest;

public class TestShare2UsersSequentially extends STFTest {

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

    /**
     * Steps:
     * <ol>
     * <li>Alice share project with bob.</li>
     * <li>Alice and bob leave the session.</li>
     * </ol>
     * 
     * Result:
     * <ol>
     * <li>Alice and Bob are participants and have both
     * {@link User.Permission#WRITE_ACCESS}.</li>
     * <li>Alice and bob have no {@link User.Permission}s after leaving the
     * session.</li>
     * </ol>
     * 
     * @throws InterruptedException
     */
    @Test
    public void aliceShareProjectWithBobSequentially() throws RemoteException,
        InterruptedException {
        alice.sarosBot().file().newJavaProjectWithClasses(PROJECT1, PKG1, CLS1);
        buildSessionSequentially(VIEW_PACKAGE_EXPLORER, PROJECT1,
            TypeOfShareProject.SHARE_PROJECT, TypeOfCreateProject.NEW_PROJECT,
            alice, bob);
        bob.bot()
            .captureScreenshot(
                (bob.bot().getPathToScreenShot() + "/invitee_in_sharedproject.png"));
        alice
            .bot()
            .captureScreenshot(
                (alice.bot().getPathToScreenShot() + "/inviter_in_sharedproject.png"));

        assertTrue(bob.sarosBot().sessionView().isParticipantNoGUI());
        assertTrue(alice.sarosBot().sessionView().isParticipantNoGUI());

        assertFalse(bob.sarosBot().sessionView().hasReadOnlyAccessNoGUI());
        assertFalse(alice.sarosBot().sessionView().hasReadOnlyAccessNoGUI());

        assertTrue(alice.sarosBot().sessionView().hasWriteAccessNoGUI());
        assertTrue(bob.sarosBot().sessionView().hasWriteAccessNoGUI());

        leaveSessionPeersFirst();

        assertFalse(bob.sarosBot().sessionView().isParticipantNoGUI());
        assertFalse(alice.sarosBot().sessionView().isParticipantNoGUI());

        assertFalse(bob.sarosBot().sessionView().hasReadOnlyAccessNoGUI());
        assertFalse(alice.sarosBot().sessionView().hasReadOnlyAccessNoGUI());

        assertFalse(alice.sarosBot().sessionView().hasWriteAccessNoGUI());
        assertFalse(bob.sarosBot().sessionView().hasWriteAccessNoGUI());

    }
}
