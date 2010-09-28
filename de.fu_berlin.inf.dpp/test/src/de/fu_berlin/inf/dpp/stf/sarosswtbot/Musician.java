package de.fu_berlin.inf.dpp.stf.sarosswtbot;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import org.apache.log4j.Logger;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.util.Util;

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
        clickShareProjectWith(projectName, shareProjectWith);
        String[] inviteeJIDs = new String[invitees.length];
        for (int i = 0; i < invitees.length; i++) {
            inviteeJIDs[i] = invitees[i].getPlainJid();
        }
        bot.confirmInvitationWindow(inviteeJIDs);
        for (Musician invitee : invitees) {
            confirmSessionUsingNewOrExistProject(invitee, projectName);
        }
    }

    public void buildSessionConcurrently(String projectName,
        String shareProjectWith, Musician... invitees) throws RemoteException {

    }

    public void clickShareProjectWith(String projectName,
        String shareProjectWith) throws RemoteException {
        if (shareProjectWith.equals(SarosConstant.CONTEXT_MENU_SHARE_PROJECT)) {
            bot.clickCMShareProjectInPEView(projectName);
        } else if (shareProjectWith
            .equals(SarosConstant.CONTEXT_MENU_SHARE_PROJECT_WITH_VCS))
            bot.clickCMShareprojectWithVCSSupportInPEView(projectName);
        else if (shareProjectWith
            .equals(SarosConstant.CONTEXT_MENU_SHARE_PROJECT_PARTIALLY))
            bot.clickCMShareProjectParticallyInPEView(projectName);
        else
            bot.clickCMAddToSessionInPEView(projectName);
    }

    public void confirmSessionUsingNewOrExistProject(Musician invitee,
        String projectName) throws RemoteException {
        invitee.bot
            .waitUntilShellActive(SarosConstant.SHELL_TITLE_SESSION_INVITATION);
        switch (invitee.typeOfSharingProject) {
        case SarosConstant.CREATE_NEW_PROJECT:
            invitee.bot.confirmSessionInvitationWizard(this.getPlainJid(),
                projectName);
            break;
        case SarosConstant.USE_EXISTING_PROJECT:
            invitee.bot.confirmSessionInvitationWizardUsingExistProject(
                this.getPlainJid(), projectName);
            break;
        case SarosConstant.USE_EXISTING_PROJECT_WITH_CANCEL_LOCAL_CHANGE:
            invitee.bot
                .confirmSessionInvitationWizardUsingExistProjectWithCancelLocalChange(
                    this.getPlainJid(), projectName);
            break;
        case SarosConstant.USE_EXISTING_PROJECT_WITH_COPY:
            invitee.bot
                .confirmSessionInvitationWizardUsingExistProjectWithCopy(
                    this.getPlainJid(), projectName);
            break;
        default:
            break;
        }
    }

    public void shareScreenWithUser(Musician respondent) throws RemoteException {
        bot.openRemoteScreenView();
        if (respondent.state.isDriver(respondent.jid)) {
            bot.selectTableItemWithLabelInView(
                SarosConstant.VIEW_TITLE_SHARED_PROJECT_SESSION,
                respondent.jid.getBase() + " (Driver)");

        } else {
            bot.selectTableItemWithLabelInView(
                SarosConstant.VIEW_TITLE_SHARED_PROJECT_SESSION,
                respondent.jid.getBase());
        }
        bot.clickShareYourScreenWithSelectedUserInSPSView();
    }

    public void followUser(Musician participant) throws RemoteException {
        if (participant.state.isDriver(participant.jid))
            bot.followUser(participant.jid.getBase(), " (Driver)");
        else
            bot.followUser(participant.jid.getBase(), "");
    }

    public void leaveSession() throws RemoteException {
        // Need to check for isDriver before leaving.
        final boolean isDriver = this.state.isDriver(this.jid);
        bot.clickTBLeaveTheSessionInSPSView();
        if (!isDriver) {
            bot.confirmWindow(
                SarosConstant.SHELL_TITLE_CONFIRM_LEAVING_SESSION,
                SarosConstant.BUTTON_YES);
        } else {
            Util.runSafeAsync(log, new Runnable() {
                public void run() {
                    try {
                        bot.confirmWindow("Confirm Closing Session",
                            SarosConstant.BUTTON_YES);
                    } catch (RemoteException e) {
                        // no popup
                    }
                }
            });
            if (bot.isShellActive("Confirm Closing Session"))
                bot.confirmWindow("Confirm Closing Session",
                    SarosConstant.BUTTON_YES);
        }
        bot.waitUntilSessionCloses();
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

    /************* wait until *****************/

    public void clickCMStopfollowingThisUserInSPSView(Musician participant)
        throws RemoteException {
        if (participant.state.isDriver(participant.jid))
            bot.clickCMStopFollowingThisUserInSPSView(
                participant.jid.getBase(), " (Driver)");
        else
            bot.clickCMStopFollowingThisUserInSPSView(
                participant.jid.getBase(), "");
    }

    public void sleep(long millis) throws RemoteException {
        bot.sleep(millis);
    }

}