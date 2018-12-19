package de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IRemoteBotCTabItem extends Remote {

  public String getToolTipText() throws RemoteException;

  public String getText() throws RemoteException;
}
