package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.remoteFinder.remoteWidgets;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IRemoteBotChatLine extends Remote {

    public String getText() throws RemoteException;
}