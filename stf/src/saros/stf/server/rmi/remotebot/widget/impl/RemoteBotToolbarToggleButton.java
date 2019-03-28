package saros.stf.server.rmi.remotebot.widget.impl;

import java.rmi.RemoteException;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarToggleButton;
import saros.stf.server.StfRemoteObject;
import saros.stf.server.rmi.remotebot.impl.RemoteWorkbenchBot;
import saros.stf.server.rmi.remotebot.widget.IRemoteBotMenu;
import saros.stf.server.rmi.remotebot.widget.IRemoteBotToolbarToggleButton;

public final class RemoteBotToolbarToggleButton extends StfRemoteObject
    implements IRemoteBotToolbarToggleButton {

  private static final RemoteBotToolbarToggleButton INSTANCE = new RemoteBotToolbarToggleButton();

  private SWTBotToolbarToggleButton widget;

  public static RemoteBotToolbarToggleButton getInstance() {
    return INSTANCE;
  }

  public IRemoteBotToolbarToggleButton setWidget(SWTBotToolbarToggleButton toolbarToggleButton) {
    this.widget = toolbarToggleButton;
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
  public void deselect() throws RemoteException {
    widget.deselect();
  }

  @Override
  public IRemoteBotToolbarToggleButton toggle() throws RemoteException {
    return setWidget(widget.toggle());
  }

  @Override
  public void select() throws RemoteException {
    widget.select();
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
  public boolean isChecked() throws RemoteException {
    return widget.isChecked();
  }

  @Override
  public void waitUntilIsEnabled() throws RemoteException {
    RemoteWorkbenchBot.getInstance().waitUntil(Conditions.widgetIsEnabled(widget));
  }
}
