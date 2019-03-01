package de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.impl;

import de.fu_berlin.inf.dpp.stf.server.StfRemoteObject;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotCombo;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotMenu;
import java.rmi.RemoteException;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCombo;

public final class RemoteBotCombo extends StfRemoteObject implements IRemoteBotCombo {

  private static final RemoteBotCombo INSTANCE = new RemoteBotCombo();

  private SWTBotCombo widget;

  public static RemoteBotCombo getInstance() {
    return INSTANCE;
  }

  public IRemoteBotCombo setWidget(SWTBotCombo ccomb) {
    this.widget = ccomb;
    return this;
  }

  /** @see SWTBotCombo#contextMenu(String) */
  @Override
  public IRemoteBotMenu contextMenu(String text) throws RemoteException {
    return RemoteBotMenu.getInstance().setWidget(widget.contextMenu(text));
  }

  /**
   * ********************************************
   *
   * <p>actions
   *
   * <p>********************************************
   */
  /** @see SWTBotCombo#typeText(String) */
  @Override
  public void typeText(String text) throws RemoteException {
    widget.typeText(text);
  }

  /** @see SWTBotCombo#typeText(String, int) */
  @Override
  public void typeText(String text, int interval) throws RemoteException {
    widget.typeText(text, interval);
  }

  /** @see SWTBotCombo#setFocus() */
  @Override
  public void setFocus() throws RemoteException {
    widget.setFocus();
  }

  /** @see SWTBotCombo#setText(String) */
  @Override
  public void setText(String text) throws RemoteException {
    widget.setText(text);
  }

  /** @see SWTBotCombo#setSelection(String) */
  @Override
  public void setSelection(String text) throws RemoteException {
    widget.setSelection(text);
  }

  /** @see SWTBotCombo#setSelection(int) */
  @Override
  public void setSelection(int index) throws RemoteException {
    widget.setSelection(index);
  }

  /** @see SWTBotCombo#itemCount() */
  @Override
  public int itemCount() throws RemoteException {
    return widget.itemCount();
  }

  /** @see SWTBotCombo#items() */
  @Override
  public String[] items() throws RemoteException {
    return widget.items();
  }

  /** @see SWTBotCombo#selection() */
  @Override
  public String selection() throws RemoteException {
    return widget.selection();
  }

  /** @see SWTBotCombo#selectionIndex() */
  @Override
  public int selectionIndex() throws RemoteException {
    return widget.selectionIndex();
  }

  /** @see SWTBotCombo#isEnabled() */
  @Override
  public boolean isEnabled() throws RemoteException {
    return widget.isEnabled();
  }

  /** @see SWTBotCombo#isVisible() */
  @Override
  public boolean isVisible() throws RemoteException {
    return widget.isVisible();
  }

  /** @see SWTBotCombo#isActive() */
  @Override
  public boolean isActive() throws RemoteException {
    return widget.isActive();
  }

  /** @see SWTBotCombo#getText() */
  @Override
  public String getText() throws RemoteException {
    return widget.getText();
  }

  /** @see SWTBotCombo#getToolTipText() */
  @Override
  public String getToolTipText() throws RemoteException {
    return widget.getText();
  }
}
