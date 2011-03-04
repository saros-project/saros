package de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.testSTF.views.sarosViews;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.STFTest;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotView;

public class TestBuddiesView extends STFTest {

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
        initTesters(TypeOfTester.ALICE, TypeOfTester.BOB, TypeOfTester.CARL);
        setUpWorkbench();
        setUpSaros();
    }

    @After
    public void runAfterEveryTest() throws RemoteException {
        resetBuddies();
        resetBuddiesName();
    }

    @Test
    public void openCloseRosterView() throws RemoteException {
        alice.bot().view(VIEW_SAROS_BUDDIES).close();
        assertEquals(false, alice.bot().isViewOpen(VIEW_SAROS_BUDDIES));
        alice.bot().openViewById(VIEW_SAROS_BUDDIES_ID);
        assertEquals(true, alice.bot().isViewOpen(VIEW_SAROS_BUDDIES));
    }

    @Test
    public void setFocusOnRosterView() throws RemoteException {
        STFBotView view_buddies = alice.bot().view(VIEW_SAROS_BUDDIES);
        view_buddies.show();
        assertTrue(view_buddies.isActive());
        view_buddies.close();
        assertFalse(view_buddies.isActive());
        alice.bot().openViewById(VIEW_SAROS_BUDDIES_ID);
        assertTrue(alice.bot().isViewOpen(VIEW_SAROS_BUDDIES));
        alice.bot().view(VIEW_SAROS_SESSION).show();
        assertFalse(alice.bot().view(VIEW_SAROS_BUDDIES).isActive());
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
        assertTrue(alice.sarosBot().views().buddiesView().hasBuddy(bob.jid));
        alice.sarosBot().views().buddiesView().selectBuddy(bob.jid)
            .rename(bob.getName());
        assertTrue(alice.sarosBot().views().buddiesView().hasBuddy(bob.jid));
        assertTrue(alice.sarosBot().views().buddiesView().getNickName(bob.jid)
            .equals(bob.getName()));

        alice.sarosBot().views().buddiesView().selectBuddy(bob.jid)
            .rename("new name");
        assertTrue(alice.sarosBot().views().buddiesView().hasBuddy(bob.jid));
        alice.bot().sleep(500);
        assertTrue(alice.sarosBot().views().buddiesView().getNickName(bob.jid)
            .equals("new name"));
    }

    @Test
    public void addBuddy() throws RemoteException {
        deleteBuddies(alice, bob);
        assertFalse(alice.sarosBot().views().buddiesView().hasBuddy(bob.jid));
        assertFalse(bob.sarosBot().views().buddiesView().hasBuddy(alice.jid));
        addBuddies(alice, bob);
        assertTrue(alice.sarosBot().views().buddiesView().hasBuddy(bob.jid));
        assertTrue(bob.sarosBot().views().buddiesView().hasBuddy(alice.jid));
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
        assertTrue(alice.sarosBot().views().buddiesView().hasBuddy(bob.jid));
        deleteBuddies(alice, bob);
        assertFalse(alice.sarosBot().views().buddiesView().hasBuddy(bob.jid));
        assertFalse(bob.sarosBot().views().buddiesView().hasBuddy(alice.jid));
    }

    /**
     * Steps:
     * 
     * 1. Alice share session with bob.
     * 
     * 2. Alice invite carl.
     * 
     * Result:
     * <ol>
     * <li>bob is in the session</li>
     * </ol>
     * 
     * @throws RemoteException
     * @throws InterruptedException
     */
    @Test
    public void inviteBuddy() throws RemoteException, InterruptedException {
        setUpSessionWithAJavaProjectAndAClass(alice, bob);

        assertFalse(carl.sarosBot().views().sessionView().isInSession());
        alice.sarosBot().views().buddiesView().selectBuddy(carl.jid)
            .inviteBuddy();

        carl.bot().shell(SHELL_SESSION_INVITATION).confirm(FINISH);
        carl.sarosBot().confirmShellAddProjectWithNewProject(PROJECT1);
        carl.sarosBot().views().sessionView().waitUntilIsInSession();
        assertTrue(carl.sarosBot().views().sessionView().isInSession());

    }
}
