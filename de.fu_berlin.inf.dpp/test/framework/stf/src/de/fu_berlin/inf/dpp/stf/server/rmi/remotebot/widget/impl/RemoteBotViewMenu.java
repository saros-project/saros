package de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.impl;

import de.fu_berlin.inf.dpp.stf.server.StfRemoteObject;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotViewMenu;
import java.rmi.RemoteException;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotViewMenu;

public final class RemoteBotViewMenu extends StfRemoteObject implements IRemoteBotViewMenu {

  private static final RemoteBotViewMenu INSTANCE = new RemoteBotViewMenu();

  private SWTBotViewMenu widget;

  public static RemoteBotViewMenu getInstance() {
    return INSTANCE;
  }

  public IRemoteBotViewMenu setWidget(SWTBotViewMenu viewMenu) {
    this.widget = viewMenu;
    return this;
  }

  @Override
  public void click() throws RemoteException {
    widget.click();
  }

  @Override
  public String getToolTipText() throws RemoteException {
    return widget.getText();
  }

  @Override
  public String getText() throws RemoteException {
    return widget.getText();
  }

  @Override
  public boolean isChecked() throws RemoteException {
    return widget.isChecked();
  }
}
