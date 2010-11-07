package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.pages;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IPopUpWindowObject extends Remote {

    public void confirmProblemOccurredWindow(String plainJID)
        throws RemoteException;

    public void confirmNewContactWindow(String plainJID) throws RemoteException;

    public void comfirmInvitationWindow(String inviteeJID)
        throws RemoteException;

    public void confirmRequestOfSubscriptionReceivedWindow()
        throws RemoteException;

    public void confirmInvitationWindow(String... invitees)
        throws RemoteException;

    public void confirmSessionInvitationWizard(String inviter,
        String projectname) throws RemoteException;

    public void confirmSessionInvitationWizardUsingExistProject(String inviter,
        String projectName) throws RemoteException;

    public void confirmSessionInvitationWizardUsingExistProjectWithCancelLocalChange(
        String inviter, String projectName) throws RemoteException;

    public void confirmSessionInvitationWizardUsingExistProjectWithCopy(
        String inviter, String projectName) throws RemoteException;

    public void confirmCreateNewUserAccountWindow(String server,
        String username, String password) throws RemoteException;

    public void confirmSessionInvitationWindowStep1() throws RemoteException;

    public void confirmSessionInvitationWindowStep2UsingNewproject(
        String projectName) throws RemoteException;

    public void confirmSessionInvitationWindowStep2UsingExistProject(
        String projectName) throws RemoteException;

    public void confirmSessionInvitationWindowStep2UsingExistProjectWithCancelLocalChange(
        String projectName) throws RemoteException;

    public void confirmSessionInvitationWindowStep2UsingExistProjectWithCopy(
        String projectName) throws RemoteException;

    public void confirmSarosConfigurationWizard(String xmppServer, String jid,
        String password) throws RemoteException;
}
