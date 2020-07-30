package saros.stf.server.rmi.remotebot.widget.impl;

import java.rmi.RemoteException;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import saros.stf.server.StfRemoteObject;
import saros.stf.server.rmi.remotebot.widget.IRemoteBotViewMenu;

public final class RemoteBotViewMenu extends StfRemoteObject implements IRemoteBotViewMenu {

  private static final RemoteBotViewMenu INSTANCE = new RemoteBotViewMenu();

  private SWTBotMenu widget;

  public static RemoteBotViewMenu getInstance() {
    return INSTANCE;
  }

  public IRemoteBotViewMenu setWidget(SWTBotMenu viewMenu) {
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
