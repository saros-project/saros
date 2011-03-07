package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface RemoteBotStyledText extends Remote {

    /**********************************************
     * 
     * finders
     * 
     **********************************************/

    public RemoteBotMenu contextMenu(String text) throws RemoteException;

    /**********************************************
     * 
     * states
     * 
     **********************************************/
    public String getText() throws RemoteException;

    public String getToolTipText() throws RemoteException;

    public String getTextOnCurrentLine() throws RemoteException;

    public String getSelection() throws RemoteException;

    public List<String> getLines() throws RemoteException;

    public int getLineCount() throws RemoteException;
}
