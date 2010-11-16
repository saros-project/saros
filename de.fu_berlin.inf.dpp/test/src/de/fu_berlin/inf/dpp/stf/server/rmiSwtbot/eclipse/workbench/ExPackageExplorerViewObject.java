package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import de.fu_berlin.inf.dpp.net.JID;

public interface ExPackageExplorerViewObject extends Remote {

    public void closePackageExplorerView() throws RemoteException;

    public void closeWelcomeView() throws RemoteException;

    public void activatePackageExplorerView() throws RemoteException;

    public void deleteProjectGui(String projectName) throws RemoteException;

    public void deleteFileGui(String... nodes) throws RemoteException;

    public boolean isClassExistGUI(String... matchTexts) throws RemoteException;

    public void openClass(String projectName, String packageName,
        String className) throws RemoteException;

    public void openFile(String... filePath) throws RemoteException;

    public void openClassWith(String whichEditor, String projectName,
        String packageName, String className) throws RemoteException;

    public void showViewPackageExplorer() throws RemoteException;

    public void moveClassTo(String projectName, String pkg, String className,
        String targetProject, String targetPkg) throws RemoteException;

    public void importProjectFromSVN(String path) throws RemoteException;

    public void renamePkg(String newName, String... texts)
        throws RemoteException;

    public void renameFolder(String projectName, String oldPath, String newPath)
        throws RemoteException;

    public void renameFile(String newName, String... texts)
        throws RemoteException;

    public void renameClass(String newName, String projectName, String pkg,
        String className) throws RemoteException;

    public void revert() throws RemoteException;

    public void switchToOtherRevision(String CLS_PATH) throws RemoteException;

    public void switchToOtherRevision() throws RemoteException;

    public void connectSVN() throws RemoteException;

    public void disConnectSVN() throws RemoteException;

    public void switchToTag() throws RemoteException;

    public void addToSession(String projectName) throws RemoteException;

    public void shareProjectPartically(String projectName)
        throws RemoteException;

    public void shareprojectWithVCSSupport(String projectName)
        throws RemoteException;

    public void shareProject(String projectName) throws RemoteException;

    public void clickShareProjectWith(String projectName,
        String shareProjectWith) throws RemoteException;

    public void shareProject(String projectName, List<String> inviteeJIDS)
        throws RemoteException;

    public boolean isWindowInvitationCancelledActive() throws RemoteException;

    public void closeWindowInvitaitonCancelled() throws RemoteException;

    public void waitUntilIsWindowInvitationCnacelledActive()
        throws RemoteException;

    public boolean isWIndowSessionInvitationActive() throws RemoteException;

    public void closeWIndowSessionInvitation() throws RemoteException;

    public void waitUntilWIndowSessionInvitationActive() throws RemoteException;

    public void confirmSessionInvitationWindowStep1() throws RemoteException;

    public void confirmSessionUsingNewOrExistProject(JID inviterJID,
        String projectName, int typeOfSharingProject) throws RemoteException;

    public void cancelInivtationInSessionInvitationWindow()
        throws RemoteException;

    public void confirmInvitationCancelledWindow() throws RemoteException;

    public void confirmSessionInvitationWindowStep2UsingExistProjectWithCopy(
        String projectName) throws RemoteException;

    public void confirmSessionInvitationWindowStep2UsingExistProjectWithCancelLocalChange(
        String projectName) throws RemoteException;

    public void confirmSessionInvitationWindowStep2UsingExistProject(
        String projectName) throws RemoteException;

    public void confirmSessionInvitationWindowStep2UsingNewproject(
        String projectName) throws RemoteException;

    public void confirmInvitationWindow(String... invitees)
        throws RemoteException;

    public void confirmSessionInvitationWizard(String inviter,
        String projectname) throws RemoteException;

    public void waitUntilIsWindowProblemOccurredActive() throws RemoteException;

    public void waitUntilSarosRunningVCSOperationClosed()
        throws RemoteException;

    public String getSecondLabelOfProblemOccurredWindow()
        throws RemoteException;

}
