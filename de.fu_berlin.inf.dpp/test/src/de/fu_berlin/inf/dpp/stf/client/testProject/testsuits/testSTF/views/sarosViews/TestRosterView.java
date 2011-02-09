package de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.testSTF.views.sarosViews;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.jivesoftware.smack.XMPPException;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.STFTest;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.views.sarosViews.RosterView;

public class TestRosterView extends STFTest {

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
        setUpWorkbenchs();
        setUpSaros();
    }

    @After
    public void runAfterEveryTest() throws RemoteException {
        resetBuddies();
        resetBuddiesName();
    }

    @Test
    public void openCloseRosterView() throws RemoteException {
        alice.view.closeViewById(VIEW_SAROS_BUDDIES_ID);
        assertEquals(false, alice.view.isViewOpen(VIEW_SAROS_BUDDIES));
        alice.view.openViewById(VIEW_SAROS_BUDDIES_ID);
        assertEquals(true, alice.view.isViewOpen(VIEW_SAROS_BUDDIES));
    }

    @Test
    public void setFocusOnRosterView() throws RemoteException {
        alice.view.setFocusOnViewByTitle(VIEW_SAROS_BUDDIES);
        assertTrue(alice.view.isViewActive(VIEW_SAROS_BUDDIES));
        alice.view.closeViewById(VIEW_SAROS_BUDDIES_ID);
        assertFalse(alice.view.isViewActive(VIEW_SAROS_BUDDIES));

        alice.view.openViewById(VIEW_SAROS_BUDDIES_ID);
        assertTrue(alice.view.isViewActive(VIEW_SAROS_BUDDIES));

        alice.view.setFocusOnViewByTitle(VIEW_SAROS_SESSION);
        assertFalse(alice.view.isViewActive(VIEW_SAROS_BUDDIES));
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
     */
    @Test
    public void renameBuddyWithGUI() throws RemoteException {
        assertTrue(alice.sarosBuddiesV.hasBuddyNoGUI(bob.jid));
        alice.sarosBuddiesV.renameBuddy(bob.jid, bob.getName());
        assertTrue(alice.sarosBuddiesV.hasBuddyNoGUI(bob.jid));
        assertTrue(alice.sarosBuddiesV.getBuddyNickNameNoGUI(bob.jid).equals(
            bob.getName()));
        // assertTrue(alice.sessionV.isContactInSessionView(bob.jid));
        alice.sarosBuddiesV.renameBuddy(bob.jid, "new bob");
        assertTrue(alice.sarosBuddiesV.hasBuddyNoGUI(bob.jid));
        assertTrue(alice.sarosBuddiesV.getBuddyNickNameNoGUI(bob.jid).equals(
            "new bob"));
        // assertTrue(alice.sessionV.isContactInSessionView(bob.jid));
    }

    @Test
    public void renameBuddyWithoutGUI() throws RemoteException {
        assertTrue(alice.sarosBuddiesV.hasBuddyNoGUI(bob.jid));
        alice.sarosBuddiesV.renameBuddyNoGUI(bob.jid, bob.getName());
        assertTrue(alice.sarosBuddiesV.hasBuddyNoGUI(bob.jid));
        assertTrue(alice.sarosBuddiesV.getBuddyNickNameNoGUI(bob.jid).equals(
            bob.getName()));
        // assertTrue(alice.sessionV.isContactInSessionView(bob.jid));
        alice.sarosBuddiesV.renameBuddyNoGUI(bob.jid, "new bob");
        assertTrue(alice.sarosBuddiesV.hasBuddyNoGUI(bob.jid));
        assertTrue(alice.sarosBuddiesV.getBuddyNickNameNoGUI(bob.jid).equals(
            "new bob"));
        // assertTrue(alice.sessionV.isContactInSessionView(bob.jid));
    }

    @Test
    public void addBuddyWithGUI() throws RemoteException {
        deleteBuddies(alice, bob);
        assertFalse(alice.sarosBuddiesV.hasBuddyNoGUI(bob.jid));
        assertFalse(bob.sarosBuddiesV.hasBuddyNoGUI(alice.jid));
        addBuddies(alice, bob);
        assertTrue(alice.sarosBuddiesV.hasBuddyNoGUI(bob.jid));
        assertTrue(bob.sarosBuddiesV.hasBuddyNoGUI(alice.jid));
    }

    /**
     * FIXME the method
     * {@link RosterView#addANewContact(de.fu_berlin.inf.dpp.net.JID)}
     * 
     * @throws RemoteException
     * @throws XMPPException
     */
    @Test
    @Ignore
    public void addBuddyWithoutGUI() throws RemoteException, XMPPException {
        deleteBuddies(alice, bob);
        assertFalse(alice.sarosBuddiesV.hasBuddyNoGUI(bob.jid));
        assertFalse(bob.sarosBuddiesV.hasBuddyNoGUI(alice.jid));
        addBuddies(alice, bob);
        assertTrue(alice.sarosBuddiesV.hasBuddyNoGUI(bob.jid));
        assertTrue(bob.sarosBuddiesV.hasBuddyNoGUI(alice.jid));
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
        assertTrue(alice.sarosBuddiesV.hasBuddyNoGUI(bob.jid));
        deleteBuddies(alice, bob);
        assertFalse(alice.sarosBuddiesV.hasBuddyNoGUI(bob.jid));
        assertFalse(bob.sarosBuddiesV.hasBuddyNoGUI(alice.jid));
    }

    @Test
    public void deleteBuddyNoGUI() throws RemoteException, XMPPException {
        assertTrue(alice.sarosBuddiesV.hasBuddyNoGUI(bob.jid));
        deleteBuddiesNoGUI(alice, bob);
        assertFalse(alice.sarosBuddiesV.hasBuddyNoGUI(bob.jid));
        assertFalse(bob.sarosBuddiesV.hasBuddyNoGUI(alice.jid));
    }

    /**
     * Steps:
     * <ol>
     * <li>alice invite bob</li>
     * </ol>
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
    public void inviteBuddyWithGUI() throws RemoteException,
        InterruptedException {
        setUpSessionWithAJavaProjectAndAClass(alice, bob);
        assertFalse(carl.sarosSessionV.isInSession());
        alice.sarosBuddiesV.inviteBuddy(carl.jid);
        carl.sarosC.confirmShellSessionnInvitation();
        carl.sarosC.confirmShellAddProjectWithNewProject(PROJECT1);
        carl.sarosSessionV.waitUntilIsInSession();
        assertTrue(carl.sarosSessionV.isInSession());

    }

}
