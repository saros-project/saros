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
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.saros.ISarosRmiSWTWorkbenchBot;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.saros.noGUI.ISarosState;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.saros.pages.IPopUpWindowObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.saros.pages.IRosterViewObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.saros.pages.ISessionViewObject;

/**
 * Musician encapsulates a test instance of Saros. It takes use of all RMI
 * interfaces to help testwriters to write their STF tests nicely. STF is short
 * for Sandor's Test Framework.
 */
public class Musician {
    private static final Logger log = Logger.getLogger(Musician.class);

    public ISarosRmiSWTWorkbenchBot bot;
    public ISarosState state;
    public IRosterViewObject rosterV;
    public IPopUpWindowObject popupWindow;
    public ISessionViewObject sessionV;
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
        bot.activateEclipseShell();
        log.trace("closeWelcomeView");
        bot.closeWelcomeView();
        log.trace("openJavaPerspective");
        bot.openPerspectiveJava();
        log.trace("openSarosViews");
        bot.openSarosViews();
        log.trace("xmppConnect");
        bot.xmppConnect(jid, password);
        log.trace("initBot leave");
    }

    public void initRmi() throws RemoteException, NotBoundException,
        AccessException {
        Registry registry = LocateRegistry.getRegistry(host, port);
        try {
            bot = (ISarosRmiSWTWorkbenchBot) registry.lookup("Bot");
        } catch (java.rmi.ConnectException e) {
            throw new RuntimeException("Could not connect to RMI of bot " + jid
                + ", did you start the Eclipse instance?");
        }

        state = (ISarosState) registry.lookup("state");
        rosterV = (IRosterViewObject) registry.lookup("rosterView");
        popupWindow = (IPopUpWindowObject) registry.lookup("popUpWindow");
        sessionV = (ISessionViewObject) registry.lookup("sessionView");
    }

    /*************** Component, which consist of other simple functions ******************/

    public void buildSessionSequential(String projectName,
        String shareProjectWith, Musician... invitees) throws RemoteException {
        String[] inviteeJIDs = new String[invitees.length];
        for (int i = 0; i < invitees.length; i++) {
            inviteeJIDs[i] = invitees[i].getPlainJid();
        }
        bot.clickShareProjectWith(projectName, shareProjectWith);

        popupWindow.confirmInvitationWindow(inviteeJIDs);
        for (Musician invitee : invitees) {
            bot.confirmSessionUsingNewOrExistProject(invitee.bot, this.jid,
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
            peersName.add(invitee.getPlainJid());
        }

        log.trace("alice.shareProjectParallel");
        this.bot.shareProject(BotConfiguration.PROJECTNAME, peersName);

        List<Callable<Void>> joinSessionTasks = new ArrayList<Callable<Void>>();
        for (int i = 0; i < peers.size(); i++) {
            final Musician musician = peers.get(i);
            joinSessionTasks.add(new Callable<Void>() {
                public Void call() throws Exception {
                    musician.popupWindow.confirmSessionInvitationWizard(
                        getPlainJid(), BotConfiguration.PROJECTNAME);
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
        sessionV.clickTBLeaveTheSessionInSPSView();
        bot.confirmWindow("Confirm Closing Session", SarosConstant.BUTTON_YES);
        bot.waitUntilSessionCloses();
        List<Callable<Void>> closeSessionTasks = new ArrayList<Callable<Void>>();
        for (int i = 0; i < musicians.length; i++) {
            final Musician musician = musicians[i];
            closeSessionTasks.add(new Callable<Void>() {
                public Void call() throws Exception {
                    // Need to check for isDriver before leaving.
                    musician.bot.waitUntilShellActive("Closing the Session");
                    musician.bot.confirmWindow("Closing the Session",
                        SarosConstant.BUTTON_OK);
                    musician.bot.waitUntilShellCloses("Closing the Session");
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
                    musician.bot.leaveSessionByPeer();
                    return null;
                }
            });
        }
        List<JID> peerJIDs = new ArrayList<JID>();
        for (Musician musician : musicians) {
            peerJIDs.add(musician.jid);
        }
        MakeOperationConcurrently.workAll(leaveTasks, leaveTasks.size());
        bot.waitUntilAllPeersLeaveSession(peerJIDs);
        sessionV.clickTBLeaveTheSessionInSPSView();
        bot.waitUntilSessionCloses();
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
                    musician.bot.followUser(state, jid);
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
                    musician.bot.stopFollowUser(state, jid);
                    return null;
                }
            });
        }
        MakeOperationConcurrently.workAll(stopFollowTasks,
            stopFollowTasks.size());
    }

    public String getName() {
        return jid.getName();
    }

    /**
     * Returns the plain {@link JID}.
     */
    public String getPlainJid() {
        return jid.getBase();
    }

    /**
     * Returns the resource qualified {@link JID}.
     */
    public String getRQjid() {
        return jid.toString();
    }

    public String getXmppServer() {
        return jid.getDomain();
    }

}