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

    public void ackProject(Musician inviter, String projectname)
        throws RemoteException {
        bot.ackProject1(inviter.getPlainJid());
        bot.ackNewProject2(projectname);
    }

    public void ackContact(Musician questioner) throws RemoteException {
        bot.ackContactAdded(questioner.getPlainJid());
    }

    public void activateShell() {
        try {
            bot.activeShell();
        } catch (RemoteException e) {
            log.error("Could not activate Shell", e);
        }
    }

    public void addContact(Musician respondent) throws RemoteException {
        bot.addNewContact(respondent.getPlainJid());
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
            bot.closeViewByTitle(title);
        } catch (RemoteException e) {
            log.error("View with title '" + title + "' could not be closed", e);
        }
    }

    public void createProjectWithClass(String projectName, String packageName,
        String className) throws RemoteException {
        if (!bot.isProjectInWorkspacePackageExplorer(projectName)) {
            bot.newJavaProject(projectName);
            bot.sleep(750);
            bot.newJavaClass(projectName, packageName, className);
            bot.sleep(750);
        }
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
            return state.hasContact(respondent.jid);
        } catch (RemoteException e) {
            log.error("Failed to check if the contact was found", e);
        }
        return false;
    }

    public void initRmi() throws RemoteException, NotBoundException,
        AccessException {
        Registry registry = LocateRegistry.getRegistry(host, port);
        bot = (ISarosRmiSWTWorkbenchBot) registry.lookup("Bot");
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

    public boolean isRosterViewOpen() {
        try {
            return bot.isRosterViewOpen();
        } catch (RemoteException e) {
            log.error("Failed to get the state of Saros RosterView.", e);
        }
        return false;
    }

    public boolean isViewOpen(String title) throws RemoteException {
        return bot.isViewOpen(title);
    }

    public void leave(boolean confirmation) throws RemoteException {
        bot.leaveSession();
        if (confirmation)
            bot.confirmWindow("Confirm Leaving Session", "Yes");
    }

    public void openJavaPackageExplorerView() throws RemoteException {
        bot.openJavaPackageExplorerView();
    }

    public void openRosterView() throws RemoteException {
        openView("Roster", "Saros", "Roster");
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
        if (!bot.isViewOpen(viewTitle))
            bot.openViewByName(inode, leaf);
    }

    public void removeContact(Musician contact) throws RemoteException {
        if (bot.isContactInRosterView(contact.getPlainJid())) {
            bot.removeContact(contact.getPlainJid());
        }
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
    public void shareProject(Musician invitee, String project)
        throws RemoteException {

        if (!bot.isViewOpen("Package Explorer"))
            bot.openViewByName("Java", "Package Explorer");

        bot.shareProject(project, invitee.getPlainJid());
    }

    public void shareProjectParallel(String projectName, List<Musician> invitees)
        throws RemoteException {
        if (!bot.isViewOpen("Package Explorer"))
            bot.openViewByName("Java", "Package Explorer");

        List<String> list = new LinkedList<String>();
        for (Musician invitee : invitees)
            list.add(invitee.getPlainJid());
        bot.shareProjectParallel(projectName, list);
    }

    public void sleep(long millis) throws RemoteException {
        bot.sleep(millis);
    }

    public void waitOnWindowByTitle(String title) {
        try {
            bot.waitOnShellByTitle(title);
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
        if (!bot.isRosterViewOpen())
            bot.addSarosRosterView();

        if (!bot.isConnectedByXmppGuiCheck())
            bot.xmppConnect();

        if (bot.isConfigShellPoppedUp()) {
            bot.doSarosConfiguration(getXmppServer(), getPlainJid(), password);
        }
    }

    public void xmppDisconnect() throws RemoteException {
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
}
