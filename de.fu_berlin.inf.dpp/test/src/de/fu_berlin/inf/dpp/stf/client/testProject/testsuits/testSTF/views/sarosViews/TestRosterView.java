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
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotView;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.views.sarosViews.RosterView;

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
     */
    @Test
    public void renameBuddyWithGUI() throws RemoteException {
        assertTrue(alice.sarosBot().buddiesView().hasBuddyNoGUI(bob.jid));
        alice.sarosBot().buddiesView().renameBuddy(bob.jid, bob.getName());
        assertTrue(alice.sarosBot().buddiesView().hasBuddyNoGUI(bob.jid));
        assertTrue(alice.sarosBot().buddiesView()
            .getBuddyNickNameNoGUI(bob.jid).equals(bob.getName()));
        // assertTrue(alice.sessionV.isContactInSessionView(bob.jid));
        alice.sarosBot().buddiesView().renameBuddy(bob.jid, "new bob");
        assertTrue(alice.sarosBot().buddiesView().hasBuddyNoGUI(bob.jid));
        assertTrue(alice.sarosBot().buddiesView()
            .getBuddyNickNameNoGUI(bob.jid).equals("new bob"));
        // assertTrue(alice.sessionV.isContactInSessionView(bob.jid));
    }

    @Test
    public void renameBuddyWithoutGUI() throws RemoteException {
        assertTrue(alice.sarosBot().buddiesView().hasBuddyNoGUI(bob.jid));
        alice.sarosBot().buddiesView().renameBuddyNoGUI(bob.jid, bob.getName());
        assertTrue(alice.sarosBot().buddiesView().hasBuddyNoGUI(bob.jid));
        assertTrue(alice.sarosBot().buddiesView()
            .getBuddyNickNameNoGUI(bob.jid).equals(bob.getName()));
        // assertTrue(alice.sessionV.isContactInSessionView(bob.jid));
        alice.sarosBot().buddiesView().renameBuddyNoGUI(bob.jid, "new bob");
        assertTrue(alice.sarosBot().buddiesView().hasBuddyNoGUI(bob.jid));
        assertTrue(alice.sarosBot().buddiesView()
            .getBuddyNickNameNoGUI(bob.jid).equals("new bob"));
        // assertTrue(alice.sessionV.isContactInSessionView(bob.jid));
    }

    @Test
    public void addBuddyWithGUI() throws RemoteException {
        deleteBuddies(alice, bob);
        assertFalse(alice.sarosBot().buddiesView().hasBuddyNoGUI(bob.jid));
        assertFalse(bob.sarosBot().buddiesView().hasBuddyNoGUI(alice.jid));
        addBuddies(alice, bob);
        assertTrue(alice.sarosBot().buddiesView().hasBuddyNoGUI(bob.jid));
        assertTrue(bob.sarosBot().buddiesView().hasBuddyNoGUI(alice.jid));
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
        assertFalse(alice.sarosBot().buddiesView().hasBuddyNoGUI(bob.jid));
        assertFalse(bob.sarosBot().buddiesView().hasBuddyNoGUI(alice.jid));
        addBuddies(alice, bob);
        assertTrue(alice.sarosBot().buddiesView().hasBuddyNoGUI(bob.jid));
        assertTrue(bob.sarosBot().buddiesView().hasBuddyNoGUI(alice.jid));
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
        assertTrue(alice.sarosBot().buddiesView().hasBuddyNoGUI(bob.jid));
        deleteBuddies(alice, bob);
        assertFalse(alice.sarosBot().buddiesView().hasBuddyNoGUI(bob.jid));
        assertFalse(bob.sarosBot().buddiesView().hasBuddyNoGUI(alice.jid));
    }

    @Test
    public void deleteBuddyNoGUI() throws RemoteException, XMPPException {
        assertTrue(alice.sarosBot().buddiesView().hasBuddyNoGUI(bob.jid));
        deleteBuddiesNoGUI(alice, bob);
        assertFalse(alice.sarosBot().buddiesView().hasBuddyNoGUI(bob.jid));
        assertFalse(bob.sarosBot().buddiesView().hasBuddyNoGUI(alice.jid));
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
        assertFalse(carl.sarosBot().sessionView().isInSession());
        alice.sarosBot().buddiesView().inviteBuddy(carl.jid);
        carl.sarosBot().packageExplorerView().saros()
            .confirmShellSessionnInvitation();
        carl.sarosBot().packageExplorerView().saros()
            .confirmShellAddProjectWithNewProject(PROJECT1);
        carl.sarosBot().sessionView().waitUntilIsInSession();
        assertTrue(carl.sarosBot().sessionView().isInSession());

    }

}
