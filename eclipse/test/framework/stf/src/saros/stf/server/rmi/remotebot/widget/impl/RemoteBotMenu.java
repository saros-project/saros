package saros.stf.server.rmi.remotebot.widget.impl;

import java.rmi.RemoteException;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import saros.stf.server.StfRemoteObject;
import saros.stf.server.rmi.remotebot.impl.RemoteWorkbenchBot;
import saros.stf.server.rmi.remotebot.widget.IRemoteBotMenu;

public final class RemoteBotMenu extends StfRemoteObject implements IRemoteBotMenu {

  private static final RemoteBotMenu INSTANCE = new RemoteBotMenu();

  private SWTBotMenu widget;

  public static RemoteBotMenu getInstance() {
    return INSTANCE;
  }

  public IRemoteBotMenu setWidget(SWTBotMenu widget) {
    this.widget = widget;
    return this;
  }

  @Override
  public RemoteBotMenu contextMenu(String text) throws RemoteException {
    widget = widget.contextMenu(text);
    return this;
  }

  @Override
  public IRemoteBotMenu menu(String menuName) throws RemoteException {
    widget = widget.menu(menuName);
    return this;
  }

  @Override
  public void click() throws RemoteException {
    widget.click();
  }

  public void clickAndWait() throws RemoteException {
    waitUntilIsEnabled();
    click();
  }

  @Override
  public void setFocus() throws RemoteException {
    widget.setFocus();
  }

  @Override
  public boolean isEnabled() throws RemoteException {
    return widget.isEnabled();
  }

  @Override
  public boolean isVisible() throws RemoteException {
    return widget.isVisible();
  }

  @Override
  public boolean isActive() throws RemoteException {
    return widget.isActive();
  }

  @Override
  public boolean isChecked() throws RemoteException {
    return widget.isChecked();
  }

  @Override
  public String getText() throws RemoteException {
    return widget.getText();
  }

  @Override
  public String getToolTipText() throws RemoteException {
    return widget.getText();
  }

  @Override
  public void waitUntilIsEnabled() throws RemoteException {
    RemoteWorkbenchBot.getInstance().waitUntil(Conditions.widgetIsEnabled(widget));
  }
}
