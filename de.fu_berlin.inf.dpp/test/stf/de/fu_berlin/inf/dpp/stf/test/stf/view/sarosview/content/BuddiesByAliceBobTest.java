package de.fu_berlin.inf.dpp.stf.test.stf.view.sarosview.content;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.BOB;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.StfTestCase;
import de.fu_berlin.inf.dpp.stf.client.util.Util;
import de.fu_berlin.inf.dpp.stf.shared.Constants.TypeOfCreateProject;
import de.fu_berlin.inf.dpp.stf.test.Constants;

public class BuddiesByAliceBobTest extends StfTestCase {

    /**
     * Preconditions:
     * <ol>
     * <li>Alice (Host, Write Access)</li>
     * <li>Bob (Read-Only Access)</li>
     * <li>Car (Read-Only Access)</li>
     * <li>Alice share a java project with BOB</li>
     * </ol>
     * 
     * @throws RemoteException
     */

    @BeforeClass
    public static void selectTesters() throws Exception {
        select(ALICE, BOB);
    }

    @After
    public void afterEveryTest() throws RemoteException {
        resetBuddies();
        resetBuddyNames();
        clearWorkspaces();
    }

    @Test
    public void testAddExistingBuddy() throws RemoteException {
        assertTrue(ALICE.superBot().views().sarosView().hasBuddy(BOB.getJID()));
        ALICE.superBot().views().sarosView().selectBuddies()
            .addBuddy(BOB.getJID());
        assertTrue(ALICE.superBot().views().sarosView().hasBuddy(BOB.getJID()));
    }

    /**
     * Steps:
     * <ol>
     * <li>ALICE rename BOB to "BOB_stf".</li>
     * <li>ALICE rename BOB to "new BOB".</li>
     * </ol>
     * 
     * Result:
     * <ol>
     * <li>ALICE hat contact with BOB and BOB'name is changed.</li>
     * <li>ALICE hat contact with BOB and BOB'name is changed.</li>
     * </ol>
     * 
     * @throws RemoteException
     * 
     *             TODO: This test isn't stable, sometime it is successful,
     *             sometime not. I think, there are some little bugs in this
     *             test case.
     */
    @Test
    public void renameBuddy() throws RemoteException {
        assertTrue(ALICE.superBot().views().sarosView().hasBuddy(BOB.getJID()));
        ALICE.superBot().views().sarosView().selectBuddy(BOB.getJID())
            .rename(BOB.getName());

        assertTrue(ALICE.superBot().views().sarosView().hasBuddy(BOB.getJID()));
        assertTrue(ALICE.superBot().views().sarosView()
            .getNickname(BOB.getJID()).equals(BOB.getName()));

        ALICE.superBot().views().sarosView().selectBuddy(BOB.getJID())
            .rename("new name");
        assertTrue(ALICE.superBot().views().sarosView().hasBuddy(BOB.getJID()));
        assertTrue(ALICE.superBot().views().sarosView()
            .getNickname(BOB.getJID()).equals("new name"));
    }

    @Test
    public void addBuddy() throws RemoteException {
        Util.removeBuddiesFromContactList(ALICE, BOB);
        assertFalse(ALICE.superBot().views().sarosView().hasBuddy(BOB.getJID()));
        assertFalse(BOB.superBot().views().sarosView().hasBuddy(ALICE.getJID()));
        Util.addBuddiesToContactList(ALICE, BOB);
        assertTrue(ALICE.superBot().views().sarosView().hasBuddy(BOB.getJID()));
        assertTrue(BOB.superBot().views().sarosView().hasBuddy(ALICE.getJID()));
    }

    /**
     * Steps:
     * <ol>
     * <li>ALICE delete BOB</li>
     * </ol>
     * 
     * Result:
     * <ol>
     * <li>ALICE and BOB don't contact each other</li>
     * </ol>
     * 
     * @throws RemoteException
     */
    @Test
    public void deleteBuddy() throws RemoteException {
        assertTrue(ALICE.superBot().views().sarosView().hasBuddy(BOB.getJID()));
        Util.removeBuddiesFromContactList(ALICE, BOB);
        assertFalse(ALICE.superBot().views().sarosView().hasBuddy(BOB.getJID()));
        assertFalse(BOB.superBot().views().sarosView().hasBuddy(ALICE.getJID()));
        ALICE.remoteBot().sleep(5000);
    }

    @Test
    public void workTogetherOnProject() throws RemoteException {
        ALICE
            .superBot()
            .views()
            .packageExplorerView()
            .tree()
            .newC()
            .javaProjectWithClasses(Constants.PROJECT1, Constants.PKG1,
                Constants.CLS1);
        ALICE.superBot().views().sarosView().selectBuddy(BOB.getJID())
            .workTogetherOn().project(Constants.PROJECT1);
        BOB.superBot().confirmShellSessionInvitationAndShellAddProject(
            Constants.PROJECT1, TypeOfCreateProject.NEW_PROJECT);

        BOB.superBot()
            .views()
            .packageExplorerView()
            .waitUntilClassExists(Constants.PROJECT1, Constants.PKG1,
                Constants.CLS1);

        ALICE.superBot().views().sarosView().selectBuddies().stopSarosSession();
        ALICE.superBot().views().sarosView().waitUntilIsNotInSession();

    }

    @Test
    public void workTogetherOnMultiProject() throws RemoteException {
        ALICE.superBot().views().packageExplorerView().tree().newC()
            .javaProject(Constants.PROJECT1);
        ALICE.superBot().views().sarosView().selectBuddy(BOB.getJID())
            .workTogetherOn()
            .multipleProjects(Constants.PROJECT1, BOB.getJID());
        BOB.superBot().confirmShellSessionInvitationAndShellAddProject(
            Constants.PROJECT1, TypeOfCreateProject.NEW_PROJECT);

        ALICE.superBot().views().sarosView().selectBuddies().stopSarosSession();
        ALICE.superBot().views().sarosView().waitUntilIsNotInSession();

    }

}
