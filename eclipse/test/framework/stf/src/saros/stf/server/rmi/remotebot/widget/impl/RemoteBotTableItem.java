package saros.stf.server.rmi.remotebot.widget.impl;

import java.rmi.RemoteException;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTableItem;
import saros.stf.server.StfRemoteObject;
import saros.stf.server.rmi.remotebot.impl.RemoteWorkbenchBot;
import saros.stf.server.rmi.remotebot.widget.IRemoteBotMenu;
import saros.stf.server.rmi.remotebot.widget.IRemoteBotTableItem;

public final class RemoteBotTableItem extends StfRemoteObject implements IRemoteBotTableItem {

  private static final RemoteBotTableItem INSTANCE = new RemoteBotTableItem();

  private SWTBotTableItem widget;

  public static RemoteBotTableItem getInstance() {
    return INSTANCE;
  }

  public IRemoteBotTableItem setWidget(SWTBotTableItem tableItem) {
    this.widget = tableItem;
    return this;
  }

  @Override
  public IRemoteBotMenu contextMenu(String text) throws RemoteException {
    return RemoteBotMenu.getInstance().setWidget(widget.contextMenu(text));
  }

  @Override
  public void select() throws RemoteException {
    widget.select();
  }

  @Override
  public void check() throws RemoteException {
    widget.check();
  }

  @Override
  public void uncheck() throws RemoteException {
    widget.uncheck();
  }

  @Override
  public void toggleCheck() throws RemoteException {
    widget.toggleCheck();
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
  public boolean existsContextMenu(String contextName) throws RemoteException {

    try {
      widget.contextMenu(contextName);
      return true;
    } catch (WidgetNotFoundException e) {
      return false;
    }
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
  public boolean isChecked() throws RemoteException {
    return widget.isChecked();
  }

  @Override
  public boolean isGrayed() throws RemoteException {
    return widget.isGrayed();
  }

  @Override
  public String getText(int index) throws RemoteException {
    return widget.getText(index);
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
