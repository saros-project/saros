package de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.testSTF.views.sarosViews.content;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.STFTest;

public class TestBuddiesByAliceBob extends STFTest {

    /**
     * Preconditions:
     * <ol>
     * <li>Alice (Host, Write Access)</li>
     * <li>Bob (Read-Only Access)</li>
     * <li>Car (Read-Only Access)</li>
     * <li>Alice share a java project with bob</li>
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

    @After
    public void runAfterEveryTest() throws RemoteException {
        resetBuddies();
        resetBuddiesName();
    }

    @Test
    public void testPreCondition() {
        //
    }

    /**
     * Steps:
     * <ol>
     * <li>alice rename bob to "bob_stf".</li>
     * <li>alice rename bob to "new bob".</li>
     * </ol>
     * 
     * Result:
     * <ol>
     * <li>alice hat contact with bob and bob'name is changed.</li>
     * <li>alice hat contact with bob and bob'name is changed.</li>
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
        assertTrue(alice.superBot().views().sarosView().hasBuddy(bob.getJID()));
        alice.superBot().views().sarosView().selectBuddy(bob.getJID())
            .rename(bob.getName());

        assertTrue(alice.superBot().views().sarosView().hasBuddy(bob.getJID()));
        assertTrue(alice.superBot().views().sarosView()
            .getNickName(bob.getJID()).equals(bob.getName()));

        alice.superBot().views().sarosView().selectBuddy(bob.getJID())
            .rename("new name");
        assertTrue(alice.superBot().views().sarosView().hasBuddy(bob.getJID()));
        assertTrue(alice.superBot().views().sarosView()
            .getNickName(bob.getJID()).equals("new name"));
    }

    @Test
    public void addBuddy() throws RemoteException {
        deleteBuddies(alice, bob);
        assertFalse(alice.superBot().views().sarosView().hasBuddy(bob.getJID()));
        assertFalse(bob.superBot().views().sarosView().hasBuddy(alice.getJID()));
        addBuddies(alice, bob);
        assertTrue(alice.superBot().views().sarosView().hasBuddy(bob.getJID()));
        assertTrue(bob.superBot().views().sarosView().hasBuddy(alice.getJID()));
    }

    /**
     * Steps:
     * <ol>
     * <li>alice delete bob</li>
     * </ol>
     * 
     * Result:
     * <ol>
     * <li>alice and bob don't contact each other</li>
     * </ol>
     * 
     * @throws RemoteException
     */
    @Test
    public void deleteBuddy() throws RemoteException {
        assertTrue(alice.superBot().views().sarosView().hasBuddy(bob.getJID()));
        deleteBuddies(alice, bob);
        assertFalse(alice.superBot().views().sarosView().hasBuddy(bob.getJID()));
        assertFalse(bob.superBot().views().sarosView().hasBuddy(alice.getJID()));
    }

    @Test
    public void workTogetherOnPorject() throws RemoteException {
        alice.superBot().views().packageExplorerView().tree().newC()
            .javaProject(PROJECT1);
        alice.superBot().views().sarosView().selectBuddy(bob.getJID())
            .workTogetherOn().project(PROJECT1);
        bob.superBot().confirmShellSessionInvitationAndShellAddProject(
            PROJECT1, TypeOfCreateProject.NEW_PROJECT);

        alice.superBot().views().sarosView().selectBuddies()
            .stopSarosSession();

    }

    @Test
    @Ignore("contextMenu multipleProjects doesn't work")
    public void workTogetherOnMultiPorject() throws RemoteException {
        alice.superBot().views().packageExplorerView().tree().newC()
            .javaProject(PROJECT1);
        alice.superBot().views().sarosView().selectBuddy(bob.getJID())
            .workTogetherOn().multipleProjects(PROJECT1, bob.getJID());
        bob.superBot().confirmShellSessionInvitationAndShellAddProject(
            PROJECT1, TypeOfCreateProject.NEW_PROJECT);
    }

}
