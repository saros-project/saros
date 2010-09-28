package de.fu_berlin.inf.dpp.stf.sarosswtbot;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import org.apache.log4j.Logger;

import de.fu_berlin.inf.dpp.net.JID;

/**
 * Musician encapsulates a test instance of Saros. It takes use of all RMI
 * interfaces to help testwriters to write their STF tests nicely. STF is short
 * for Sandor's Test Framework.
 */
public class Musician {
    private static final Logger log = Logger.getLogger(Musician.class);

    public ISarosRmiSWTWorkbenchBot bot;
    public ISarosState state;
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
    }

    /*************** Component, which consist of other simple functions ******************/

    public void buildSessionSequential(String projectName,
        String shareProjectWith, Musician... invitees) throws RemoteException {
        String[] inviteeJIDs = new String[invitees.length];
        for (int i = 0; i < invitees.length; i++) {
            inviteeJIDs[i] = invitees[i].getPlainJid();
        }
        bot.clickShareProjectWith(projectName, shareProjectWith);

        bot.confirmInvitationWindow(inviteeJIDs);
        for (Musician invitee : invitees) {
            bot.confirmSessionUsingNewOrExistProject(invitee.bot, this.jid,
                projectName, invitee.typeOfSharingProject);
        }
    }

    public void buildSessionConcurrently(String projectName,
        String shareProjectWith, Musician... invitees) throws RemoteException {
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