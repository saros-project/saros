package de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.impl;

import de.fu_berlin.inf.dpp.stf.server.StfRemoteObject;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.impl.RemoteWorkbenchBot;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotMenu;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotToolbarRadioButton;
import java.rmi.RemoteException;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarRadioButton;

public final class RemoteBotToolbarRadioButton extends StfRemoteObject
    implements IRemoteBotToolbarRadioButton {

  private static final RemoteBotToolbarRadioButton INSTANCE = new RemoteBotToolbarRadioButton();

  private SWTBotToolbarRadioButton widget;

  public static RemoteBotToolbarRadioButton getInstance() {
    return INSTANCE;
  }

  public IRemoteBotToolbarRadioButton setWidget(SWTBotToolbarRadioButton toolbarRadioButton) {
    this.widget = toolbarRadioButton;
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
  public IRemoteBotToolbarRadioButton toggle() throws RemoteException {
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
