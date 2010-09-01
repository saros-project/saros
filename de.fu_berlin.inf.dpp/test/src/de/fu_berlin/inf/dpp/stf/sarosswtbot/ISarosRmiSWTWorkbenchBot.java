package de.fu_berlin.inf.dpp.stf.sarosswtbot;

import java.rmi.RemoteException;
import java.util.List;

import de.fu_berlin.inf.dpp.stf.swtbot.IRmiSWTWorkbenchBot;
import de.fu_berlin.inf.dpp.ui.RosterView;

/**
 * This is the RMI interface for remoting Saros Eclipse Plugin. Use this from
 * {@link Musician} to write tests.
 */
public interface ISarosRmiSWTWorkbenchBot extends IRmiSWTWorkbenchBot {

    /*************** Saros-specific-highlevel RMI exported Methods ******************/

    public void accountBySarosMenu(String server, String username,
        String password) throws RemoteException;

    public void ackContactAdded(String bareJid) throws RemoteException;

    /**
     * First acknowledge of a new Session initiated by the given inviter.
     */
    public void ackProject1(String inviter) throws RemoteException;

    /**
     * Acknowledge the given Project creating a new Project in your workspace
     */
    public void ackNewProject2(String projectName) throws RemoteException;

    public void addNewContact(String name) throws RemoteException;

    public void addSarosRosterView() throws RemoteException;

    public void addSarosSessionView() throws RemoteException;

    public void addToSharedProject(String bareJid) throws RemoteException;

    /**
     * Popup Window with title "Saros Configuration"
     */
    public void doSarosConfiguration(String xmppServer, String jid,
        String password) throws RemoteException;

    public boolean isConfigShellPoppedUp() throws RemoteException;

    /**
     * It returns true if the GUI on the {@link RosterView} is showing the
     * connected state.
     */
    public boolean isConnectedByXmppGuiCheck() throws RemoteException;

    public boolean isContactInRosterView(String contact) throws RemoteException;

    public boolean isContactOnline(String contact) throws RemoteException;

    /**
     * Invite given jid to the current shared project
     */
    public void inviteToProject(String jid) throws RemoteException;

    public boolean isInSession() throws RemoteException;

    public boolean isInSharedProject(String jid) throws RemoteException;

    public boolean isRosterViewOpen() throws RemoteException;

    public boolean isSharedSessionViewOpen() throws RemoteException;

    public void leaveSession() throws RemoteException;

    public void removeContact(String name) throws RemoteException;

    /**
     * It shares a project, but cancel the interface popped up
     */
    public void shareProject(String projectName) throws RemoteException;

    public void shareProject(String projectName, String invitee)
        throws RemoteException;

    public void shareProjectSequential(String projectName, List<String> invitees)
        throws RemoteException;

    public void shareProjectParallel(String projectName, List<String> invitees)
        throws RemoteException;

    public void xmppConnect() throws RemoteException;

    public boolean xmppDisconnect() throws RemoteException;

    public void follow(String participantJID, String sufix)
        throws RemoteException;

    public boolean isInFollowMode(String participantJID, String sufix)
        throws RemoteException;

}
