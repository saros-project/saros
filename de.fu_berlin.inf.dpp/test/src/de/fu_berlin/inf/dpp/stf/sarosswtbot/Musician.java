package de.fu_berlin.inf.dpp.stf.sarosswtbot;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.swt.graphics.RGB;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.ui.RosterView;

/**
 * Musician encapsulates a test instance of Saros. It takes use of all RMI
 * interfaces to help testwriters to write their STF tests nicely. STF is short
 * for Sandor's Test Framework.
 */
public class Musician {
    private static final Logger log = Logger.getLogger(Musician.class);

    protected ISarosRmiSWTWorkbenchBot bot;
    protected ISarosState state;
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
        closeWelcomeView();
        log.trace("openJavaPerspective");
        openJavaPerspective();
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
        int typeOfSharingProject) throws RemoteException {
        openPackageExplorerView();
        activatePackageExplorerView();
        clickCMShareProjectInPEView(projectName);
        confirmInvitationWindow(invitee);
        invitee
            .waitUntilShellActive(SarosConstant.SHELL_TITLE_SESSION_INVITATION);
        switch (typeOfSharingProject) {
        case SarosConstant.CREATE_NEW_PROJECT:
            invitee.confirmSessionInvitationWizard(this, projectName);
            break;
        case SarosConstant.USE_EXISTING_PROJECT:
            invitee.confirmSessionInvitationWizardUsingExistProject(this,
                projectName);
            break;
        case SarosConstant.USE_EXISTING_PROJECT_WITH_COPY:
            invitee.confirmSessionInvitationWizardUsingExistProjectWithCopy(
                this, projectName);
            break;
        default:
            break;
        }
    }

    public void setBreakPoint(int line, String projectName, String packageName,
        String className) throws RemoteException {
        bot.setBreakPoint(line, projectName, packageName, className);
    }

    public void debugJavaFile(String projectName, String packageName,
        String className) throws RemoteException {
        bot.debugJavaFile(projectName, packageName, className);
    }

    public void shareScreenWithUser(Musician respondent) throws RemoteException {
        openRemoteScreenView();
        if (respondent.isDriver()) {
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
        if (participant.isDriver())
            bot.followUser(participant.jid.getBase(), " (Driver)");
        else
            bot.followUser(participant.jid.getBase(), "");
    }

    public void giveDriverRole(Musician invitee) throws RemoteException {
        openSarosSessionView();
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
                log.trace("isShellActive");
                boolean shellActive = isShellActive(SarosConstant.SAROS_CONFI_SHELL_TITLE);
                if (shellActive) {
                    log.trace("confirmSarosConfigurationWindow");
                    bot.confirmSarosConfigurationWindow(getXmppServer(),
                        getName(), password);
                }
                waitUntilConnected();
            }
        } catch (RemoteException e) {
            log.error("can't conncet!");
        }

    }

    public void xmppDisconnect() {
        try {
            if (isConnectedByXMPP()) {
                bot.clickTBDisconnectInRosterView();
                waitUntilDisConnected();
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

    public void addContact(Musician respondent) throws RemoteException {
        bot.addContact(respondent.getPlainJid());
    }

    public void leaveSession() throws RemoteException {
        if (!this.isDriver()) {
            bot.clickTBLeaveTheSessionInSPSView();
            bot.confirmWindow(
                SarosConstant.SHELL_TITLE_CONFIRM_LEAVING_SESSION,
                SarosConstant.BUTTON_YES);
        } else {
            bot.clickTBLeaveTheSessionInSPSView();
            // if (isShellActive("Confirm Closing Session")) {
            // bot.confirmWindow("Confirm Closing Session",
            // SarosConstant.BUTTON_YES);
            // }
        }
        waitUntilSessionCloses();
    }

    public void shareProjectParallel(String projectName, List<Musician> invitees)
        throws RemoteException {
        List<String> list = new LinkedList<String>();
        for (Musician invitee : invitees)
            list.add(invitee.getPlainJid());
        bot.shareProjectParallel(projectName, list);
    }

    /*******************************************************************************
     * confirm window
     * 
     * The methods are used if Saros has broken GUI PopUps
     *******************************************************************************/

    public void confirmWindow(String title, String button) {
        try {
            bot.confirmWindow(title, button);
        } catch (RemoteException e) {
            // ignore if no window popped up
        }
    }

    public void confirmScreenShareSession() throws RemoteException {
        bot.confirmWindow("Incoming screenshare session",
            SarosConstant.BUTTON_YES);
    }

    public void confirmInvitationWindow(Musician invitee)
        throws RemoteException {
        bot.confirmWindowWithCheckBox(SarosConstant.SHELL_TITLE_INVITATION,
            SarosConstant.BUTTON_FINISH, invitee.getPlainJid());
    }

    public void confirmSessionInvitationWindowStep1(Musician inviter)
        throws RemoteException {
        // waitUntilShellActive(SarosConstant.SHELL_TITLE_SESSION_INVITATION);
        bot.confirmSessionInvitationWindowStep1(inviter.getPlainJid());
    }

    public void confirmSessionInvitationWindowStep2UsingNewproject(
        String projectName) throws RemoteException {
        bot.confirmSessionInvitationWindowStep2UsingNewproject(projectName);
    }

    public void confirmSessionInvitationWizard(Musician inviter,
        String projectname) throws RemoteException {
        confirmSessionInvitationWindowStep1(inviter);
        confirmSessionInvitationWindowStep2UsingNewproject(projectname);
    }

    public void confirmSessionInvitationWizardUsingExistProject(
        Musician inviter, String projectName) throws RemoteException {
        confirmSessionInvitationWindowStep1(inviter);
        bot.confirmSessionInvitationWindowStep2UsingExistProject(projectName);
    }

    public void confirmSessionInvitationWizardUsingExistProjectWithCopy(
        Musician inviter, String projectName) throws RemoteException {
        confirmSessionInvitationWindowStep1(inviter);
        bot.confirmSessionInvitationWindowStep2UsingExistProjectWithCopy(projectName);
    }

    public void confirmContact(Musician questioner) throws RemoteException {
        // bot.ackContactAdded(questioner.getPlainJid());
        bot.confirmWindow(
            SarosConstant.SHELL_TITLE_REQUEST_OF_SUBSCRIPTION_RECEIVED,
            SarosConstant.BUTTON_OK);
    }

    /******************** close widget ***********************/

    public void closeWelcomeView() {
        try {
            bot.closeWelcomeView();
        } catch (RemoteException e) {
            log.error("can't not close welcome view ");
        }
    }

    public void closeRosterView() {
        try {
            bot.closeRosterView();
        } catch (RemoteException e) {
            log.error("can't not close roster view ");
        }
    }

    public void closeSarosSessionView() {
        try {
            bot.closeSharedSessionView();
        } catch (RemoteException e) {
            log.error("can't not close shared project session view ");
        }
    }

    public void closeChatView() {
        try {
            bot.closeChatView();
        } catch (RemoteException e) {
            log.error("can't not close chat view ");
        }
    }

    public void closeRmoteScreenView() {
        try {
            bot.closeRemoteScreenView();
        } catch (RemoteException e) {
            log.error("can't not close remote screen view ");
        }
    }

    /***************** new widget *******************/

    public void newProjectWithClass(String projectName, String packageName,
        String className) throws RemoteException {

        if (!isJavaProjectExist(projectName)) {
            bot.newJavaProject(projectName);
        }
        if (!isJavaClassExist(projectName, packageName, className))
            bot.newJavaClass(projectName, packageName, className);
    }

    public void newJavaClassInProject(String projectName, String packageName,
        String className) throws RemoteException {
        if (!isJavaClassExist(className, projectName, packageName))
            bot.newJavaClass(projectName, packageName, className);
    }

    /**************** get ****************/

    public void getCurrentActiveShell() {
        try {
            bot.getCurrentActiveShell();
        } catch (RemoteException e) {
            log.error("Could not get the current activated Shell", e);
        }
    }

    public String getPathToScreenShot() {
        try {
            return state.getPathToScreenShot();
        } catch (RemoteException e) {
            log.error("can't not find the path of screenshot");
        }
        return null;
    }

    public String getTextOfJavaEditor(String projectName, String packageName,
        String className) throws RemoteException {
        return bot.getTextOfJavaEditor(projectName, packageName, className);
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

    public String getJavaTextOnLine(String projectName, String packageName,
        String className, int line) throws RemoteException {
        return bot.getJavaTextOnLine(projectName, packageName, className, line);
    }

    public int getJavaCursorLinePosition(String projectName,
        String packageName, String className) throws RemoteException {
        return bot.getJavaCursorLinePosition(projectName, packageName,
            className);
    }

    public RGB getJavaLineBackground(String projectName, String packageName,
        String className, int line) throws RemoteException {
        return bot.getJavaLineBackground(projectName, packageName, className,
            line);
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

    public boolean hasObserver(Musician other) throws RemoteException {
        return state.isObserver(other.jid);
    }

    public boolean hasParticipant(Musician other) throws RemoteException {
        return state.isParticipant(other.jid);
    }

    /**************** delete widget ****************/
    public void deleteContact(Musician contact) throws RemoteException {
        bot.deleteContact(contact.jid.getBase());
    }

    public void deleteProject(String projectname) throws RemoteException {
        bot.deleteProject(projectname);
    }

    /*
     * set...
     */
    public void setTextInJavaEditor(String contentPath, String projectName,
        String packageName, String className) throws RemoteException {
        // String contents = state.getContents(contentPath);
        // openJavaFileWithEditor(projectName, packageName, className);
        // activateJavaEditor(className);
        bot.setTextInJavaEditor(contentPath, projectName, packageName,
            className);
        // setTextInEditor(contents, className, "java");

    }

    // public void setTextInEditor(String contents, String fileName,
    // String extension) throws RemoteException {
    // bot.setTextinEditor(contents, fileName + "." + extension);
    // }

    /******************* open widget *************************/

    public void openPackageExplorerView() {
        try {
            bot.openPackageExplorerView();
        } catch (RemoteException e) {
            log.error("Failed to open package explorer view", e);
        }
    }

    public void openRosterView() {
        try {
            bot.openRosterView();
        } catch (RemoteException e) {
            log.error("Failed to open View Roster", e);
        }
    }

    public void openRemoteScreenView() {
        try {
            bot.openRemoteScreenView();
        } catch (RemoteException e) {
            log.error("Failed to open View Remote screen", e);
        }
    }

    public void openChatView() {
        try {
            bot.openChatView();
        } catch (RemoteException e) {
            log.error("Failed to open View chat view", e);
        }
    }

    public void openSarosSessionView() {
        try {
            bot.openSarosSessionView();
        } catch (RemoteException e) {
            log.error("Failed to open View shared projrect screen", e);
        }
    }

    /**
     * Convenient method for opening all views that are needed for tests. The
     * titles of the views are: "Roster","Shared Project Session" and
     * "Package Explorer".
     */
    public void openSarosViews() {
        openRosterView();
        openSarosSessionView();
        openChatView();
        openRemoteScreenView();
    }

    // /**
    // *
    // * @param viewTitle
    // * The title of the View. Example: "Shared Project Session"
    // * @param inode
    // * The inode of the tree on the "Show View" window. Example:
    // * "Saros"
    // * @param leaf
    // * The leaf of the tree on the "Show View" window. Example:
    // * "Saros Session"
    // *
    // * @throws RemoteException
    // */
    // public void openView(String viewTitle, String inode, String leaf)
    // throws RemoteException {
    // bot.openViewWithName(viewTitle, inode, leaf);
    // }

    public void openJavaFileWithEditor(String projectName, String packageName,
        String className) throws RemoteException {
        bot.openJavaFileWithEditor(projectName, packageName, className);
    }

    public void openJavaPerspective() throws RemoteException {
        bot.openJavaPerspective();
    }

    public void openDebugPerspective() throws RemoteException {
        bot.openDebugPerspective();
    }

    // public void openPerspectiveWithName(String nodeName) {
    // try {
    // bot.openPerspectiveWithName(nodeName);
    // } catch (RemoteException e) {
    // log.error("can't open perspective " + nodeName);
    // }
    // }

    /******************** boolean: is.. ************************/

    public boolean isJavaProjectExist(String projectName) {
        try {
            return bot.isJavaProjectExist(projectName);
        } catch (RemoteException e) {
            log.error("Could not know, if the javaproje", e);
            return false;
        }
    }

    public boolean isJavaClassExist(String projectName, String pkg,
        String className) {
        try {
            return bot.isJavaClassExist(projectName, pkg, className);
        } catch (RemoteException e) {
            log.error("Could not know, if the javaproje", e);
            return false;
        }
    }

    public boolean isShellActive(String title) throws RemoteException {
        return bot.isShellActive(title);
    }

    public boolean isJavaEditorActive(String className) throws RemoteException {

        return bot.isJavaEditorActive(className);
    }

    // public boolean isEditorActive(String fileName, String extension)
    // throws RemoteException {
    // return bot.isEditorActive(fileName + "." + extension);
    // }

    public boolean isInFollowMode(Musician participant) throws RemoteException {

        if (participant.isDriver()) {
            return bot.isInFollowMode(participant.jid.getBase(), " (Driver)");
        } else {
            return bot.isInFollowMode(participant.jid.getBase(), "");
        }
    }

    public boolean isPerspectiveActive(String title) {
        try {
            return bot.isPerspectiveActive(title);
        } catch (RemoteException e) {
            log.error("Could not know, if the perspective is active", e);
            return false;
        }
    }

    public boolean isDebugPerspectiveActive() {
        return isPerspectiveActive(SarosConstant.PERSPECTIVE_TITLE_DEBUG);
    }

    public boolean isJavaPerspectiveActive() {
        return isPerspectiveActive(SarosConstant.PERSPECTIVE_TITLE_JAVA);
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

    public boolean isDriver() throws RemoteException {
        return state.isDriver(jid);
    }

    public boolean isDriver(Musician other) throws RemoteException {
        return state.isDriver(other.jid);
    }

    public boolean isParticipant() throws RemoteException {
        return state.isParticipant(jid);
    }

    public boolean isObserver() throws RemoteException {
        return state.isObserver(jid);
    }

    public boolean isObserver(Musician other) throws RemoteException {
        return state.isObserver(other.jid);
    }

    public boolean isViewOpen(String title) throws RemoteException {
        return bot.isViewOpen(title);
    }

    public boolean isRosterViewOpen() {
        try {
            return bot.isRosterViewOpen();
        } catch (RemoteException e) {
            log.error("Failed to get the state of Saros RosterView.", e);
        }
        return false;
    }

    public boolean isRemoteScreenViewOpen() {
        try {
            return bot.isRemoteScreenViewOpen();
        } catch (RemoteException e) {
            log.error("Failed to get the state of Saros RosterView.", e);
        }
        return false;
    }

    public boolean isChatViewOpen() {
        try {
            return bot.isChatViewOpen();
        } catch (RemoteException e) {
            log.error("Failed to get the state of Saros chatview.", e);
        }
        return false;
    }

    public boolean issharedSessionViewOpen() {
        try {
            return bot.isSharedSessionViewOpen();
        } catch (RemoteException e) {
            log.error("Failed to get the state of Saros chatview.", e);
        }
        return false;
    }

    /*************** activate widget ******************/

    public void activateShellWithText(String text) {
        try {
            bot.activateShellWithText(text);
        } catch (RemoteException e) {
            log.error("Could not activate the Shell with Text " + text, e);
        }
    }

    /*
     * wenn du junitest mehrfach durchführen willst, muss vor den Test den
     * sarosInstance aktviert werden. Bei Default ist der sarosinstanze nach der
     * Durchführung von junittest deactiviert.
     */
    public void activateShellWithMatchText(String matchText) {
        try {
            bot.activateShellWithMatchText(matchText);
            log.trace("Eclipse shell is active");
        } catch (RemoteException e) {
            log.error("Could not activate shell matching " + matchText, e);
        }
    }

    public void activateEclipseShell() {
        activateShellWithMatchText(".+? - .+");
    }

    // public void activateEditor(String textName, String extension) {
    // try {
    // bot.activateEditor(textName + "." + extension);
    // } catch (RemoteException e) {
    // log.error("Could not activate the editor with name " + textName
    // + extension, e);
    // }
    // }

    public void activateJavaEditor(String className) {
        try {
            bot.activateJavaEditor(className);
        } catch (RemoteException e) {
            log.error("Could not activate the editor with name " + className, e);
        }
    }

    public void activatePackageExplorerView() {
        activateViewWithTitle(SarosConstant.VIEW_TITLE_PACKAGE_EXPLORER);
    }

    public void activateRosterView() {
        activateViewWithTitle(SarosConstant.VIEW_TITLE_ROSTER);
    }

    public void activateSharedSessionView() {
        activateViewWithTitle(SarosConstant.VIEW_TITLE_SHARED_PROJECT_SESSION);
    }

    public void activateViewWithTitle(String title) {
        try {
            bot.activateViewWithTitle(title);
        } catch (RemoteException e) {
            log.error(
                "Could not set focus on View with title '" + title + "'.", e);
        }
    }

    /************* import ********************/

    public void importProjectFromSVN(String path) throws RemoteException {
        bot.importProjectFromSVN(path);
    }

    /************* wait until *****************/

    public void waitUntilOtherLeaveSession(Musician other)
        throws RemoteException {
        while (hasParticipant(other)) {
            sleep(100);
        }
    }

    public void waitUntilIsObserverBy(Musician other) throws RemoteException {
        while (!other.hasObserver(this)) {
            sleep(100);
        }
    }

    public void waitUntilShellCloses(String title) throws RemoteException {
        bot.waitUntilShellCloses(bot.getShellWithText(title));
    }

    public void waitUntilSessionClosesBy(Musician participant)
        throws RemoteException {
        bot.waitUntilSessionCloses(participant.state);
    }

    public void waitUntilSessionCloses() throws RemoteException {
        bot.waitUntilSessionCloses();
    }

    public void waitUntilJavaEditorActive(String className)
        throws RemoteException {
        bot.waitUntilJavaEditorActive(className);
    }

    public void waitUntilSessionOpenBy(Musician participant)
        throws RemoteException {
        bot.waitUntilSessionOpenBy(participant.state);
    }

    public void waitUntilSessionOpen() throws RemoteException {
        bot.waitUntilSessionOpen();
    }

    public void waitUntilConnected() throws RemoteException {
        bot.waitUntilConnected();
    }

    public void waitUntilDisConnected() throws RemoteException {
        bot.waitUntilDisConnected();
    }

    public void waitUntilFileEqualWithFile(String projectName,
        String packageName, String className, String file)
        throws RemoteException {

        // bot.waitUntilFileEqualWithFile(projectName, packageName, className,
        // file);
        String myFile = getTextOfJavaEditor(BotConfiguration.PROJECTNAME,
            BotConfiguration.PACKAGENAME, BotConfiguration.CLASSNAME);
        while (!myFile.equals(file)) {
            sleep(100);
            myFile = getTextOfJavaEditor(BotConfiguration.PROJECTNAME,
                BotConfiguration.PACKAGENAME, BotConfiguration.CLASSNAME);
        }
    }

    public void waitUntilHasContactWith(Musician respondent)
        throws RemoteException {
        while (!hasContactWith(respondent)) {
            sleep(50);
        }
    }

    public void waitUntilHasNoContactWith(Musician respondent)
        throws RemoteException {
        while (hasContactWith(respondent)) {
            sleep(50);
        }
    }

    public void waitUntilShellActive(String title) {
        try {
            bot.waitUntilShellActive(title);

        } catch (RemoteException e) {
            log.error("Could not wait on Shell", e);
        }
    }

    /********************* click view ********************/

    public void clickCheckBox(String title) throws RemoteException {
        bot.clickCheckBox(title);
    }

    /**
     * Share given project with given invitee. click contextmenu
     */
    public void clickCMShareProjectInPEView(String project)
        throws RemoteException {
        bot.clickCMShareProjectInPEView(project);
    }

    public void clickTBLeaveTheSessionInSPSView() throws RemoteException {
        bot.clickTBLeaveTheSessionInSPSView();
    }

    public void clickTBEnableDisableFollowModeInSPSView()
        throws RemoteException {
        bot.clickEnableDisableFollowModeInSPSView();
    }

    public void clickTBRemoveAllRriverRolesInSPSView() throws RemoteException {
        bot.clickRemoveAllRriverRolesInSPSView();
    }

    public void clickNoInconsistenciesInSPSView() throws RemoteException {
        bot.clickNoInconsistenciesInSPSView();
    }

    public void clickStartAVoIPSessionInSPSView() throws RemoteException {
        bot.clickStartAVoIPSessionInSPSView();
    }

    public void clickSendAFileToSelectedUserInSPSView(Musician respondent)
        throws RemoteException {
        bot.clickSendAFileToSelectedUserInSPSView(respondent.jid.getBase());
    }

    public void clickStopSessionWithUserInSPSView(Musician respondent)
        throws RemoteException {
        bot.clickStopSessionWithUserInSPSView(respondent.jid.getName());
    }

    public void clickChangeModeOfImageSourceInSPSView() throws RemoteException {
        bot.clickChangeModeOfImageSourceInRSView();
    }

    public void clickStopRunningSessionInRSView() throws RemoteException {
        bot.clickStopRunningSessionInRSView();
    }

    public void clickResumeShareScreenInRSView() throws RemoteException {
        bot.clickResumeInRSView();
    }

    public void clickPauseShareScreenInRSView() throws RemoteException {
        bot.clickPauseInRSView();
    }

    public void clickCMJumpToPositionOfSelectedUserInSPSView(
        Musician participant) throws RemoteException {
        bot.clickCMJumpToPositionOfSelectedUserInSPSView(
            participant.jid.getBase(), "");
    }

    public void clickCMStopfollowingThisUserInSPSView(Musician participant)
        throws RemoteException {
        if (participant.isDriver())
            bot.clickCMStopFollowingThisUserInSPSView(
                participant.jid.getBase(), " (Driver)");
        else
            bot.clickCMStopFollowingThisUserInSPSView(
                participant.jid.getBase(), "");
    }

    public void clickCMgiveExclusiveDriverRoleInSPSView(Musician invitee)
        throws RemoteException {
        bot.clickCMgiveExclusiveDriverRoleInSPSView(invitee.jid.getBase());
    }

    public void clickCMRemoveDriverRoleInSPSView(Musician invitee)
        throws RemoteException {
        bot.clickCMRemoveDriverRoleInSPSView(invitee.jid.getBase());
    }

    public boolean isEditorOpen(String fileName, String extension)
        throws RemoteException {
        return bot.isEditorOpen(fileName + "." + extension);
    }

    public void captureScreenshot(String filename) {
        try {
            bot.captureScreenshot(filename);
        } catch (RemoteException e) {
            log.error("can't not capture Screenshot", e);
        }
    }

    public void sleep(long millis) throws RemoteException {
        bot.sleep(millis);
    }
}