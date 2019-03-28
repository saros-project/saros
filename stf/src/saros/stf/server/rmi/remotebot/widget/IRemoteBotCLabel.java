package saros.stf.server.rmi.remotebot.widget;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IRemoteBotCLabel extends Remote {

  public String getToolTipText() throws RemoteException;

  public String getText() throws RemoteException;
}
