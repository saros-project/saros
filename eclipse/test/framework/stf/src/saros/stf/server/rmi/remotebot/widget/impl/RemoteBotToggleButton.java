package de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.impl;

import de.fu_berlin.inf.dpp.stf.server.StfRemoteObject;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.impl.RemoteWorkbenchBot;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotToggleButton;
import java.rmi.RemoteException;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToggleButton;

public final class RemoteBotToggleButton extends StfRemoteObject implements IRemoteBotToggleButton {

  private static final RemoteBotToggleButton INSTANCE = new RemoteBotToggleButton();

  private SWTBotToggleButton widget;

  public static RemoteBotToggleButton getInstance() {
    return INSTANCE;
  }

  public IRemoteBotToggleButton setWidget(SWTBotToggleButton toggleButton) {
    this.widget = toggleButton;
    return this;
  }

  @Override
  public void click() throws RemoteException {
    widget.click();
  }

  @Override
  public void press() throws RemoteException {
    widget.press();
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
  public boolean isPressed() throws RemoteException {
    return widget.isPressed();
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
