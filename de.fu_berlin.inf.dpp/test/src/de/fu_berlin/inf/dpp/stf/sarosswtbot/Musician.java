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

/**
 * Musician encapsulates a test instance of Saros. It takes use of all RMI
 * interfaces to help testwriters to write their STF tests nicely. STF is short
 * for Sandor's Test Framework.
 */
public class Musician {
    private static final transient Logger log = Logger
        .getLogger(Musician.class);

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
        log.trace("closeViewByTitle");
        closeViewWithText(SarosConstant.VIEW_TITLE_WELCOME);
        openPerspectiveWithName(SarosConstant.PERSPECTIVE_TITLE_JAVA);
        openSarosViews();
        xmppConnect();
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
        String NameOfContextMenu, int typeOfSharingProject)
        throws RemoteException {
        openJavaPackageExplorerView();
        activatePackageExplorerView();
        clickCMShareProjectInPEView(projectName, NameOfContextMenu);
        confirmInvitationWindow(invitee);
        invitee
            .waitUntilShellActive(SarosConstant.SHELL_TITLE_SESSION_INVITATION);
        switch (typeOfSharingProject) {
        case SarosConstant.CREATE_NEW_PROJECT:
            invitee.ackProject(this, projectName);
            break;
        case SarosConstant.USE_EXISTING_PROJECT:
            invitee.ackProjectUsingExistProject(this, projectName);
            break;
        case SarosConstant.USE_EXISTING_PROJECT_WITH_COPY:
            invitee.ackProjectUsingExistProjectWithCopy(this, projectName);
            break;
        default:
            break;
        }
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
            openRosterView();
            activateRosterView();
            if (!isConnectedByXMPP())
                bot.clickTBConnectInSPSView();
            if (isShellActive(SarosConstant.SAROS_CONFI_SHELL_TITLE)) {
                bot.confirmSarosConfigurationWindow(getXmppServer(), getName(),
                    password);
            }
            waitUntilConnect();
        } catch (RemoteException e) {
            log.error("can't conncet!");
        }

    }

    public void xmppDisconnect() {
        try {
            openRosterView();
            activateRosterView();
            if (isConnectedByXMPP())
                bot.clickTBDisconnectInSPSView();
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
        openSarosSessionView();
        activateSharedSessionView();
        bot.clickToolbarPushButtonWithTooltipInView(
            SarosConstant.VIEW_TITLE_SHARED_PROJECT_SESSION,
            SarosConstant.TOOL_TIP_TEXT_OPEN_INVITATION_INTERFACE);
        bot.waitUntilShellActive("Invitation");
        bot.confirmWindowWithCheckBox("Invitation",
            SarosConstant.BUTTON_FINISH, invitee.getPlainJid());
    }

    public void addContact(Musician respondent) throws RemoteException {
        bot.addContact(respondent.getPlainJid());
    }

    public void leaveSession() throws RemoteException {
        if (!this.isDriver()) {
            bot.leaveSession();
            bot.confirmWindow(
                SarosConstant.SHELL_TITLE_CONFIRM_LEAVING_SESSION,
                SarosConstant.BUTTON_YES);
        } else {
            bot.leaveSession();
            // if (isShellActive("Confirm Closing Session")) {
            // bot.confirmWindow("Confirm Closing Session",
            // SarosConstant.BUTTON_YES);
            // }
        }
        waitUntilSessionCloses();
    }

    public void shareProjectParallel(String projectName, List<Musician> invitees)
        throws RemoteException {
        if (isViewOpen(SarosConstant.VIEW_TITLE_PACKAGE_EXPLORER))
            openPerspectiveWithName(SarosConstant.PERSPECTIVE_TITLE_JAVA);
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

    /******************* ack ********************/
    public void ackProject(Musician inviter, String projectname)
        throws RemoteException {
        confirmSessionInvitationWindowStep1(inviter);
        confirmSessionInvitationWindowStep2UsingNewproject(projectname);
    }

    public void ackProjectUsingExistProject(Musician inviter, String projectName)
        throws RemoteException {
        confirmSessionInvitationWindowStep1(inviter);
        bot.confirmSessionInvitationWindowStep2UsingExistProject(projectName);
    }

    public void ackProjectUsingExistProjectWithCopy(Musician inviter,
        String projectName) throws RemoteException {
        confirmSessionInvitationWindowStep1(inviter);
        bot.confirmSessionInvitationWindowStep2UsingExistProjectWithCopy(projectName);
    }

    public void ackContact(Musician questioner) throws RemoteException {
        // bot.ackContactAdded(questioner.getPlainJid());
        bot.confirmWindow(
            SarosConstant.SHELL_TITLE_REQUEST_OF_SUBSCRIPTION_RECEIVED,
            SarosConstant.BUTTON_OK);
    }

    /******************** close widget ***********************/

    public void closeViewWithText(String title) {
        try {
            bot.closeViewWithText(title);
        } catch (RemoteException e) {
            log.error("can't not close view + " + title);
        }
    }

    public void closeRosterView() {
        closeViewWithText(SarosConstant.VIEW_TITLE_ROSTER);
    }

    public void closeSarosSessionView() {
        closeViewWithText(SarosConstant.VIEW_TITLE_SHARED_PROJECT_SESSION);
    }

    public void closeChatView() {
        closeViewWithText(SarosConstant.VIEW_TITLE_CHAT_VIEW);
    }

    public void closeRmoteScreenView() {
        closeViewWithText(SarosConstant.VIEW_TITLE_REMOTE_SCREEN);
    }

    /***************** new widget *******************/

    public void newProjectWithClass(String projectName, String packageName,
        String className) throws RemoteException {
        openJavaPackageExplorerView();
        activatePackageExplorerView();
        if (!isJavaProjectExist(projectName)) {
            bot.newJavaProject(projectName);
            bot.newJavaClass(projectName, packageName, className);
        }
    }

    public void newJavaClassInProject(String projectName, String packageName,
        String className) throws RemoteException {
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
        if (!isEditorOpen(className, "java"))
            openJavaFileWithEditor(projectName, packageName, className);
        activateJavaEditor(className);
        return bot.getTextOfJavaEditor(projectName, packageName, className
            + ".java");
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

    public boolean hasParticipant(Musician other) throws RemoteException {
        return state.isParticipant(other.jid);
    }

    /**************** delete widget ****************/
    public void deleteContact(Musician contact) throws RemoteException {
        bot.deleteContact(contact.jid.getBase());
    }

    public void deleteResource(String projectname) throws RemoteException {
        bot.deleteResource(projectname);
    }

    /*
     * set...
     */
    public void setTextInJavaEditor(String contentPath, String projectName,
        String packageName, String className) throws RemoteException {
        String contents = state.getContents(contentPath);
        openJavaFileWithEditor(projectName, packageName, className);
        activateJavaEditor(className);
        setTextInEditor(contents, className, "java");
    }

    public void setTextInEditor(String contents, String fileName,
        String extension) throws RemoteException {
        bot.setTextinEditor(contents, fileName + "." + extension);
    }

    /******************* open widget *************************/

    public void openJavaPackageExplorerView() {
        try {
            bot.openJavaPackageExplorerView();
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
     * 
     * @throws RemoteException
     */
    public void openSarosViews() {
        openRosterView();
        openSarosSessionView();
        openChatView();
        openRemoteScreenView();
    }

    /**
     * 
     * @param viewTitle
     *            The title of the View. Example: "Shared Project Session"
     * @param inode
     *            The inode of the tree on the "Show View" window. Example:
     *            "Saros"
     * @param leaf
     *            The leaf of the tree on the "Show View" window. Example:
     *            "Saros Session"
     * 
     * @throws RemoteException
     */
    public void openView(String viewTitle, String inode, String leaf)
        throws RemoteException {
        bot.openViewWithName(viewTitle, inode, leaf);
    }

    public void openJavaFileWithEditor(String projectName, String packageName,
        String className) throws RemoteException {
        bot.openJavaFileWithEditor(projectName, packageName, className
            + ".java");
    }

    public void openPerspectiveWithName(String nodeName) {
        try {
            bot.openPerspectiveWithName(nodeName);
        } catch (RemoteException e) {
            log.error("can't open perspective " + nodeName);
        }
    }

    /******************** boolean: is.. ************************/

    public boolean isJavaProjectExist(String projectName) {
        try {
            return bot.isProjectInViewExisted(
                SarosConstant.VIEW_TITLE_PACKAGE_EXPLORER, projectName);
        } catch (RemoteException e) {
            log.error("Could not know, if the javaproje", e);
            return false;
        }
    }

    public boolean isShellActive(String title) throws RemoteException {
        return bot.isShellActive(title);
    }

    public boolean isJavaEditorActive(String className) throws RemoteException {
        return isEditorActive(className, "java");
    }

    public boolean isEditorActive(String fileName, String extension)
        throws RemoteException {
        return bot.isEditorActive(fileName + "." + extension);
    }

    public boolean isInFollowMode(Musician participant) throws RemoteException {
        if (participant.isDriver()) {
            return bot.isInFollowMode(participant.jid.getBase(), " (Driver)");
        } else {
            return bot.isInFollowMode(participant.jid.getBase(), "");
        }
    }

    public boolean isPerspectiveOpen(String title) throws RemoteException {
        return bot.isPerspectiveActive(title);
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

    public void activateShellWithMatchText(String matchText) {
        try {
            bot.activateShellWithMatchText(matchText);
            log.trace("eclipseShell is actived");
        } catch (RemoteException e) {
            log.error("Could not activate the Shell with matchText "
                + matchText, e);
        }
    }

    public void activateEclipseShell() {
        activateShellWithMatchText(".+? - .+");
    }

    public void activateEditor(String textName, String extension) {
        try {
            bot.activateEditor(textName + "." + extension);
        } catch (RemoteException e) {
            log.error("Could not activate the editor with name " + textName
                + extension, e);
        }
    }

    public void activateJavaEditor(String className) {
        activateEditor(className, "java");
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
        waitUntilEditorActive(className, "java");
    }

    public void waitUntilEditorActive(String fileName, String extension)
        throws RemoteException {
        bot.waitUntilEditorActive(fileName + "." + extension);
    }

    public void waitUntilConnect() throws RemoteException {
        bot.waitUntilConnected();
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
    public void clickCMShareProjectInPEView(String project,
        String nameOfContextMenu) throws RemoteException {
        bot.clickCMShareProjectInPEView(project, nameOfContextMenu);
    }

    public void clickLeaveTheSessionInSPSView() throws RemoteException {
        bot.clickLeaveTheSessionInSPSView();
    }

    public void clickEnableDisableFollowModeInSPSView() throws RemoteException {
        bot.clickEnableDisableFollowModeInSPSView();
    }

    public void clickRemoveAllRriverRolesInSPSView() throws RemoteException {
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