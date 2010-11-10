package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.pages;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IPackageExplorerViewObject extends Remote {

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
}
