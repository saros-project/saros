package de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.invitation.permutations;

import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.STFTest;

public class TestStrictSequentialInvitationWithoutTermination extends STFTest {

    /**
     * Preconditions:
     * <ol>
     * <li>alice (Host, Write Access)</li>
     * <li>bob (Read-Only Access)</li>
     * <li>carl (Read-Only Access)</li>
     * </ol>
     * 
     * @throws RemoteException
     * @throws InterruptedException
     */

    @BeforeClass
    public static void runBeforeClass() throws RemoteException,
        InterruptedException {
        initTesters(TypeOfTester.ALICE, TypeOfTester.BOB, TypeOfTester.CARL);
        setUpWorkbench();
        setUpSaros();
        alice.sarosBot().file().newJavaProjectWithClasses(PROJECT1, PKG1, CLS1);
    }

    @After
    public void runAfterEveryTest() throws RemoteException {
        carl.sarosBot().window().setNewTextFileLineDelimiter("Default");
        bob.sarosBot().window().setNewTextFileLineDelimiter("Default");
        alice.sarosBot().window().setNewTextFileLineDelimiter("Default");
    }

    /**
     * 
     * TODO the test failed and need to be fixed at first. <br/>
     * 
     * Steps:
     * <ol>
     * <li>alice is either in global or project-specific settings and chooses
     * explicitly one of the possible line delimiter.</li>
     * <li>carl invites all strictly sequentially, one after the other.</li>
     * <li>All participants accept (use new project).</li>
     * <li>Once the invitation has been completed, all leave the session again.</li>
     * </ol>
     * 
     * Result:
     * <ol>
     * <li></li>
     * <li></li>
     * <li>All participants find that the line terminator of the host is set in
     * their project settings.</li>
     * <li></li>
     * </ol>
     * 
     * @throws RemoteException
     * @throws InterruptedException
     */
    @Test
    public void testSetLineDelimiter() throws RemoteException,
        InterruptedException {
        alice.sarosBot().window().setNewTextFileLineDelimiter("Unix");

        buildSessionSequentially(VIEW_PACKAGE_EXPLORER, PROJECT1,
            CM_SHARE_PROJECT, TypeOfCreateProject.NEW_PROJECT, carl, alice, bob);

        String delimiterByAlice = alice.sarosBot().window()
            .getTextFileLineDelimiter();
        String delimiterByCarl = carl.sarosBot().window()
            .getTextFileLineDelimiter();
        String delimiterByBob = bob.sarosBot().window()
            .getTextFileLineDelimiter();

        log.debug("delimiter by alice: " + delimiterByAlice
            + "delimiter by bob: " + delimiterByBob + "delimiter by carl: "
            + delimiterByCarl);

        /*
         * TODO there are some bugs in saros... need to be fixed. Now you will
         * get AssertionError here.
         */
        assertTrue(delimiterByAlice.equals(delimiterByCarl));
        assertTrue(delimiterByAlice.equals(delimiterByBob));

        leaveSessionPeersFirst();

    }
}
