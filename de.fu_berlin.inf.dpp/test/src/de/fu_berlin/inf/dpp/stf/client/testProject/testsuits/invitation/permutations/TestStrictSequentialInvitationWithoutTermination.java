package de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.invitation.permutations;

import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.STFTest;

public class TestStrictSequentialInvitationWithoutTermination extends STFTest {
    private static final Logger log = Logger
        .getLogger(TestStrictSequentialInvitationWithoutTermination.class);

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
        setUpWorkbenchs();
        setUpSaros();
        alice.fileM.newJavaProjectWithClass(PROJECT1, PKG1, CLS1);
    }

    @AfterClass
    public static void runAfterClass() {
        //
    }

    @Before
    public void runBeforeEveryTest() {
        //
    }

    @After
    public void runAfterEveryTest() throws RemoteException {
        carl.windowM.setNewTextFileLineDelimiter("Default");
        bob.windowM.setNewTextFileLineDelimiter("Default");
        alice.windowM.setNewTextFileLineDelimiter("Default");
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
        alice.windowM.setNewTextFileLineDelimiter("Unix");

        alice.buildSessionDoneSequentially(PROJECT1,
            TypeOfShareProject.SHARE_PROJECT, TypeOfCreateProject.NEW_PROJECT,
            carl, bob);

        String delimiterByAlice = alice.windowM.getTextFileLineDelimiter();
        String delimiterByCarl = carl.windowM.getTextFileLineDelimiter();
        String delimiterByBob = bob.windowM.getTextFileLineDelimiter();

        log.debug("delimiter by alice: " + delimiterByAlice
            + "delimiter by bob: " + delimiterByBob + "delimiter by carl: "
            + delimiterByCarl);

        /*
         * TODO there are some bugs in saros... need to be fixed. Now you will
         * get AssertionError here.
         */
        assertTrue(delimiterByAlice.equals(delimiterByCarl));
        assertTrue(delimiterByAlice.equals(delimiterByBob));

        alice.leaveSessionPeersFirstDone(carl, bob);

    }
}
