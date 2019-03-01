package de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.impl;

import de.fu_berlin.inf.dpp.stf.server.StfRemoteObject;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.impl.RemoteWorkbenchBot;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotMenu;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotToolbarButton;
import java.rmi.RemoteException;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarButton;

public final class RemoteBotToolbarButton extends StfRemoteObject
    implements IRemoteBotToolbarButton {

  private static final RemoteBotToolbarButton INSTANCE = new RemoteBotToolbarButton();

  private SWTBotToolbarButton toolbarButton;

  public static RemoteBotToolbarButton getInstance() {
    return INSTANCE;
  }

  public IRemoteBotToolbarButton setWidget(SWTBotToolbarButton toolbarButton) {
    this.toolbarButton = toolbarButton;
    return this;
  }

  @Override
  public IRemoteBotMenu contextMenu(String text) throws RemoteException {
    return RemoteBotMenu.getInstance().setWidget(toolbarButton.contextMenu(text));
  }

  @Override
  public void click() throws RemoteException {
    toolbarButton.click();
  }

  @Override
  public void clickAndWait() throws RemoteException {
    waitUntilIsEnabled();
    click();
  }

  @Override
  public void setFocus() throws RemoteException {
    toolbarButton.setFocus();
  }

  @Override
  public boolean isEnabled() throws RemoteException {
    return toolbarButton.isEnabled();
  }

  @Override
  public boolean isVisible() throws RemoteException {
    return toolbarButton.isVisible();
  }

  @Override
  public boolean isActive() throws RemoteException {
    return toolbarButton.isActive();
  }

  @Override
  public String getText() throws RemoteException {
    return toolbarButton.getText();
  }

  @Override
  public String getToolTipText() throws RemoteException {
    return toolbarButton.getToolTipText();
  }

  @Override
  public void waitUntilIsEnabled() throws RemoteException {
    RemoteWorkbenchBot.getInstance().waitUntil(Conditions.widgetIsEnabled(toolbarButton));
  }
}
