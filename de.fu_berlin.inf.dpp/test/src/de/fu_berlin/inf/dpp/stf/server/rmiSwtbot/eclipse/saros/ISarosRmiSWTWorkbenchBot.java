package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros;

import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.client.Musician;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.IRmiSWTWorkbenchBot;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.pages.IRosterViewObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.pages.ISarosWindowObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.pages.ISessionViewObject;
import de.fu_berlin.inf.dpp.ui.RosterView;

/**
 * This is the RMI interface for remoting Saros Eclipse Plugin. Use this from
 * {@link Musician} to write tests.
 */
public interface ISarosRmiSWTWorkbenchBot extends IRmiSWTWorkbenchBot {

    /*************** Saros-specific-highlevel RMI exported Methods ******************/

    // public void confirmCreateNewUserAccountWindow(String server,
    // String username, String password) throws RemoteException;

    // public void ackContactAdded(String bareJid) throws RemoteException;

    /**
     * First acknowledge of a new Session initiated by the given inviter.
     */
    // public void confirmSessionInvitationWindowStep1() throws RemoteException;

    /**
     * Acknowledge the given Project creating a new Project in your workspace
     */
    // public void confirmSessionInvitationWindowStep2UsingNewproject(
    // String projectName) throws RemoteException;

    // public void confirmSessionInvitationWindowStep2UsingExistProject(
    // String projectName) throws RemoteException;

    // public void confirmSessionInvitationWindowStep2UsingExistProjectWithCopy(
    // String projectName) throws RemoteException;

    // public void addNewContact(String name) throws RemoteException;

    // public void addToSharedProjects(String bareJid) throws RemoteException;

    /**
     * Popup Window with title "Saros Configuration"
     */
    // public void confirmSarosConfigurationWizard(String xmppServer, String
    // jid,
    // String password) throws RemoteException;

    // public boolean isConfigShellPoppedUp() throws RemoteException;

    /**
     * It returns true if the GUI on the {@link RosterView} is showing the
     * connected state.
     */
    // public boolean isConnectedByXmppGuiCheck() throws RemoteException;

    // public boolean hasContactWith(String contact) throws RemoteException;

    // public boolean isContactOnline(String contact) throws RemoteException;

    // /**
    // * Invite given jid to the current shared project
    // */
    // public boolean isInSession() throws RemoteException;

    // public boolean isInSharedProject(String jid) throws RemoteException;

    // public boolean isRosterViewOpen() throws RemoteException;

    // public boolean isSharedSessionViewOpen() throws RemoteException;

    // public void leaveSession() throws RemoteException;

    // public void deleteContact(JID jid, ISarosRmiSWTWorkbenchBot participant)
    // throws RemoteException;

    // /**
    // * It shares a project, but cancel the interface popped up
    // */
    // public void clickProjectContextMenu(String projectName,
    // String nameOfContextMenu) throws RemoteException;

    // public void clickCMShareProjectInPEView(String projectName)
    // throws RemoteException;

    // public void shareProject(String projectName, List<String> inviteeJIDS)
    // throws RemoteException;

    // public void clickTBConnectInRosterView() throws RemoteException;

    // public boolean clickTBDisconnectInRosterView() throws RemoteException;

    // public void followUser(ISarosState stateOfFollowedUser,
    // JID JIDOfFollowedUser) throws RemoteException;

    // public void clickCMgiveDriverRoleInSPSView(String inviteeJID)
    // throws RemoteException;

    // public boolean isInFollowMode(String participantJID, String sufix)
    // throws RemoteException;

    // public void clickTBShareYourScreenWithSelectedUserInSPSView()
    // throws RemoteException;

    // public void clickTBSendAFileToSelectedUserInSPSView(String inviteeJID)
    // throws RemoteException;

    // public void clickTBStopSessionWithUserInSPSView(String name)
    // throws RemoteException;

    // public void clickTBChangeModeOfImageSourceInRSView() throws
    // RemoteException;

    // public void clickTBStopRunningSessionInRSView() throws RemoteException;

    // public void clickTBResumeInRSView() throws RemoteException;

    // public void clickTBPauseInRSView() throws RemoteException;

    // public void clickTBStartAVoIPSessionInSPSView() throws RemoteException;

    // public void clickTBNoInconsistenciesInSPSView() throws RemoteException;

    // public void clickTBRemoveAllRriverRolesInSPSView() throws
    // RemoteException;

    // public void clickTBEnableDisableFollowModeInSPSView()
    // throws RemoteException;

    // public void clickTBLeaveTheSessionInSPSView() throws RemoteException;

    // public void clickCMRemoveDriverRoleInSPSView(String inviteeJID)
    // throws RemoteException;

    // public void clickCMgiveExclusiveDriverRoleInSPSView(String inviteeJID)
    // throws RemoteException;

    // public void clickCMStopFollowingThisUserInSPSView(ISarosState state, JID
    // jid)
    // throws RemoteException;

    // public void clickCMJumpToPositionOfSelectedUserInSPSView(
    // String participantJID, String sufix) throws RemoteException;

    // public void waitUntilSessionCloses() throws RemoteException;

    // public void waitUntilSessionClosedBy(ISarosState state)
    // throws RemoteException;

    // public void openChatView() throws RemoteException;

    // public void openRemoteScreenView() throws RemoteException;

    // public void openSharedSessionView() throws RemoteException;

    // public void openRosterView() throws RemoteException;

    // public boolean isChatViewOpen() throws RemoteException;

    // public boolean isRemoteScreenViewOpen() throws RemoteException;

    // public void addContact(JID jid, ISarosRmiSWTWorkbenchBot participant)
    // throws RemoteException;

    // public void setTextInJavaEditorWithSave(String contentPath,
    // String projectName, String packageName, String className)
    // throws RemoteException;

    // public boolean isConnectedByXMPP() throws RemoteException;

    // public void clickTBOpenInvitationInterfaceInSPSView()
    // throws RemoteException;

    // public void clickTBAddANewContactInRosterView() throws RemoteException;

    // public void clickCMShareprojectWithVCSSupportInPEView(String projectName)
    // throws RemoteException;

    // public void clickCMShareProjectParticallyInPEView(String projectName)
    // throws RemoteException;

    // public void clickCMAddToSessionInPEView(String projectName)
    // throws RemoteException;

    // public void closeRosterView() throws RemoteException;

    // public void closeSessionView() throws RemoteException;

    // public void closeRemoteScreenView() throws RemoteException;

    // public void closeChatView() throws RemoteException;

    // public void closePackageExplorerView() throws RemoteException;

    // public void waitUntilConnected() throws RemoteException;

    // public void waitUntilDisConnected() throws RemoteException;

    // public SWTBotTreeItem selectBuddy(String contact) throws RemoteException;

    // public void waitUntilSessionOpenBy(ISarosState state)
    // throws RemoteException;

    // public void waitUntilSessionOpen() throws RemoteException;

    // public void
    // confirmSessionInvitationWindowStep2UsingExistProjectWithCancelLocalChange(
    // String projectName) throws RemoteException;

    // public void confirmSessionInvitationWizard(String inviter,
    // String projectname) throws RemoteException;

    // public void confirmSessionInvitationWizardUsingExistProject(String
    // inviter,
    // String projectName) throws RemoteException;

    // public void
    // confirmSessionInvitationWizardUsingExistProjectWithCancelLocalChange(
    // String inviter, String projectName) throws RemoteException;

    // public void confirmSessionInvitationWizardUsingExistProjectWithCopy(
    // String inviter, String projectName) throws RemoteException;

    // public void confirmInvitationWindow(String... invitee)
    // throws RemoteException;

    // public void confirmRequestOfSubscriptionReceivedWindow()
    // throws RemoteException;

    // public void giveDriverRole(String inviteeJID) throws RemoteException;

    // public void comfirmInvitationWindow(String inviteeJID)
    // throws RemoteException;

    // public boolean isConnectedByXMPP() throws RemoteException;

    public void xmppConnect(JID jid, String password) throws RemoteException;

    // public void xmppDisconnect() throws RemoteException;
    //
    // public void creatNewAccount(JID jid, String password)
    // throws RemoteException;

    // public boolean hasContactWith(JID jid) throws RemoteException;

    public void openSarosViews() throws RemoteException;

    // public void renameContact(String contact, String newName)
    // throws RemoteException;

    // public boolean isContactInSessionView(String Contact)
    // throws RemoteException;

    public void leaveSessionByPeer() throws RemoteException;

    // public void clickShareProjectWith(String projectName,
    // String shareProjectWith) throws RemoteException;

    public void confirmSessionUsingNewOrExistProject(
        ISarosRmiSWTWorkbenchBot inviteeBot, JID inviterJID,
        String projectName, int typeOfSharingProject) throws RemoteException;

    // public void sendChatMessage(String string) throws RemoteException;

    // public boolean compareChatMessage(String jid, String string)
    // throws RemoteException;

    public void resetSaros() throws RemoteException;

    // public void activateRosterView() throws RemoteException;

    // public void confirmNewContactWindow(String plainJID) throws
    // RemoteException;

    public void leaveSessionByHost() throws RemoteException;

    // public void waitUntilAllPeersLeaveSession(List<JID> jids)
    // throws RemoteException;

    // public void cancelInvitation() throws RemoteException;

    // public void openProgressView() throws RemoteException;

    // public boolean isProgressViewOpen() throws RemoteException;

    // public void confirmInvitationCancelledWindow() throws RemoteException;

    // public void removeProgress() throws RemoteException;

    // public void waitUntilGetChatMessage(String jid, String message)
    // throws RemoteException;

    // public void typeTextInJavaEditor(String contentPath, String projectName,
    // String packageName, String className) throws RemoteException;

    // public void waitUntilNoInvitationProgress() throws RemoteException;

    // public void setTextInJavaEditorWithoutSave(String contentPath,
    // String projectName, String packageName, String className)
    // throws RemoteException;

    // public void giveExclusiveDriverRole(String inviteePlainJID)
    // throws RemoteException;

    // public void setTextInEditorWithSave(String contentPath, String...
    // filePath)
    // throws RemoteException;

    // public void cancelInvitation(int index) throws RemoteException;

    // public void cancelInivtationInSessionInvitationWindow()
    // throws RemoteException;

    // public void stopFollowUser(ISarosState stateOfFollowedUser,
    // JID JIDOfFollowedUser) throws RemoteException;

    // public void waitUntilFollowed(String plainJID) throws RemoteException;

    // public void invitateUser(String inviteeJID) throws RemoteException;

    // public boolean isToolbarNoInconsistenciesEnabled() throws
    // RemoteException;

    // public String getTextOfLastChatLine() throws RemoteException;

    // public String getTextOfChatLine(String regex) throws RemoteException;

    // public String getTextOfChatLine() throws RemoteException;

    // public String getTextOfChatLine(int index) throws RemoteException;

    // public String getUserNameOnChatLinePartnerChangeSeparator()
    // throws RemoteException;

    // public String getUserNameOnChatLinePartnerChangeSeparator(int index)
    // throws RemoteException;

    // public String getUserNameOnChatLinePartnerChangeSeparator(String plainID)
    // throws RemoteException;

    public IRosterViewObject getRosterViewObject() throws RemoteException;

    public ISarosWindowObject getPopupWindowObject() throws RemoteException;

    public ISessionViewObject getSessionViewObject() throws RemoteException;
}
