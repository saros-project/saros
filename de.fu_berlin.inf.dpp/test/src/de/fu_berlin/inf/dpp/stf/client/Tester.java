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
import de.fu_berlin.inf.dpp.stf.client.testProject.helpers.MakeOperationConcurrently;
import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.STFTest.TypeOfCreateProject;
import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.STFTest.TypeOfShareProject;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.Workbench;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.basicWidgets.Button;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.basicWidgets.Label;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.basicWidgets.Menu;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.basicWidgets.Shell;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.basicWidgets.Table;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.basicWidgets.Text;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.basicWidgets.ToolbarButton;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.basicWidgets.Tree;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.basicWidgets.View;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.contextMenu.SarosC;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.contextMenu.TeamC;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.editor.Editor;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.menuBar.EditM;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.menuBar.FileM;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.menuBar.RefactorM;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.menuBar.SarosM;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.menuBar.WindowM;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.views.ConsoleView;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.views.PEView;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.views.ProgressView;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.views.sarosViews.ChatView;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.views.sarosViews.RSView;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.views.sarosViews.RosterView;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.views.sarosViews.SessionView;

/**
 * Tester encapsulates a test instance of Saros. It takes use of all RMI
 * interfaces to help testwriters to write their STF tests nicely. STF is short
 * for Sandor's Test Framework.
 */
public class Tester {
    private static final Logger log = Logger.getLogger(Tester.class);

    public PEView pEV;
    public ProgressView progressV;
    public RosterView sarosBuddiesV;
    public SessionView sarosSessionV;
    public RSView rSV;
    public ChatView chatV;
    public ConsoleView consoleV;

    public Table table;
    public Tree tree;
    public Button button;
    public ToolbarButton toolbarButton;
    public Shell shell;
    public View view;
    public Menu menu;
    public Label label;
    public Text text;

    public Workbench workbench;

    public Editor editor;

    // menuBar
    public FileM fileM;
    public EditM editM;
    public RefactorM refactorM;
    public WindowM windowM;
    public SarosM sarosM;

    // contextMenu
    public TeamC team;
    public SarosC sarosC;

    public JID jid;
    public String password;
    public String host;
    public int port;

    public Tester(JID jid, String password, String host, int port) {
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
        getRegistriedRmiObject();
    }

    private void getRegistriedRmiObject() throws RemoteException,
        NotBoundException, AccessException {
        Registry registry = LocateRegistry.getRegistry(host, port);
        try {

            chatV = (ChatView) registry.lookup("chatView");
            sarosBuddiesV = (RosterView) registry.lookup("rosterView");
            sarosSessionV = (SessionView) registry.lookup("sessionView");
            /*
             * TODO i am not sure, if i can pass the local value to remote
             * object. It worked for the local tests, but i don't know if it
             * work for the remote tests too.
             */
            sarosSessionV.setJID(jid);
            rSV = (RSView) registry.lookup("remoteScreenView");
            pEV = (PEView) registry.lookup("packageExplorerView");
            progressV = (ProgressView) registry.lookup("progressView");
            consoleV = (ConsoleView) registry.lookup("consoleView");
            workbench = (Workbench) registry.lookup("workbench");
            shell = (Shell) registry.lookup("shell");
            editor = (Editor) registry.lookup("eclipseEditor");

            table = (Table) registry.lookup("table");
            tree = (Tree) registry.lookup("tree");
            button = (Button) registry.lookup("button");
            toolbarButton = (ToolbarButton) registry.lookup("toolbarButton");
            menu = (Menu) registry.lookup("menu");
            view = (View) registry.lookup("view");
            label = (Label) registry.lookup("label");
            text = (Text) registry.lookup("text");
            // menus in menu bar
            fileM = (FileM) registry.lookup("fileM");
            editM = (EditM) registry.lookup("editM");
            refactorM = (RefactorM) registry.lookup("refactorM");
            windowM = (WindowM) registry.lookup("windowM");
            sarosM = (SarosM) registry.lookup("sarosM");

            // contextMenu
            team = (TeamC) registry.lookup("team");
            sarosC = (SarosC) registry.lookup("saros");

        } catch (java.rmi.ConnectException e) {
            throw new RuntimeException("Could not connect to RMI of bot " + jid
                + ", did you start the Eclipse instance?");
        }

    }

    // ********** Component, which consist of other simple functions ***********

    public void buildSessionDoneSequentially(String projectName,
        TypeOfShareProject howToShareProject,
        TypeOfCreateProject usingWhichProject, Tester... invitees)
        throws RemoteException {
        String[] baseJIDOfInvitees = getPeersBaseJID(invitees);

        sarosC.shareProjectWith(projectName, howToShareProject,
            baseJIDOfInvitees);
        for (Tester invitee : invitees) {
            invitee.sarosC.confirmShellSessionnInvitation();
            invitee.sarosC.confirmShellAddProjectUsingWhichProject(
                projectName, usingWhichProject);
        }
    }

    public void buildSessionDoneConcurrently(final String projectName,
        TypeOfShareProject howToShareProject,
        final TypeOfCreateProject usingWhichProject, Tester... invitees)
        throws RemoteException, InterruptedException {

        log.trace("alice.shareProjectParallel");
        this.sarosC.shareProjectWith(projectName, howToShareProject,
            getPeersBaseJID(invitees));

        List<Callable<Void>> joinSessionTasks = new ArrayList<Callable<Void>>();
        for (final Tester invitee : invitees) {
            joinSessionTasks.add(new Callable<Void>() {
                public Void call() throws Exception {
                    invitee.sarosC.confirmShellSessionnInvitation();
                    invitee.sarosC
                        .confirmShellAddProjectUsingWhichProject(
                            projectName, usingWhichProject);
                    invitee.sarosSessionV.waitUntilIsInSession();
                    return null;
                }
            });
        }
        log.trace("workAll(joinSessionTasks)");
        MakeOperationConcurrently.workAll(joinSessionTasks);
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
    public void leaveSessionHostFirstDone(Tester... peers)
        throws RemoteException, InterruptedException {
        List<Callable<Void>> closeSessionTasks = new ArrayList<Callable<Void>>();
        for (final Tester tester : peers) {
            if (tester.sarosSessionV.isInSession()) {
                closeSessionTasks.add(new Callable<Void>() {
                    public Void call() throws Exception {
                        // Need to check for isDriver before leaving.
                        tester.sarosSessionV.confirmClosingTheSessionWindow();
                        return null;
                    }
                });
            }
        }
        sarosSessionV.leaveTheSessionByHost();
        MakeOperationConcurrently.workAll(closeSessionTasks);
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
     * @param testers
     *            bob and carl
     * @throws RemoteException
     * @throws InterruptedException
     */
    public void leaveSessionPeersFirstDone(Tester... testers)
        throws RemoteException, InterruptedException {
        List<Callable<Void>> leaveTasks = new ArrayList<Callable<Void>>();
        for (int i = 0; i < testers.length; i++) {
            final Tester tester = testers[i];
            leaveTasks.add(new Callable<Void>() {
                public Void call() throws Exception {
                    tester.sarosSessionV.leaveTheSessionByPeer();
                    return null;
                }
            });
        }
        List<JID> peerJIDs = new ArrayList<JID>();
        for (Tester tester : testers) {
            peerJIDs.add(tester.jid);
        }
        MakeOperationConcurrently.workAll(leaveTasks);
        sarosSessionV.waitUntilAllPeersLeaveSession(peerJIDs);
        sarosSessionV.clickTBleaveTheSession();
        sarosSessionV.waitUntilSessionClosed();
    }

    private String[] getPeersBaseJID(Tester... peers) {
        String[] peerBaseJIDs = new String[peers.length];
        for (int i = 0; i < peers.length; i++) {
            peerBaseJIDs[i] = peers[i].getBaseJid();
        }
        return peerBaseJIDs;
    }

    /**
     * the local user can be concurrently followed by many other users.
     * 
     * @param testers
     *            the list of the buddies who want to follow the local user.
     * @throws RemoteException
     * @throws InterruptedException
     */
    public void followedBy(Tester... testers) throws RemoteException,
        InterruptedException {
        List<Callable<Void>> followTasks = new ArrayList<Callable<Void>>();
        for (int i = 0; i < testers.length; i++) {
            final Tester tester = testers[i];
            followTasks.add(new Callable<Void>() {
                public Void call() throws Exception {
                    tester.sarosSessionV.followThisBuddyGUI(jid);
                    return null;
                }
            });
        }
        MakeOperationConcurrently.workAll(followTasks);
    }

    /**
     * stop the follow-mode of the buddies who are following the local user.
     * 
     * @param testers
     *            the list of the buddies who are following the local user.
     * @throws RemoteException
     * @throws InterruptedException
     */
    public void stopFollowedBy(Tester... testers) throws RemoteException,
        InterruptedException {
        List<Callable<Void>> stopFollowTasks = new ArrayList<Callable<Void>>();
        for (int i = 0; i < testers.length; i++) {
            final Tester tester = testers[i];
            stopFollowTasks.add(new Callable<Void>() {
                public Void call() throws Exception {
                    tester.sarosSessionV.stopFollowingThisBuddyGUI(jid);
                    return null;
                }
            });
        }
        MakeOperationConcurrently.workAll(stopFollowTasks);
    }

    /**
     * @Return the name segment of {@link JID}.
     */
    public String getName() {
        return jid.getName();
    }

    /**
     * @Return the JID without resource qualifier.
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
     *            the user, which should be added in your contact list
     * @throws RemoteException
     * @throws XMPPException
     * 
     */
    public void addBuddyDone(Tester peer) throws RemoteException, XMPPException {
        if (!sarosBuddiesV.hasBuddy(peer.jid)) {
            sarosBuddiesV.addANewBuddyGUI(peer.jid);
            peer.sarosBuddiesV.confirmShellRequestOfSubscriptionReceived();
            sarosBuddiesV.confirmShellRequestOfSubscriptionReceived();
        }
    }

    /**
     * add buddy with GUI, which should be performed by both the users.
     * 
     * @param peer
     *            the user, which should be added in your contact list
     * @throws RemoteException
     */
    public void addBuddyGUIDone(Tester peer) throws RemoteException {
        if (!sarosBuddiesV.hasBuddy(peer.jid)) {
            sarosBuddiesV.addANewBuddyGUI(peer.jid);
            peer.sarosBuddiesV.confirmShellRequestOfSubscriptionReceived();
            sarosBuddiesV.confirmShellRequestOfSubscriptionReceived();
        }
    }

    /**
     * Remove given contact from Roster with GUI, if contact was added before.
     */
    public void deleteBuddyGUIDone(Tester peer) throws RemoteException {
        if (!sarosBuddiesV.hasBuddy(peer.jid))
            return;
        sarosBuddiesV.deleteBuddyGUI(peer.jid);
        peer.sarosBuddiesV.confirmRemovelOfSubscriptionWindow();
    }

    /**
     * Remove given contact from Roster without GUI, if contact was added before
     * 
     * @throws XMPPException
     */
    public void deleteBuddyDone(Tester peer) throws RemoteException,
        XMPPException {
        if (!sarosBuddiesV.hasBuddy(peer.jid))
            return;
        sarosBuddiesV.deleteBuddy(peer.jid);
        peer.sarosBuddiesV.confirmRemovelOfSubscriptionWindow();
    }

    public void shareYourScreenWithSelectedUserDone(Tester peer)
        throws RemoteException {
        sarosSessionV.shareYourScreenWithSelectedBuddyGUI(peer.jid);
        peer.sarosSessionV.confirmIncomingScreensharingSesionWindow();
    }

    /**
     * This method is same as
     * {@link Tester#buildSessionDoneConcurrently(String, TypeOfShareProject, TypeOfCreateProject, Tester...)}
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
    public void inviteUsersInSessionDone(final String projectName,
        final TypeOfCreateProject usingWhichProject, Tester... peers)
        throws RemoteException, InterruptedException {
        sarosSessionV.openInvitationInterface(getPeersBaseJID(peers));
        List<Callable<Void>> joinSessionTasks = new ArrayList<Callable<Void>>();
        for (final Tester tester : peers) {
            joinSessionTasks.add(new Callable<Void>() {
                public Void call() throws Exception {
                    tester.sarosC.confirmShellSessionnInvitation();
                    tester.sarosC.confirmShellAddProjectUsingWhichProject(
                        projectName, usingWhichProject);
                    return null;
                }
            });
        }
        log.trace("workAll(joinSessionTasks)");
        MakeOperationConcurrently.workAll(joinSessionTasks);
    }
}