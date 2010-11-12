package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench;

import java.rmi.Remote;
import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;

public interface EclipsePopUpWindowObject extends Remote {
    public void waitUntilShellActive(String title) throws RemoteException;

    public void waitUntilShellCloses(SWTBotShell shell) throws RemoteException;

    public void waitUntilShellCloses(String shellText) throws RemoteException;

    public void closeShell(String title) throws RemoteException;

    public boolean isShellOpen(String title) throws RemoteException;

    public boolean isShellActive(String title) throws RemoteException;

    public boolean activateShellWithMatchText(String matchText)
        throws RemoteException;

    public void confirmWindow(String title, String buttonText)
        throws RemoteException;

    public void confirmWindowWithCheckBox(String title, String buttonText,
        boolean isChecked) throws RemoteException;

    public void confirmWindowWithCheckBox(String title, String buttonText,
        String... itemNames) throws RemoteException;

    public void confirmWindowWithTable(String title, String itemName,
        String buttonText) throws RemoteException;

    public void confirmWindowWithTreeWithFilterText(String title,
        String rootOfTreeNode, String teeNode, String buttonText)
        throws RemoteException;

    public String getSecondLabelOfProblemOccurredWindow()
        throws RemoteException;
}
