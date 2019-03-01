package de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.impl;

import de.fu_berlin.inf.dpp.stf.server.StfRemoteObject;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotCheckBox;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotMenu;
import java.rmi.RemoteException;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCheckBox;

public final class RemoteBotCheckBox extends StfRemoteObject implements IRemoteBotCheckBox {

  private static final RemoteBotCheckBox INSTANCE = new RemoteBotCheckBox();

  private SWTBotCheckBox widget;

  public static RemoteBotCheckBox getInstance() {
    return INSTANCE;
  }

  public IRemoteBotCheckBox setWidget(SWTBotCheckBox checkBox) {
    this.widget = checkBox;
    return this;
  }

  /** @see SWTBotCheckBox#contextMenu(String) */
  @Override
  public IRemoteBotMenu contextMenu(String text) throws RemoteException {
    return RemoteBotMenu.getInstance().setWidget(widget.contextMenu(text));
  }

  /** @see SWTBotCheckBox#click() */
  @Override
  public void click() throws RemoteException {
    widget.click();
  }

  /** @see SWTBotCheckBox#select() */
  @Override
  public void select() throws RemoteException {
    widget.select();
  }

  /** @see SWTBotCheckBox#deselect() */
  @Override
  public void deselect() throws RemoteException {
    widget.deselect();
  }

  /** @see SWTBotCheckBox#setFocus() */
  @Override
  public void setFocus() throws RemoteException {
    widget.setFocus();
  }

  /** @see SWTBotCheckBox#isEnabled() */
  @Override
  public boolean isEnabled() throws RemoteException {
    return widget.isEnabled();
  }

  /** @see SWTBotCheckBox#isVisible() */
  @Override
  public boolean isVisible() throws RemoteException {
    return widget.isVisible();
  }

  /** @see SWTBotCheckBox#isActive() */
  @Override
  public boolean isActive() throws RemoteException {
    return widget.isActive();
  }

  /** @see SWTBotCheckBox#isChecked() */
  @Override
  public boolean isChecked() throws RemoteException {
    return widget.isChecked();
  }

  /** @see SWTBotCheckBox#getText() */
  @Override
  public String getText() throws RemoteException {
    return widget.getText();
  }

  /** @see SWTBotCheckBox#getToolTipText() */
  @Override
  public String getToolTipText() throws RemoteException {
    return widget.getText();
  }
}
