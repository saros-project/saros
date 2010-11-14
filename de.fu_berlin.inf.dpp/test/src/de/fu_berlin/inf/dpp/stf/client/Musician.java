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
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.noGUI.ExStateObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.ExChatViewObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.ExMainMenuObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.ExRemoteScreenViewObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.ExRosterViewObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.ExSessionViewObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.ExWindowObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.ExWorkbenchObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench.ExBasicObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench.ExEditorObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench.ExPackageExplorerViewObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench.ExProgressViewObject;

/**
 * Musician encapsulates a test instance of Saros. It takes use of all RMI
 * interfaces to help testwriters to write their STF tests nicely. STF is short
 * for Sandor's Test Framework.
 */
public class Musician {
    private static final Logger log = Logger.getLogger(Musician.class);

    public ExEditorObject eclipseEditor;
    public ExPackageExplorerViewObject packageExplorerV;
    public ExMainMenuObject mainMenu;
    public ExProgressViewObject progressV;
    public ExBasicObject basic;

    // public ISarosRmiSWTWorkbenchBot bot;
    public ExStateObject state;
    public ExRosterViewObject rosterV;
    public ExSessionViewObject sessionV;
    public ExRemoteScreenViewObject remoteScreenV;
    public ExChatViewObject chatV;
    public ExWindowObject popupWindow;
    public ExWorkbenchObject workbench;

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
        packageExplorerV.closeWelcomeView();
        log.trace("openJavaPerspective");
        mainMenu.openPerspectiveJava();
        log.trace("openSarosViews");
        workbench.openSarosViews();
        log.trace("xmppConnect");
        rosterV.xmppConnect(jid, password);
        log.trace("initBot leave");
    }

    private void initRmi() throws RemoteException, NotBoundException,
        AccessException {
        Registry registry = LocateRegistry.getRegistry(host, port);
        try {
            // bot = (ISarosRmiSWTWorkbenchBot) registry.lookup("Bot");
            state = (ExStateObject) registry.lookup("state");
            /*
             * TODO i am not sure, if i can pass the local value to remote
             * object. It worked for the local tests, but i don't know if it
             * work for the remote tests too.
             */
            state.setJID(jid);
            workbench = (ExWorkbenchObject) registry.lookup("workbench");
            chatV = (ExChatViewObject) registry.lookup("chatView");
            rosterV = (ExRosterViewObject) registry.lookup("rosterView");
            sessionV = (ExSessionViewObject) registry.lookup("sessionView");
            remoteScreenV = (ExRemoteScreenViewObject) registry
                .lookup("remoteScreenView");
            popupWindow = (ExWindowObject) registry.lookup("popUpWindow");

            eclipseEditor = (ExEditorObject) registry.lookup("eclipseEditor");
            packageExplorerV = (ExPackageExplorerViewObject) registry
                .lookup("packageExplorerView");
            mainMenu = (ExMainMenuObject) registry.lookup("sarosMainMenu");
            progressV = (ExProgressViewObject) registry.lookup("progressView");
            basic = (ExBasicObject) registry.lookup("basicObject");

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

        popupWindow.confirmInvitationWindow(inviteeJIDs);
        for (Musician invitee : invitees) {
            invitee.popupWindow.confirmSessionUsingNewOrExistProject(this.jid,
                projectName, invitee.typeOfSharingProject);
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
                    musician.popupWindow.confirmSessionInvitationWizard(
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
                    musician.popupWindow
                        .waitUntilShellActive("Closing the Session");
                    musician.popupWindow.confirmWindow("Closing the Session",
                        SarosConstant.BUTTON_OK);
                    musician.popupWindow
                        .waitUntilShellCloses("Closing the Session");
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

    public void giveExclusiveDriverRole(Musician invitee)
        throws RemoteException {
        if (invitee.state.isDriver(invitee.jid)) {
            throw new RuntimeException(
                "User \""
                    + invitee.getBaseJid()
                    + "\" is already a driver! Please pass a correct Musician Object to the method.");
        }
        if (invitee.equals(this))
            sessionV.giveExclusiveDriverRole(SarosConstant.OWNCONTACTNAME);
        else
            sessionV.giveExclusiveDriverRole(invitee.getBaseJid());
    }

    public void removeDriverRole(Musician driver) throws RemoteException {
        if (!driver.state.isDriver()) {
            throw new RuntimeException(
                "User \""
                    + driver.getBaseJid()
                    + "\" is no driver! Please pass a correct Musician Object to the method.");
        }
        if (driver.equals(this))
            sessionV.removeDriverRole(SarosConstant.OWNCONTACTNAME);
        else
            sessionV.removeDriverRole(driver.getBaseJid());
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
            rosterV.addContact(peer.jid);
            peer.popupWindow.confirmRequestOfSubscriptionReceivedWindow();
            popupWindow.confirmRequestOfSubscriptionReceivedWindow();
        }
    }

    /**
     * Remove given contact from Roster, if contact was added before.
     */
    public void deleteContactDone(Musician peer) throws RemoteException {
        if (!rosterV.hasContactWith(peer.jid))
            return;
        rosterV.deleteContact(peer.jid);

        peer.popupWindow
            .waitUntilShellActive(SarosConstant.SHELL_TITLE_REMOVAL_OF_SUBSCRIPTION);
        peer.popupWindow.confirmWindow(
            SarosConstant.SHELL_TITLE_REMOVAL_OF_SUBSCRIPTION,
            SarosConstant.BUTTON_OK);

    }

}