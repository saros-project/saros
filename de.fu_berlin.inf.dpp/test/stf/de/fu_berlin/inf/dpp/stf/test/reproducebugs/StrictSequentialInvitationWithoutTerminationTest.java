package de.fu_berlin.inf.dpp.stf.test.reproducebugs;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.BOB;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.CARL;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.StfTestCase;
import de.fu_berlin.inf.dpp.stf.client.util.Util;
import de.fu_berlin.inf.dpp.stf.shared.Constants.TypeOfCreateProject;
import de.fu_berlin.inf.dpp.stf.test.Constants;

public class StrictSequentialInvitationWithoutTerminationTest extends
    StfTestCase {

    /**
     * Preconditions:
     * <ol>
     * <li>ALICE (Host, Write Access)</li>
     * <li>BOB (Read-Only Access)</li>
     * <li>CARL (Read-Only Access)</li>
     * </ol>
     * 
     * @throws RemoteException
     * @throws InterruptedException
     */

    @BeforeClass
    public static void selectTesters() throws Exception {
        select(ALICE, BOB, CARL);
    }

    @After
    public void runAfterEveryTest() throws RemoteException {
        CARL.superBot().menuBar().window()
            .setNewTextFileLineDelimiter("Default");
        BOB.superBot().menuBar().window()
            .setNewTextFileLineDelimiter("Default");
        ALICE.superBot().menuBar().window()
            .setNewTextFileLineDelimiter("Default");
    }

    /**
     * 
     * TODO the test failed and need to be fixed at first. <br/>
     * 
     * Steps:
     * <ol>
     * <li>ALICE is either in global or project-specific settings and chooses
     * explicitly one of the possible line delimiter.</li>
     * <li>CARL invites all strictly sequentially, one after the other.</li>
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
    public void testSetLineDelimiter() throws Exception {

        ALICE
            .superBot()
            .views()
            .packageExplorerView()
            .tree()
            .newC()
            .javaProjectWithClasses(Constants.PROJECT1, Constants.PKG1,
                Constants.CLS1);

        ALICE.superBot().menuBar().window().setNewTextFileLineDelimiter("Unix");

        Util.buildSessionSequentially(Constants.PROJECT1,
            TypeOfCreateProject.NEW_PROJECT, CARL, ALICE, BOB);

        String delimiterByAlice = ALICE.superBot().menuBar().window()
            .getTextFileLineDelimiter();
        String delimiterByCarl = CARL.superBot().menuBar().window()
            .getTextFileLineDelimiter();
        String delimiterByBob = BOB.superBot().menuBar().window()
            .getTextFileLineDelimiter();

        System.out.println("delimiter by ALICE: " + delimiterByAlice
            + "delimiter by BOB: " + delimiterByBob + "delimiter by CARL: "
            + delimiterByCarl);

        /*
         * TODO there are some bugs in saros... need to be fixed. Now you will
         * get AssertionError here.
         */
        assertTrue(delimiterByAlice.equals(delimiterByCarl));
        assertTrue(delimiterByAlice.equals(delimiterByBob));

        leaveSessionPeersFirst(ALICE);

    }
}
