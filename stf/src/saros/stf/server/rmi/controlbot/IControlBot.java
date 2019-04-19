package saros.stf.server.rmi.controlbot;

import java.rmi.Remote;
import java.rmi.RemoteException;
import saros.stf.server.rmi.controlbot.manipulation.IAccountManipulator;
import saros.stf.server.rmi.controlbot.manipulation.INetworkManipulator;

/**
 * The Saros Control robot allows you to change the behavior of Saros during runtime and accessing
 * business logic directly. It does <b>NOT</b> perform any GUI operations but manipulate Saros via
 * reflection, direct method calls and other hooks. This robot should only be used in very few
 * cases.
 */
public interface IControlBot extends Remote {

  /**
   * Returns an interface to manipulate the Saros network layer
   *
   * @return
   * @throws RemoteException
   */
  public INetworkManipulator getNetworkManipulator() throws RemoteException;

  /**
   * Returns an interface to manipulate the Saros account store
   *
   * @return
   * @throws RemoteException
   */
  public IAccountManipulator getAccountManipulator() throws RemoteException;
}
