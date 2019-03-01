package de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.impl;

import de.fu_berlin.inf.dpp.stf.server.StfRemoteObject;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotLabel;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotMenu;
import java.rmi.RemoteException;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotLabel;

public final class RemoteBotLabel extends StfRemoteObject implements IRemoteBotLabel {

  private static final RemoteBotLabel INSTANCE = new RemoteBotLabel();

  private SWTBotLabel widget;

  public static RemoteBotLabel getInstance() {
    return INSTANCE;
  }

  public IRemoteBotLabel setWidget(SWTBotLabel label) {
    this.widget = label;
    return this;
  }

  @Override
  public IRemoteBotMenu contextMenu(String text) throws RemoteException {
    return RemoteBotMenu.getInstance().setWidget(widget.contextMenu(text));
  }

  @Override
  public void setFocus() throws RemoteException {
    widget.setFocus();
  }

  @Override
  public int alignment() throws RemoteException {
    return widget.alignment();
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
  public String getToolTipText() throws RemoteException {
    return widget.getText();
  }

  @Override
  public String getText() throws RemoteException {
    return widget.getText();
  }
}
