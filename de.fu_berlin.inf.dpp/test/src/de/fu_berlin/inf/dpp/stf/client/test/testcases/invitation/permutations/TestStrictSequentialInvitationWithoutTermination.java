package de.fu_berlin.inf.dpp.stf.client.test.testcases.invitation.permutations;

import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.Musician;
import de.fu_berlin.inf.dpp.stf.client.MusicianConfigurationInfos;
import de.fu_berlin.inf.dpp.stf.client.test.helpers.InitMusician;
import de.fu_berlin.inf.dpp.stf.client.test.helpers.STFTest;

public class TestStrictSequentialInvitationWithoutTermination extends STFTest {
    private static final Logger log = Logger
        .getLogger(TestStrictSequentialInvitationWithoutTermination.class);

    /**
     * Preconditions:
     * <ol>
     * <li>alice (Host, Driver)</li>
     * <li>bob (Observer)</li>
     * <li>carl (Observer)</li>
     * </ol>
     * 
     * @throws RemoteException
     * @throws InterruptedException
     */
    @BeforeClass
    public static void initMusicians() throws RemoteException,
        InterruptedException {
        /*
         * initialize the musicians simultaneously
         */
        List<Musician> musicians = InitMusician.initMusiciansConcurrently(
            MusicianConfigurationInfos.PORT_ALICE,
            MusicianConfigurationInfos.PORT_BOB,
            MusicianConfigurationInfos.PORT_CARL);
        alice = musicians.get(0);
        bob = musicians.get(1);
        carl = musicians.get(2);

        alice.pEV.newJavaProjectWithClass(PROJECT1, PKG1, CLS1);
    }

    /**
     * Closes all opened xmppConnects, popup windows and editor.<br/>
     * Delete all existed projects.
     * 
     * @throws RemoteException
     */
    @AfterClass
    public static void resetSaros() throws RemoteException {
        carl.workbench.resetSaros();
        bob.workbench.resetSaros();
        alice.workbench.resetSaros();
    }

    /**
     * Closes all opened popup windows and editors.<br/>
     * Set the line delimiter to default.
     * 
     * @throws RemoteException
     */
    @After
    public void cleanUp() throws RemoteException {
        carl.workbench.resetWorkbench();
        carl.mainMenu.newTextFileLineDelimiter("Default");
        bob.workbench.resetWorkbench();
        bob.mainMenu.newTextFileLineDelimiter("Default");
        alice.workbench.resetWorkbench();
        alice.mainMenu.newTextFileLineDelimiter("Default");

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
        alice.mainMenu.newTextFileLineDelimiter("Unix");

        alice.buildSessionSequentially(PROJECT1, CONTEXT_MENU_SHARE_PROJECT, carl,
            bob);

        String delimiterByAlice = alice.mainMenu.getTextFileLineDelimiter();
        String delimiterByCarl = carl.mainMenu.getTextFileLineDelimiter();
        String delimiterByBob = bob.mainMenu.getTextFileLineDelimiter();

        log.debug("delimiter by alice: " + delimiterByAlice
            + "delimiter by bob: " + delimiterByBob + "delimiter by carl: "
            + delimiterByCarl);

        /*
         * TODO there are some bugs in saros... need to be fixed. Now you will
         * get AssertionError here.
         */
        assertTrue(delimiterByAlice.equals(delimiterByCarl));
        assertTrue(delimiterByAlice.equals(delimiterByBob));

        alice.leaveSessionFirstByPeers(carl, bob);

    }
}
