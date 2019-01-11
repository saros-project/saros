package de.fu_berlin.inf.dpp.stf.server.rmi.controlbot.impl;

import de.fu_berlin.inf.dpp.stf.server.StfRemoteObject;
import de.fu_berlin.inf.dpp.stf.server.rmi.controlbot.IControlBot;
import de.fu_berlin.inf.dpp.stf.server.rmi.controlbot.manipulation.IAccountManipulator;
import de.fu_berlin.inf.dpp.stf.server.rmi.controlbot.manipulation.INetworkManipulator;
import de.fu_berlin.inf.dpp.stf.server.rmi.controlbot.manipulation.impl.AccountManipulatorImpl;
import de.fu_berlin.inf.dpp.stf.server.rmi.controlbot.manipulation.impl.NetworkManipulatorImpl;
import java.rmi.RemoteException;

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
