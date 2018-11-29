package de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.impl;

import de.fu_berlin.inf.dpp.stf.server.StfRemoteObject;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotCTabItem;
import java.rmi.RemoteException;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCTabItem;

public final class RemoteBotCTabItem extends StfRemoteObject implements IRemoteBotCTabItem {

  private static final RemoteBotCTabItem INSTANCE = new RemoteBotCTabItem();

  private SWTBotCTabItem widget;

  public static RemoteBotCTabItem getInstance() {
    return INSTANCE;
  }

  public IRemoteBotCTabItem setWidget(SWTBotCTabItem widget) {
    this.widget = widget;
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
