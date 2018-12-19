package de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IRemoteBotPerspective extends Remote {

  public void activate() throws RemoteException;

  public String getLabel() throws RemoteException;

  public boolean isActive() throws RemoteException;
}
