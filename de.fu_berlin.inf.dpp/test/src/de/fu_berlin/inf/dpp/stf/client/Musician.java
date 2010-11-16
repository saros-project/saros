package de.fu_berlin.inf.dpp.stf.client;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.client.test.helpers.MakeOperationConcurrently;
import de.fu_berlin.inf.dpp.stf.server.BotConfiguration;
import de.fu_berlin.inf.dpp.stf.server.SarosConstant;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.noGUI.SarosState;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.ChatViewComponent;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.SarosMainMenuComponent;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.RSViewComponent;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.RosterViewComponent;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.SessionViewComponent;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.SarosWorkbenchComponent;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench.BasicComponent;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench.EditorComponent;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench.PEViewComponent;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench.ProgressViewComponent;

/**
 * Musician encapsulates a test instance of Saros. It takes use of all RMI
 * interfaces to help testwriters to write their STF tests nicely. STF is short
 * for Sandor's Test Framework.
 */
public class Musician {
    private static final Logger log = Logger.getLogger(Musician.class);

    public EditorComponent eclipseEditor;
    public PEViewComponent packageExplorerV;
    public SarosMainMenuComponent mainMenu;
    public ProgressViewComponent progressV;
    public BasicComponent basic;

    // public ISarosRmiSWTWorkbenchBot bot;
    public SarosState state;
    public RosterViewComponent rosterV;
    public SessionViewComponent sessionV;
    public RSViewComponent remoteScreenV;
    public ChatViewComponent chatV;
    // public ExWindowObject popupWindow;
    public SarosWorkbenchComponent workbench;

    public JID jid;
    public String password;
    public String host;
    public int port;
    public int typeOfSharingProject = SarosConstant.CREATE_NEW_PROJECT;

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
        log.trace("activeEclipseShell");
        workbench.activateEclipseShell();
        log.trace("closeWelcomeView");
        workbench.closeWelcomeView();
        log.trace("openJavaPerspective");
        mainMenu.openPerspectiveJava();
        log.trace("openSarosViews");
        workbench.openSarosViews();
        log.trace("xmppConnect");
        rosterV.connect(jid, password);
        log.trace("initBot leave");
    }

    private void initRmi() throws RemoteException, NotBoundException,
        AccessException {
        Registry registry = LocateRegistry.getRegistry(host, port);
        try {
            // bot = (ISarosRmiSWTWorkbenchBot) registry.lookup("Bot");
            state = (SarosState) registry.lookup("state");
            /*
             * TODO i am not sure, if i can pass the local value to remote
             * object. It worked for the local tests, but i don't know if it
             * work for the remote tests too.
             */
            state.setJID(jid);
            workbench = (SarosWorkbenchComponent) registry.lookup("workbench");
            chatV = (ChatViewComponent) registry.lookup("chatView");
            rosterV = (RosterViewComponent) registry.lookup("rosterView");
            sessionV = (SessionViewComponent) registry.lookup("sessionView");
            remoteScreenV = (RSViewComponent) registry
                .lookup("remoteScreenView");
            // popupWindow = (ExWindowObject) registry.lookup("popUpWindow");

            eclipseEditor = (EditorComponent) registry.lookup("eclipseEditor");
            packageExplorerV = (PEViewComponent) registry
                .lookup("packageExplorerView");
            mainMenu = (SarosMainMenuComponent) registry.lookup("sarosMainMenu");
            progressV = (ProgressViewComponent) registry.lookup("progressView");
            basic = (BasicComponent) registry.lookup("basicObject");

        } catch (java.rmi.ConnectException e) {
            throw new RuntimeException("Could not connect to RMI of bot " + jid
                + ", did you start the Eclipse instance?");
        }

    }

    /*************** Component, which consist of other simple functions ******************/

    public void buildSessionSequential(String projectName,
        String shareProjectWith, Musician... invitees) throws RemoteException {
        String[] inviteeJIDs = new String[invitees.length];
        for (int i = 0; i < invitees.length; i++) {
            inviteeJIDs[i] = invitees[i].getBaseJid();
        }
        packageExplorerV.clickShareProjectWith(projectName, shareProjectWith);

        packageExplorerV.confirmInvitationWindow(inviteeJIDs);
        for (Musician invitee : invitees) {
            invitee.packageExplorerV.confirmSessionUsingNewOrExistProject(
                this.jid, projectName, invitee.typeOfSharingProject);
        }
    }

    public void buildSessionConcurrently(String projectName,
        String shareProjectWith, Musician... invitees) throws RemoteException,
        InterruptedException {
        List<Musician> peers = new LinkedList<Musician>();
        List<String> peersName = new LinkedList<String>();
        for (Musician invitee : invitees) {
            peers.add(invitee);
            peersName.add(invitee.getBaseJid());
        }

        log.trace("alice.shareProjectParallel");
        this.packageExplorerV.shareProject(BotConfiguration.PROJECTNAME,
            peersName);

        List<Callable<Void>> joinSessionTasks = new ArrayList<Callable<Void>>();
        for (int i = 0; i < peers.size(); i++) {
            final Musician musician = peers.get(i);
            joinSessionTasks.add(new Callable<Void>() {
                public Void call() throws Exception {
                    musician.packageExplorerV.confirmSessionInvitationWizard(
                        getBaseJid(), BotConfiguration.PROJECTNAME);
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
     * <li>Then confirm the windonws "Closing the Session" for musicians carl
     * and bob concurrently</li>
     * </ol>
     * 
     * @param musicians
     *            bob and carl.
     * @throws RemoteException
     * @throws InterruptedException
     */
    public void leaveSessionFirst(Musician... musicians)
        throws RemoteException, InterruptedException {
        sessionV.leaveTheSessionByHost();
        List<Callable<Void>> closeSessionTasks = new ArrayList<Callable<Void>>();
        for (int i = 0; i < musicians.length; i++) {
            final Musician musician = musicians[i];
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
    public void leaveSessionFirstByPeers(Musician... musicians)
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
        sessionV.leaveTheSessionByHost();
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
                    musician.sessionV.followThisUser(state);
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
                    musician.sessionV.stopFollowingThisUser(state);
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

    public void addContactDone(Musician peer) throws RemoteException {
        if (!rosterV.hasContactWith(peer.jid)) {
            rosterV.addANewContact(peer.jid);
            peer.rosterV.confirmRequestOfSubscriptionReceivedWindow();
            rosterV.confirmRequestOfSubscriptionReceivedWindow();
        }
    }

    /**
     * Remove given contact from Roster, if contact was added before.
     */
    public void deleteContactDone(Musician peer) throws RemoteException {
        if (!rosterV.hasContactWith(peer.jid))
            return;
        rosterV.deleteContact(peer.jid);
        peer.rosterV.confirmRemovelOfSubscriptionWindow();
    }

    public void shareYourScreenWithSelectedUserDone(Musician peer)
        throws RemoteException {
        sessionV.shareYourScreenWithSelectedUser(peer.state);
        peer.sessionV.confirmIncomingScreensharingSesionWindow();
    }

}