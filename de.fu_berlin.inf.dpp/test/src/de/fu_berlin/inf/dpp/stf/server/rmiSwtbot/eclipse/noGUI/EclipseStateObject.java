package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noGUI;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;

import org.eclipse.core.runtime.CoreException;

public interface EclipseStateObject extends Remote {
    public void waitUntilFileExist(String... filePath) throws RemoteException;

    public void waitUntilClassExist(String projectName, String pkg,
        String className) throws RemoteException;

    public void waitUntilClassNotExist(String projectName, String pkg,
        String className) throws RemoteException;

    public void waitUntilClassContentsSame(String projectName, String pkg,
        String className, String otherClassContent) throws RemoteException;

    public void waitUntilFolderExist(String... folderPath)
        throws RemoteException;

    public void waitUntilProjectNotInSVN(String projectName)
        throws RemoteException;

    public void waitUntilProjectInSVN(String projectName)
        throws RemoteException;

    public void waitUntilPkgExist(String projectName, String pkg)
        throws RemoteException;

    public void waitUntilPkgNotExist(String projectName, String pkg)
        throws RemoteException;

    public void openClassWithSystemEditor(String projectName, String pkg,
        String className) throws RemoteException;

    public String getRevision(String fullPath) throws RemoteException;

    public String getURLOfRemoteResource(String fullPath)
        throws RemoteException;

    public boolean isFolderExist(String... folderPath) throws RemoteException;

    public String getClassContent(String projectName, String pkg,
        String className) throws RemoteException, IOException, CoreException;

    public boolean isFileExist(String... filePath) throws RemoteException;

    public boolean existsClass(String projectName, String pkg, String className)
        throws RemoteException;

    public boolean isPkgExist(String projectName, String pkg)
        throws RemoteException;

    public boolean isResourceExist(String resourcePath) throws RemoteException;

    public boolean existsProject(String projectName) throws RemoteException;

    public boolean isInSVN() throws RemoteException;

    public void deleteAllProjects() throws RemoteException;

    public void deleteProject(String projectName) throws RemoteException;

    public void deletePkg(String projectName, String pkg)
        throws RemoteException;

    public void deleteFolder(String... folders) throws RemoteException;

    public void deleteClass(String projectName, String pkg, String className)
        throws RemoteException;
}
