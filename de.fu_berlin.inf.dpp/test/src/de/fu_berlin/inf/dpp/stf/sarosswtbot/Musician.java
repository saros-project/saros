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

    public void initBot() throws AccessException, RemoteException,
        NotBoundException {
        initRmi();
        activeMusican();
        closeViewByTitle(SarosConstant.VIEW_TITLE_WELCOME);
        openPerspective(SarosConstant.PERSPECTIVE_TITLE_JAVA);
        openSarosViews();
        xmppConnect();
    }

    public void buildSession(Musician invitee, String projectName,
        String NameOfContextMenu, int typeOfSharingProject)
        throws RemoteException {
        this.shareProject(invitee, projectName, NameOfContextMenu);
        invitee.waitUntilShellActive("Session Invitation");
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

    // public void ackProject1(Musician inviter, String projectname)
    // throws RemoteException {
    //
    // bot.ackProjectStep1(inviter.getPlainJid());
    //
    // }
    //
    // public void ackProject2(Musician inviter, String projectname)
    // throws RemoteException {
    //
    // bot.ackProjectStep1(inviter.getPlainJid());
    //
    // }

    public void ackProject(Musician inviter, String projectname)
        throws RemoteException {

        bot.ackProjectStep1(inviter.getPlainJid());
        bot.ackProjectStep2UsingNewProject(projectname);
    }

    public void ackProjectStep1(Musician inviter) throws RemoteException {
        waitUntilShellActive(SarosConstant.SHELL_TITLE_SESSION_INVITATION);
        bot.ackProjectStep1(inviter.getPlainJid());
    }

    public void ackProjectStep2UsingNewproject(Musician inviter,
        String projectName) throws RemoteException {
        bot.ackProjectStep2UsingNewProject(projectName);
    }

    public void ackProjectUsingExistProject(Musician inviter, String projectName)
        throws RemoteException {
        bot.ackProjectStep1(inviter.getPlainJid());
        bot.ackProjectStep2UsingExistProject(projectName);
    }

    public void ackProjectUsingExistProjectWithCopy(Musician inviter,
        String projectName) throws RemoteException {
        bot.ackProjectStep1(inviter.getPlainJid());
        bot.ackProjectStep2UsingExistProjectWithCopy(projectName);
    }

    public void ackContact(Musician questioner) throws RemoteException {
        // bot.ackContactAdded(questioner.getPlainJid());
        bot.confirmWindow(
            SarosConstant.SHELL_TITLE_REQUEST_OF_SUBSCRIPTION_RECEIVED,
            SarosConstant.BUTTON_OK);
    }

    public void activateShell() {
        try {
            bot.getCurrentActiveShell();

        } catch (RemoteException e) {
            log.error("Could not activate Shell", e);
        }
    }

    public void addContact(Musician respondent) throws RemoteException {
        bot.addNewContact(respondent.jid.getBase());
    }

    /**
     * Add invitee to an existing shared project.
     */
    public void addToSharedProject(Musician invitee) throws RemoteException {
        bot.addToSharedProject(invitee.getPlainJid());
    }

    public void captureScreenshot(String filename) throws RemoteException {
        bot.captureScreenshot(filename);
    }

    public void closeViewByTitle(String title) {
        try {
            if (isViewOpen(title))
                bot.closeViewByTitle(title);
        } catch (RemoteException e) {
            log.error("View with title '" + title + "' could not be closed", e);
        }
    }

    public void createProjectWithClass(String projectName, String packageName,
        String className) throws RemoteException {
        if (!bot.isProjectInWorkspacePackageExplorer(projectName)) {
            bot.newJavaProject(projectName);
            bot.newJavaClass(projectName, packageName, className);
        }
    }

    public void createJavaClassInProject(String projectName,
        String packageName, String className) throws RemoteException {
        bot.newJavaClass(projectName, packageName, className);
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

    public boolean hasContact(Musician respondent) {
        try {
            return state.hasContact(respondent.jid)
                && bot.isContactInRosterView(respondent.jid.getBase());
        } catch (RemoteException e) {
            log.error("Failed to check if the contact was found", e);
        }
        return false;
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

    public boolean isParticipant(Musician other) throws RemoteException {
        return state.isParticipant(other.jid);
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
            return bot.isViewOpen("Roster");
        } catch (RemoteException e) {
            log.error("Failed to get the state of Saros RosterView.", e);
        }
        return false;
    }

    public void leave(boolean confirmation) throws RemoteException {
        bot.leaveSession();
        if (confirmation)
            bot.confirmWindow(
                SarosConstant.SHELL_TITLE_CONFIRM_LEAVING_SESSION,
                SarosConstant.BUTTON_YES);
    }

    public void openJavaPackageExplorerView() throws RemoteException {
        bot.openJavaPackageExplorerView();
    }

    public void openRosterView() {
        try {
            if (!isViewOpen(SarosConstant.VIEW_TITLE_ROSTER))
                openView(SarosConstant.VIEW_TITLE_ROSTER,
                    SarosConstant.INNODE_SAROS, SarosConstant.LEAF_ROSTER);
        } catch (RemoteException e) {
            log.error("Failed to open View Roster", e);
        }
    }

    public void openChatView() throws RemoteException {
        openView("Chat View", "Saros", "Chat View");

    }

    /**
     * Convenient method for opening all views that are needed for tests. The
     * titles of the views are: "Roster","Shared Project Session" and
     * "Package Explorer".
     * 
     * @throws RemoteException
     */
    public void openSarosViews() throws RemoteException {
        openView("Roster", "Saros", "Roster");
        openView("Shared Project Session", "Saros", "Saros Session");
        openView("Package Explorer", "Java", "Package Explorer");
    }

    public void openSessionView() throws RemoteException {
        openView("Shared Project Session", "Saros", "Saros Session");
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
        if (!isViewOpen(viewTitle)) {
            bot.openViewByName(inode, leaf);
        }

    }

    public void removeContact(Musician contact) throws RemoteException {
        bot.removeContact(contact.jid.getBase());
    }

    public void removeProject(String projectname) throws RemoteException {
        bot.removeProject(projectname);
    }

    public void setFocusOnViewByTitle(String title) {
        try {
            bot.setFocusOnViewByTitle(title);
        } catch (RemoteException e) {
            log.error(
                "Could not set focus on View with title '" + title + "'.", e);
        }
    }

    /**
     * Share given project with given invitee.
     */
    public void shareProject(Musician invitee, String project,
        String nameOfContextMenu) throws RemoteException {
        if (!bot.isViewOpen("Package Explorer"))
            bot.openViewByName("Java", "Package Explorer");
        bot.shareProject(project, nameOfContextMenu, invitee.getPlainJid());
    }

    public void inviteUser(Musician invitee, String projectName)
        throws RemoteException {

        bot.clickToolbarButtonWithTooltipInViewWithTitle(
            SarosConstant.VIEW_TITLE_SHARED_PROJECT_SESSION,
            SarosConstant.TOOL_TIP_TEXT_OPEN_INVITATION_INTERFACE);
        bot.selectCheckBoxInvitation(invitee.getPlainJid());
        bot.clickButton(SarosConstant.BUTTON_FINISH);

    }

    public void shareProjectParallel(String projectName, List<Musician> invitees)
        throws RemoteException {
        if (isViewOpen(SarosConstant.VIEW_TITLE_PACKAGE_EXPLORER))
            openPerspective(SarosConstant.PERSPECTIVE_TITLE_JAVA);
        // bot.openViewByName("Java", "Package Explorer");

        List<String> list = new LinkedList<String>();
        for (Musician invitee : invitees)
            list.add(invitee.getPlainJid());
        bot.shareProjectParallel(projectName, list);
    }

    public void sleep(long millis) throws RemoteException {
        bot.sleep(millis);
    }

    public void waitUntilShellActive(String title) {
        try {
            bot.waitUntilShellActive(title);

        } catch (RemoteException e) {
            log.error("Could not wait on Shell", e);
        }
    }

    public boolean waitingForPermissionToAddContact(Musician respondent)
        throws RemoteException {
        return bot.isContactInRosterView(respondent.getName()
            + " (wait for permission)");
    }

    public void xmppConnect() throws RemoteException {
        openRosterView();
        if (!isConnectedByXMPP())
            bot.xmppConnect();
        if (isShellOpenByTitle(SarosConstant.SAROS_CONFI_SHELL_TITLE)) {
            bot.doSarosConfiguration(getXmppServer(), jid.getName(), password);
        }
    }

    public void xmppDisconnect() throws RemoteException {
        if (isConnectedByXMPP())
            bot.xmppDisconnect();
    }

    /**
     * This method is used if Saros has broken GUI PopUps
     */
    public void clickButtonOnPopup(String title, String button) {
        try {
            bot.confirmWindow(title, button);
        } catch (RemoteException e) {
            // ignore if no window popped up
        }

    }

    /**
     * Lin
     */
    public void activeMusican() throws RemoteException {
        // if (System.getProperty("os.name", "Unknown OS").equals("Mac OS X"))
        bot.activeMusician();
    }

    public void waitForConnect() throws RemoteException {
        bot.waitUntilConnect();
    }

    public boolean isPerspectiveOpen(String title) throws RemoteException {
        return bot.isPerspectiveOpen(title);
    }

    public void openPerspective(String nodeName) {
        try {
            if (!isPerspectiveOpen(nodeName)) {
                bot.openPerspectiveByName(nodeName);
            }
        } catch (RemoteException e) {
            log.error("can't open perspective " + nodeName);
        }

    }

    public void setTextInClass(String contentPath, String projectName,
        String packageName, String className) throws RemoteException {
        String contents = state.getContents(contentPath);
        bot.setTextInClass(contents, projectName, packageName, className);
    }

    public String getTextOfClass(String projectName, String packageName,
        String className) throws RemoteException {
        return bot.getTextOfClass(projectName, packageName, className);
    }

    public void openFile(String projectName, String packageName,
        String className) throws RemoteException {
        bot.openFile(projectName, packageName, className + ".java");
    }

    public void follow(Musician participant) throws RemoteException {
        if (participant.isDriver())
            bot.follow(participant.jid.getBase(), " (Driver)");
        else
            bot.follow(participant.jid.getBase(), "");
    }

    public void giveDriverRole(Musician invitee) throws RemoteException {
        bot.giveDriverRole(invitee.jid.getBase());
    }

    public boolean isInFollowMode(Musician participant) throws RemoteException {
        if (participant.isDriver()) {
            return bot.isInFollowMode(participant.jid.getBase(), " (Driver)");
        } else {
            return bot.isInFollowMode(participant.jid.getBase(), "");
        }

    }

    public String getPathToScreenShot() throws RemoteException {
        return state.getPathToScreenShot();
    }

    public boolean isEditorActive(String className) throws RemoteException {
        return bot.isEditorActive(className);
    }

    public void activeEditor(String className) throws RemoteException {
        bot.activeJavaEditor(className);
    }

    public boolean isShellOpenByTitle(String title) throws RemoteException {
        return bot.isShellOpenByTitle(title);

    }

    public void clickCheckBox(String title) throws RemoteException {
        bot.clickCheckBox(title);
    }

    public void waitUntilShellCloses(String title) throws RemoteException {
        bot.waitUntilShellCloses(bot.getShellWithText(title));
    }

    public void activateShellByText(String text) throws RemoteException {
        bot.activateShellWithText(text);
    }

    public void getProjectFromSVN(String path) throws RemoteException {
        bot.getProjectFromSVN(path);
    }
}