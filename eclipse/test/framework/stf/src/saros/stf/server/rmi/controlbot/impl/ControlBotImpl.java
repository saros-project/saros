package saros.stf.server.rmi.controlbot.impl;

import java.rmi.RemoteException;
import saros.stf.server.StfRemoteObject;
import saros.stf.server.rmi.controlbot.IControlBot;
import saros.stf.server.rmi.controlbot.manipulation.IAccountManipulator;
import saros.stf.server.rmi.controlbot.manipulation.INetworkManipulator;
import saros.stf.server.rmi.controlbot.manipulation.impl.AccountManipulatorImpl;
import saros.stf.server.rmi.controlbot.manipulation.impl.NetworkManipulatorImpl;

public final class ControlBotImpl extends StfRemoteObject implements IControlBot {

  private static final IControlBot INSTANCE = new ControlBotImpl();

  public static IControlBot getInstance() {
    return ControlBotImpl.INSTANCE;
  }

  @Override
  public INetworkManipulator getNetworkManipulator() throws RemoteException {
    return NetworkManipulatorImpl.getInstance();
  }

  @Override
  public IAccountManipulator getAccountManipulator() throws RemoteException {
    return AccountManipulatorImpl.getInstance();
  }
}
