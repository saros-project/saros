package de.fu_berlin.inf.dpp.stf.client;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.XMPPException;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.client.test.helpers.MakeOperationConcurrently;
import de.fu_berlin.inf.dpp.stf.client.test.helpers.STFTest;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.noGUI.SarosState;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.ChatViewComponent;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.RSViewComponent;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.RosterViewComponent;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.SarosMainMenuComponent;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.SarosPEViewComponent;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.SarosWorkbenchComponent;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.SessionViewComponent;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench.BasicComponent;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench.EditorComponent;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench.ProgressViewComponent;

/**
 * Musician encapsulates a test instance of Saros. It takes use of all RMI
 * interfaces to help testwriters to write their STF tests nicely. STF is short
 * for Sandor's Test Framework.
 */
public class Musician extends STFTest {
    private static final Logger log = Logger.getLogger(Musician.class);

    public EditorComponent editor;
    public SarosPEViewComponent pEV;
    public SarosMainMenuComponent mainMenu;
    public ProgressViewComponent progressV;
    public BasicComponent basic;
    public SarosState state;
    public RosterViewComponent rosterV;
    public SessionViewComponent sessionV;
    public RSViewComponent rSV;
    public ChatViewComponent chatV;
    public SarosWorkbenchComponent workbench;

    public JID jid;
    public String password;
    public String host;
    public int port;
    public int typeOfSharingProject = CREATE_NEW_PROJECT;

    public Musician(JID jid, String password, String host, int port) {
        super();
        this.jid = jid;
        this.password = password;
        this.host = host;
        this.port = port;
    }

    /*************** init Methods ******************/

    public void initBot() throws AccessException, RemoteException,
        NotBoundException {
        log.trace("initBot enter, initRmi");
        initRmi();
    }

    private void initRmi() throws RemoteException, NotBoundException,
        AccessException {
        Registry registry = LocateRegistry.getRegistry(host, port);
        try {
            // bot = (ISarosRmiSWTWorkbenchBot) registry.lookup("Bot");
            state = (SarosState) registry.lookup("state");

            workbench = (SarosWorkbenchComponent) registry.lookup("workbench");
            chatV = (ChatViewComponent) registry.lookup("chatView");
            rosterV = (RosterViewComponent) registry.lookup("rosterView");
            sessionV = (SessionViewComponent) registry.lookup("sessionView");
            /*
             * TODO i am not sure, if i can pass the local value to remote
             * object. It worked for the local tests, but i don't know if it
             * work for the remote tests too.
             */

            sessionV.setJID(jid);
            rSV = (RSViewComponent) registry.lookup("remoteScreenView");
            // popupWindow = (ExWindowObject) registry.lookup("popUpWindow");

            editor = (EditorComponent) registry.lookup("eclipseEditor");
            pEV = (SarosPEViewComponent) registry.lookup("packageExplorerView");
            mainMenu = (SarosMainMenuComponent) registry
                .lookup("sarosMainMenu");
            progressV = (ProgressViewComponent) registry.lookup("progressView");
            basic = (BasicComponent) registry.lookup("basicObject");

        } catch (java.rmi.ConnectException e) {
            throw new RuntimeException("Could not connect to RMI of bot " + jid
                + ", did you start the Eclipse instance?");
        }

    }

    // ********** Component, which consist of other simple functions ***********

    public void buildSessionSequentially(String projectName,
        String howToShareProject, Musician... invitees) throws RemoteException {
        String[] inviteeBaseJIDs = new String[invitees.length];
        for (int i = 0; i < invitees.length; i++) {
            inviteeBaseJIDs[i] = invitees[i].getBaseJid();
        }
        pEV.shareProjectWith(projectName, howToShareProject, inviteeBaseJIDs);
        for (Musician invitee : invitees) {
            invitee.pEV.confirmWizardSessionInvitationUsingWhichProject(
                getBaseJid(), projectName, invitee.typeOfSharingProject);
        }
    }

    public void buildSessionConcurrentlyDone(final String projectName,
        String shareProjectWith, Musician... invitees) throws RemoteException,
        InterruptedException {
        String[] peersName = new String[invitees.length];
        for (int i = 0; i < invitees.length; i++) {
            peersName[i] = invitees[i].getBaseJid();
        }
        log.trace("alice.shareProjectParallel");
        this.pEV.shareProject(projectName, peersName);

        List<Callable<Void>> joinSessionTasks = new ArrayList<Callable<Void>>();
        for (final Musician musician : invitees) {
            joinSessionTasks.add(new Callable<Void>() {
                public Void call() throws Exception {
                    musician.pEV
                        .confirmWirzardSessionInvitationWithNewProject(projectName);
                    return null;
                }
            });
        }
        log.trace("workAll(joinSessionTasks)");
        MakeOperationConcurrently.workAll(joinSessionTasks,
            joinSessionTasks.size());
    }

    /**
     * Define the leave session with the following steps.
     * <ol>
     * <li>The host(alice) leave session first.</li>
     * <li>Then other invitees confirm the windonws "Closing the Session"
     * concurrently</li>
     * </ol>
     * 
     * @param peers
     *            the invitees
     * @throws RemoteException
     * @throws InterruptedException
     */
    public void leaveSessionHostFirstDone(Musician... peers)
        throws RemoteException, InterruptedException {
        sessionV.leaveTheSessionByHost();
        List<Callable<Void>> closeSessionTasks = new ArrayList<Callable<Void>>();
        for (int i = 0; i < peers.length; i++) {
            final Musician musician = peers[i];
            closeSessionTasks.add(new Callable<Void>() {
                public Void call() throws Exception {
                    // Need to check for isDriver before leaving.
                    musician.sessionV.confirmClosingTheSessionWindow();
                    return null;
                }
            });
        }
        MakeOperationConcurrently.workAll(closeSessionTasks,
            closeSessionTasks.size());
    }

    /**
     * Define the leave session with the following steps.
     * <ol>
     * <li>The musicians bob and carl leave the session first.(concurrently)</li>
     * <li>wait until bob and carl are really not in the session using
     * "waitUntilAllPeersLeaveSession", then leave the host alice.</li>
     * </ol>
     * make sure,
     * 
     * @param musicians
     *            bob and carl
     * @throws RemoteException
     * @throws InterruptedException
     */
    public void leaveSessionPeersFirstDone(Musician... musicians)
        throws RemoteException, InterruptedException {
        List<Callable<Void>> leaveTasks = new ArrayList<Callable<Void>>();
        for (int i = 0; i < musicians.length; i++) {
            final Musician musician = musicians[i];
            leaveTasks.add(new Callable<Void>() {
                public Void call() throws Exception {
                    musician.sessionV.leaveTheSessionByPeer();
                    return null;
                }
            });
        }
        List<JID> peerJIDs = new ArrayList<JID>();
        for (Musician musician : musicians) {
            peerJIDs.add(musician.jid);
        }
        MakeOperationConcurrently.workAll(leaveTasks, leaveTasks.size());
        sessionV.waitUntilAllPeersLeaveSession(peerJIDs);
        sessionV.clickTBleaveTheSession();
    }

    private String[] getPeersBaseJID(Musician... peers) {
        String[] peerBaseJIDs = new String[peers.length];
        for (int i = 0; i < peers.length; i++) {
            peerBaseJIDs[i] = peers[i].getBaseJid();
        }
        return peerBaseJIDs;
    }

    /**
     * the local user can be concurrently followed by many other users.
     * 
     * @param musicians
     *            the list of the remote Users who want to follow the local
     *            user.
     * @throws RemoteException
     * @throws InterruptedException
     */
    public void followedBy(Musician... musicians) throws RemoteException,
        InterruptedException {
        List<Callable<Void>> followTasks = new ArrayList<Callable<Void>>();
        for (int i = 0; i < musicians.length; i++) {
            final Musician musician = musicians[i];
            followTasks.add(new Callable<Void>() {
                public Void call() throws Exception {
                    musician.sessionV.followThisUserGUI(jid);
                    return null;
                }
            });
        }
        MakeOperationConcurrently.workAll(followTasks, followTasks.size());
    }

    /**
     * stop the follow-mode of the remote users who are following the local
     * user.
     * 
     * @param musicians
     *            the list of the remote Users who are following the local user.
     * @throws RemoteException
     * @throws InterruptedException
     */
    public void stopFollowedBy(Musician... musicians) throws RemoteException,
        InterruptedException {
        List<Callable<Void>> stopFollowTasks = new ArrayList<Callable<Void>>();
        for (int i = 0; i < musicians.length; i++) {
            final Musician musician = musicians[i];
            stopFollowTasks.add(new Callable<Void>() {
                public Void call() throws Exception {
                    musician.sessionV.stopFollowingThisUserGUI(jid);
                    return null;
                }
            });
        }
        MakeOperationConcurrently.workAll(stopFollowTasks,
            stopFollowTasks.size());
    }

    /**
     * @Return the name segment of {@link JID}.
     */
    public String getName() {
        return jid.getName();
    }

    /**
     * @Return the Jabber ID without resource qualifier.
     */
    public String getBaseJid() {
        return jid.getBase();
    }

    /**
     * @Return the resource qualified {@link JID}.
     */
    public String getRQjid() {
        return jid.toString();
    }

    public String getXmppServer() {
        return jid.getDomain();
    }

    /**
     * add buddy with GUI, which should be performed by both the users.
     * 
     * @param peer
     *            the musician, which should be added in your contact list
     * @throws RemoteException
     * @throws XMPPException
     * 
     */
    public void addBuddyDone(Musician peer) throws RemoteException,
        XMPPException {
        if (!rosterV.hasBuddy(peer.jid)) {
            rosterV.addANewContact(peer.jid);
            peer.rosterV.confirmRequestOfSubscriptionReceivedWindow();
            rosterV.confirmRequestOfSubscriptionReceivedWindow();
        }
    }

    /**
     * add buddy with GUI, which should be performed by both the users.
     * 
     * @param peer
     *            the musician, which should be added in your contact list
     * @throws RemoteException
     */
    public void addBuddyGUIDone(Musician peer) throws RemoteException {
        if (!rosterV.hasBuddy(peer.jid)) {
            rosterV.addANewContactGUI(peer.jid);
            peer.rosterV.confirmRequestOfSubscriptionReceivedWindow();
            rosterV.confirmRequestOfSubscriptionReceivedWindow();
        }
    }

    /**
     * Remove given contact from Roster with GUI, if contact was added before.
     */
    public void deleteBuddyGUIDone(Musician peer) throws RemoteException {
        if (!rosterV.hasBuddy(peer.jid))
            return;
        rosterV.deleteBuddyGUI(peer.jid);
        peer.rosterV.confirmRemovelOfSubscriptionWindow();
    }

    /**
     * Remove given contact from Roster without GUI, if contact was added before
     * 
     * @throws XMPPException
     */
    public void deleteBuddyDone(Musician peer) throws RemoteException,
        XMPPException {
        if (!rosterV.hasBuddy(peer.jid))
            return;
        rosterV.deleteBuddy(peer.jid);
        peer.rosterV.confirmRemovelOfSubscriptionWindow();
    }

    public void shareYourScreenWithSelectedUserDone(Musician peer)
        throws RemoteException {
        sessionV.shareYourScreenWithSelectedUserGUI(peer.jid);
        peer.sessionV.confirmIncomingScreensharingSesionWindow();
    }

    /**
     * This method is same as
     * {@link Musician#buildSessionConcurrentlyDone(String, String, Musician...)}
     * . The difference to buildSessionConcurrently is that the invitation
     * process is activated by clicking the toolbarbutton
     * "open invitation interface" in the roster view.
     * 
     * @param projectName
     *            the name of the project which is in a session now.
     * @param peers
     *            the user whom you want to invite to your session.
     * @throws RemoteException
     * @throws InterruptedException
     */
    public void inviteUsersInYourSessionDone(final String projectName,
        Musician... peers) throws RemoteException, InterruptedException {
        sessionV.openInvitationInterface(getPeersBaseJID(peers));
        List<Callable<Void>> joinSessionTasks = new ArrayList<Callable<Void>>();
        for (final Musician musician : peers) {
            joinSessionTasks.add(new Callable<Void>() {
                public Void call() throws Exception {
                    musician.pEV
                        .confirmWirzardSessionInvitationWithNewProject(projectName);
                    return null;
                }
            });
        }
        log.trace("workAll(joinSessionTasks)");
        MakeOperationConcurrently.workAll(joinSessionTasks,
            joinSessionTasks.size());

    }
}