package saros.stf.server.rmi.remotebot.widget.impl;

import java.rmi.RemoteException;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCLabel;
import saros.stf.server.StfRemoteObject;
import saros.stf.server.rmi.remotebot.widget.IRemoteBotCLabel;

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
