package de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.impl;

import de.fu_berlin.inf.dpp.stf.server.StfRemoteObject;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.impl.RemoteWorkbenchBot;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotMenu;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotRadio;
import java.rmi.RemoteException;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotRadio;

public final class RemoteBotRadio extends StfRemoteObject implements IRemoteBotRadio {

  private static final RemoteBotRadio INSTANCE = new RemoteBotRadio();

  private SWTBotRadio swtBotRadio;

  public static RemoteBotRadio getInstance() {
    return INSTANCE;
  }

  public IRemoteBotRadio setWidget(SWTBotRadio radio) {
    this.swtBotRadio = radio;
    return this;
  }

  @Override
  public IRemoteBotMenu contextMenu(String text) throws RemoteException {
    return RemoteBotMenu.getInstance().setWidget(swtBotRadio.contextMenu(text));
  }

  @Override
  public void click() throws RemoteException {
    swtBotRadio.click();
  }

  @Override
  public void clickAndWait() throws RemoteException {
    waitUntilIsEnabled();
    click();
  }

  @Override
  public void setFocus() throws RemoteException {
    swtBotRadio.setFocus();
  }

  @Override
  public boolean isEnabled() throws RemoteException {
    return swtBotRadio.isEnabled();
  }

  @Override
  public boolean isVisible() throws RemoteException {
    return swtBotRadio.isVisible();
  }

  @Override
  public boolean isActive() throws RemoteException {
    return swtBotRadio.isActive();
  }

  @Override
  public String getText() throws RemoteException {
    return swtBotRadio.getText();
  }

  @Override
  public String getToolTipText() throws RemoteException {
    return swtBotRadio.getText();
  }

  @Override
  public boolean isSelected() throws RemoteException {
    return swtBotRadio.isSelected();
  }

  @Override
  public void waitUntilIsEnabled() throws RemoteException {
    RemoteWorkbenchBot.getInstance().waitUntil(Conditions.widgetIsEnabled(swtBotRadio));
  }
}
