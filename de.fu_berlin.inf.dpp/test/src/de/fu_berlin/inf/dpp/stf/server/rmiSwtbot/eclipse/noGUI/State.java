package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noGUI;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;

import org.eclipse.core.runtime.CoreException;

public interface State extends Remote {

    /**
     * Sometimes you want to know, if a peer(e.g. Bob) can see the changes of
     * file, which is modified by another peer (e.g. Alice). Because of data
     * transfer delay Bob need to wait a minute to see the changes . So it will
     * be a good idea that you give bob some time before you compare the two
     * files from Alice and Bob.
     * 
     * <p>
     * <b>Note:</b> the mothod is different from
     * {@link #waitUntilEditorContentSame(String, String, String, String)}. this
     * method compare only the contents of the class files which is saved.
     * </p>
     * * *
     */
    public void waitUntilClassContentsSame(String projectName, String pkg,
        String className, String otherClassContent) throws RemoteException;

    public String getClassContent(String projectName, String pkg,
        String className) throws RemoteException, IOException, CoreException;

    public void deleteAllProjects() throws RemoteException;

}
