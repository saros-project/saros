package de.fu_berlin.inf.dpp.stf.sarosswtbot;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.ui.RosterView;
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
        activateEclipseShell();
        log.trace("closeWelcomeView");
        bot.closeWelcomeView();
        log.trace("openJavaPerspective");
        bot.openJavaPerspective();
        log.trace("openSarosViews");
        openSarosViews();
        log.trace("xmppConnect");
        xmppConnect();
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

    // public boolean waitingForPermissionToAddContact(Musician respondent)
    // throws RemoteException {
    // return bot.hasContactWith(respondent.getName()
    // + " (wait for permission)");
    // }

    public void buildSession(Musician invitee, String projectName,
        String shareProjectWith, int typeOfSharingProject)
        throws RemoteException {
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

        confirmInvitationWindow(invitee);
        invitee.bot
            .waitUntilShellActive(SarosConstant.SHELL_TITLE_SESSION_INVITATION);
        switch (typeOfSharingProject) {
        case SarosConstant.CREATE_NEW_PROJECT:
            invitee.confirmSessionInvitationWizard(this, projectName);
            break;
        case SarosConstant.USE_EXISTING_PROJECT:
            invitee.confirmSessionInvitationWizardUsingExistProject(this,
                projectName);
            break;
        case SarosConstant.USE_EXISTING_PROJECT_WITH_CANCEL_LOCAL_CHANGE:
            invitee
                .confirmSessionInvitationWizardUsingExistProjectWithCancelLocalChange(
                    this, projectName);
            break;
        case SarosConstant.USE_EXISTING_PROJECT_WITH_COPY:
            invitee.confirmSessionInvitationWizardUsingExistProjectWithCopy(
                this, projectName);
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

    public void giveDriverRole(Musician invitee) throws RemoteException {
        bot.openSessionView();
        activateSharedSessionView();
        bot.clickContextMenuOfTableInView(BotConfiguration.NAME_SESSION_VIEW,
            invitee.getPlainJid(), SarosConstant.CONTEXT_MENU_GIVE_DRIVER_ROLE);
    }

    public void xmppConnect() {
        try {
            log.trace("connectedByXMPP");
            boolean connectedByXMPP = isConnectedByXMPP();
            if (!connectedByXMPP) {
                log.trace("clickTBConnectInRosterView");
                bot.clickTBConnectInRosterView();
                bot.sleep(100);// wait a bit to check if shell pops up
                log.trace("isShellActive");
                boolean shellActive = bot
                    .isShellActive(SarosConstant.SAROS_CONFI_SHELL_TITLE);
                if (shellActive) {
                    log.trace("confirmSarosConfigurationWindow");
                    bot.confirmSarosConfigurationWindow(getXmppServer(),
                        getName(), password);
                }
                bot.waitUntilConnected();
            }
        } catch (RemoteException e) {
            log.error("can't connect!");
        }

    }

    public void xmppDisconnect() {
        try {
            if (isConnectedByXMPP()) {
                bot.clickTBDisconnectInRosterView();
                bot.waitUntilDisConnected();
            }
        } catch (RemoteException e) {
            // ignore if no window popped up
        }

    }

    public void creatNewAccount(String server, String username, String password)
        throws RemoteException {
        bot.clickMenuWithTexts("Saros", "Create Account");
        bot.confirmCreateNewUserAccountWindow(server, username, password);
    }

    public void inviteUser(Musician invitee, String projectName)
        throws RemoteException {
        bot.clickTBOpenInvitationInterfaceInSPSView();
        bot.waitUntilShellActive("Invitation");
        bot.confirmWindowWithCheckBox("Invitation",
            SarosConstant.BUTTON_FINISH, invitee.getPlainJid());
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

    public void shareProjectParallel(String projectName, List<Musician> invitees)
        throws RemoteException {
        List<String> list = new LinkedList<String>();
        for (Musician invitee : invitees)
            list.add(invitee.getPlainJid());
        bot.shareProjectParallel(projectName, list);
    }

    public void confirmScreenShareSession() throws RemoteException {
        bot.confirmWindow("Incoming screenshare session",
            SarosConstant.BUTTON_YES);
    }

    public void confirmInvitationWindow(Musician invitee)
        throws RemoteException {
        bot.waitUntilShellActive(SarosConstant.SHELL_TITLE_INVITATION);
        bot.confirmWindowWithCheckBox(SarosConstant.SHELL_TITLE_INVITATION,
            SarosConstant.BUTTON_FINISH, invitee.getPlainJid());
    }

    public void confirmSessionInvitationWizard(Musician inviter,
        String projectname) throws RemoteException {
        // waitUntilShellActive(SarosConstant.SHELL_TITLE_SESSION_INVITATION);
        bot.confirmSessionInvitationWindowStep1(inviter.getPlainJid());
        bot.confirmSessionInvitationWindowStep2UsingNewproject(projectname);
    }

    public void confirmSessionInvitationWizardUsingExistProject(
        Musician inviter, String projectName) throws RemoteException {
        // waitUntilShellActive(SarosConstant.SHELL_TITLE_SESSION_INVITATION);
        bot.confirmSessionInvitationWindowStep1(inviter.getPlainJid());
        bot.confirmSessionInvitationWindowStep2UsingExistProject(projectName);
    }

    public void confirmSessionInvitationWizardUsingExistProjectWithCancelLocalChange(
        Musician inviter, String projectName) throws RemoteException {
        // waitUntilShellActive(SarosConstant.SHELL_TITLE_SESSION_INVITATION);
        bot.confirmSessionInvitationWindowStep1(inviter.getPlainJid());
        bot.confirmSessionInvitationWindowStep2UsingExistProjectWithCancelLocalChange(projectName);

    }

    public void confirmSessionInvitationWizardUsingExistProjectWithCopy(
        Musician inviter, String projectName) throws RemoteException {
        // waitUntilShellActive(SarosConstant.SHELL_TITLE_SESSION_INVITATION);
        bot.confirmSessionInvitationWindowStep1(inviter.getPlainJid());
        bot.confirmSessionInvitationWindowStep2UsingExistProjectWithCopy(projectName);
    }

    public void confirmContact(Musician questioner) throws RemoteException {
        // bot.ackContactAdded(questioner.getPlainJid());
        bot.waitUntilShellActive(SarosConstant.SHELL_TITLE_REQUEST_OF_SUBSCRIPTION_RECEIVED);
        bot.confirmWindow(
            SarosConstant.SHELL_TITLE_REQUEST_OF_SUBSCRIPTION_RECEIVED,
            SarosConstant.BUTTON_OK);
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

    /************* has *************/

    public boolean hasContactWith(Musician respondent) {
        try {
            return state.hasContactWith(respondent.jid)
                && bot.hasContactWith(respondent.jid.getBase());
        } catch (RemoteException e) {
            log.error("Failed to check if the contact was found", e);
        }
        return false;
    }

    /**
     * Convenient method for opening all views that are needed for tests. The
     * titles of the views are: "Roster","Shared Project Session" and
     * "Package Explorer".
     */
    public void openSarosViews() throws RemoteException {
        bot.openRosterView();
        bot.openSessionView();
        bot.openChatView();
        bot.openRemoteScreenView();
    }

    public boolean isInFollowMode(Musician participant) throws RemoteException {

        if (participant.state.isDriver(participant.jid)) {
            return bot.isInFollowMode(participant.jid.getBase(), " (Driver)");
        } else {
            return bot.isInFollowMode(participant.jid.getBase(), "");
        }
    }

    public boolean isDebugPerspectiveActive() throws RemoteException {
        return bot.isPerspectiveActive(SarosConstant.PERSPECTIVE_TITLE_DEBUG);
    }

    public boolean isJavaPerspectiveActive() throws RemoteException {
        return bot.isPerspectiveActive(SarosConstant.PERSPECTIVE_TITLE_JAVA);
    }

    /**
     * This method returns true if {@link SarosState} and the GUI
     * {@link RosterView} having the connected state.
     */
    public boolean isConnectedByXMPP() {
        try {
            return state.isConnectedByXMPP() && bot.isConnectedByXmppGuiCheck();
        } catch (RemoteException e) {
            log.error("Failed to get the xmpp connection state.", e);
        }
        return false;
    }

    /*
     * wenn du junitest mehrfach durchführen willst, muss vor den Test den
     * sarosInstance aktviert werden. Bei Default ist der sarosinstanze nach der
     * Durchführung von junittest deactiviert.
     */
    public boolean activateShellWithMatchText(String matchText) {
        try {
            if (bot.activateShellWithMatchText(matchText)) {
                log.trace("Eclipse shell is active");
                return true;
            } else {
                log.error("Could not activate shell matching " + matchText);
            }
        } catch (RemoteException e) {
            log.error("Could not activate shell matching " + matchText, e);
            //
        }
        return false;
    }

    public boolean activateEclipseShell() {
        return activateShellWithMatchText(".+? - .+");
    }

    // public void activateEditor(String textName, String extension) {
    // try {
    // bot.activateEditor(textName + "." + extension);
    // } catch (RemoteException e) {
    // log.error("Could not activate the editor with name " + textName
    // + extension, e);
    // }
    // }

    public void activatePackageExplorerView() throws RemoteException {
        bot.activateViewWithTitle(SarosConstant.VIEW_TITLE_PACKAGE_EXPLORER);
    }

    public void activateRosterView() throws RemoteException {
        bot.activateViewWithTitle(SarosConstant.VIEW_TITLE_ROSTER);
    }

    public void activateSharedSessionView() throws RemoteException {
        bot.activateViewWithTitle(SarosConstant.VIEW_TITLE_SHARED_PROJECT_SESSION);
    }

    /************* wait until *****************/

    public void waitUntilOtherLeaveSession(Musician other)
        throws RemoteException {
        while (state.isParticipant(other.jid)) {
            sleep(100);
        }
    }

    public void waitUntilOtherInSession(Musician other) throws RemoteException {
        int time = 0;
        while (!state.isParticipant(other.jid) && time < 2000) {
            sleep(100);
            time = time + 100;
        }
    }

    public void waitUntilIsObserverBy(Musician other) throws RemoteException {
        while (!other.state.isObserver(this.jid)) {
            sleep(100);
        }
    }

    public void waitUntilFileEqualWithFile(String projectName,
        String packageName, String className, String file)
        throws RemoteException {

        // bot.waitUntilFileEqualWithFile(projectName, packageName, className,
        // file);
        String myFile = bot.getTextOfJavaEditor(BotConfiguration.PROJECTNAME,
            BotConfiguration.PACKAGENAME, BotConfiguration.CLASSNAME);
        while (!myFile.equals(file)) {
            sleep(100);
            myFile = bot.getTextOfJavaEditor(BotConfiguration.PROJECTNAME,
                BotConfiguration.PACKAGENAME, BotConfiguration.CLASSNAME);
        }
    }

    public void waitUntilHasContactWith(Musician respondent)
        throws RemoteException {
        // FIXME infinite loop, replace with wait condition
        // while (!hasContactWith(respondent)) {
        // sleep(50);
        if (!hasContactWith(respondent)) {
            sleep(500);
        }
    }

    public void waitUntilHasNoContactWith(Musician respondent)
        throws RemoteException {
        // FIXME infinite loop, replace with wait condition
        // while (hasContactWith(respondent)) {
        // sleep(50);
        if (hasContactWith(respondent)) {
            sleep(500);
        }
    }

    public void clickCMJumpToPositionOfSelectedUserInSPSView(
        Musician participant) throws RemoteException {
        bot.clickCMJumpToPositionOfSelectedUserInSPSView(
            participant.jid.getBase(), "");
    }

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