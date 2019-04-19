package saros.stf.server.rmi.remotebot.widget.impl;

import java.rmi.RemoteException;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarPushButton;
import saros.stf.server.StfRemoteObject;
import saros.stf.server.rmi.remotebot.impl.RemoteWorkbenchBot;
import saros.stf.server.rmi.remotebot.widget.IRemoteBotMenu;
import saros.stf.server.rmi.remotebot.widget.IRemoteBotToolbarPushButton;

public final class RemoteBotToolbarPushButton extends StfRemoteObject
    implements IRemoteBotToolbarPushButton {

  private static final RemoteBotToolbarPushButton INSTANCE = new RemoteBotToolbarPushButton();

  private SWTBotToolbarPushButton widget;

  public static RemoteBotToolbarPushButton getInstance() {
    return INSTANCE;
  }

  public IRemoteBotToolbarPushButton setWidget(SWTBotToolbarPushButton toolbarPushButton) {
    this.widget = toolbarPushButton;
    return this;
  }

  @Override
  public IRemoteBotMenu contextMenu(String text) throws RemoteException {
    return RemoteBotMenu.getInstance().setWidget(widget.contextMenu(text));
  }

  @Override
  public void click() throws RemoteException {
    widget.click();
  }

  @Override
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
