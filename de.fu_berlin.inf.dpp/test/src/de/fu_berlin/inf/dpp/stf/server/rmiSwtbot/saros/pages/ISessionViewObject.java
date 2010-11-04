package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.saros.pages;

import java.rmi.Remote;
import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.saros.noGUI.ISarosState;

public interface ISessionViewObject extends Remote {

    public void activateSharedSessionView() throws RemoteException;

    public void giveDriverRole(String inviteeJID) throws RemoteException;

    public boolean isSharedSessionViewOpen() throws RemoteException;

    public boolean isInSession() throws RemoteException;

    public boolean isContactInSessionView(String Contact)
        throws RemoteException;

    public boolean isFollowing() throws RemoteException;

    public void openSharedSessionView() throws RemoteException;

    public void closeSessionView() throws RemoteException;

    public void clickTBShareYourScreenWithSelectedUserInSPSView()
        throws RemoteException;

    public void clickTBStopSessionWithUserInSPSView(String name)
        throws RemoteException;

    public void clickTBSendAFileToSelectedUserInSPSView(String inviteeJID)
        throws RemoteException;

    public void clickTBOpenInvitationInterfaceInSPSView()
        throws RemoteException;

    public void clickTBStartAVoIPSessionInSPSView() throws RemoteException;

    public void clickTBNoInconsistenciesInSPSView() throws RemoteException;

    public void clickTBRemoveAllRriverRolesInSPSView() throws RemoteException;

    public void clickTBEnableDisableFollowModeInSPSView()
        throws RemoteException;

    public void clickTBLeaveTheSessionInSPSView() throws RemoteException;

    public void clickCMJumpToPositionOfSelectedUserInSPSView(
        String participantJID, String sufix) throws RemoteException;

    public void clickCMStopFollowingThisUserInSPSView(ISarosState state, JID jid)
        throws RemoteException;

    public void clickCMgiveExclusiveDriverRoleInSPSView(String inviteeJID)
        throws RemoteException;

    public void clickCMRemoveDriverRoleInSPSView(String inviteeJID)
        throws RemoteException;

    public void giveExclusiveDriverRole(String inviteePlainJID)
        throws RemoteException;
}
