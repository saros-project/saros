package saros.stf.server.rmi.remotebot.widget.impl;

import java.rmi.RemoteException;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
import saros.stf.server.StfRemoteObject;
import saros.stf.server.rmi.remotebot.impl.RemoteWorkbenchBot;
import saros.stf.server.rmi.remotebot.widget.IRemoteBotButton;
import saros.stf.server.rmi.remotebot.widget.IRemoteBotMenu;

public final class RemoteBotButton extends StfRemoteObject implements IRemoteBotButton {

  private static final RemoteBotButton INSTANCE = new RemoteBotButton();

  private SWTBotButton widget;

  public static RemoteBotButton getInstance() {
    return INSTANCE;
  }

  public IRemoteBotButton setWidget(SWTBotButton button) {
    this.widget = button;
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

  @Override
  public void waitLongUntilIsEnabled() throws RemoteException {
    RemoteWorkbenchBot.getInstance().waitLongUntil(Conditions.widgetIsEnabled(widget));
  }
}
