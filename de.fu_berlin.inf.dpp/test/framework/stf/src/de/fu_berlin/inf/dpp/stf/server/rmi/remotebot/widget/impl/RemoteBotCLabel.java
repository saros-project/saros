package de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.impl;

import de.fu_berlin.inf.dpp.stf.server.StfRemoteObject;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotCLabel;
import java.rmi.RemoteException;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCLabel;

public final class RemoteBotCLabel extends StfRemoteObject implements IRemoteBotCLabel {

  private static final RemoteBotCLabel INSTANCE = new RemoteBotCLabel();

  private SWTBotCLabel widget;

  public static RemoteBotCLabel getInstance() {
    return INSTANCE;
  }

  public IRemoteBotCLabel setWidget(SWTBotCLabel label) {
    this.widget = label;
    return this;
  }

  @Override
  public String getToolTipText() throws RemoteException {
    return widget.getText();
  }

  @Override
  public String getText() throws RemoteException {
    return widget.getText();
  }
}
