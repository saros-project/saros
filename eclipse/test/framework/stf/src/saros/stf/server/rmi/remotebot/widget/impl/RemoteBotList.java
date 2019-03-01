package saros.stf.server.rmi.remotebot.widget.impl;

import java.rmi.RemoteException;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotList;
import saros.stf.server.StfRemoteObject;
import saros.stf.server.rmi.remotebot.widget.IRemoteBotList;
import saros.stf.server.rmi.remotebot.widget.IRemoteBotMenu;

public final class RemoteBotList extends StfRemoteObject implements IRemoteBotList {

  private static final RemoteBotList INSTANCE = new RemoteBotList();

  private SWTBotList widget;

  public static RemoteBotList getInstance() {
    return INSTANCE;
  }

  public IRemoteBotList setWidget(SWTBotList list) {
    this.widget = list;
    return this;
  }

  @Override
  public IRemoteBotMenu contextMenu(String text) throws RemoteException {
    return RemoteBotMenu.getInstance().setWidget(widget.contextMenu(text));
  }

  @Override
  public void select(String item) throws RemoteException {
    widget.select(item);
  }

  @Override
  public void select(int... indices) throws RemoteException {
    widget.select(indices);
  }

  @Override
  public void select(int index) throws RemoteException {
    widget.select(index);
  }

  @Override
  public String[] selection() throws RemoteException {
    return widget.selection();
  }

  @Override
  public void select(String... items) throws RemoteException {
    widget.select(items);
  }

  @Override
  public int selectionCount() throws RemoteException {
    return widget.selectionCount();
  }

  @Override
  public void unselect() throws RemoteException {
    widget.unselect();
  }

  @Override
  public void setFocus() throws RemoteException {
    widget.setFocus();
  }

  @Override
  public String itemAt(int index) throws RemoteException {
    return widget.itemAt(index);
  }

  @Override
  public int itemCount() throws RemoteException {
    return widget.itemCount();
  }

  @Override
  public int indexOf(String item) throws RemoteException {
    return widget.indexOf(item);
  }

  @Override
  public String[] getItems() throws RemoteException {
    return widget.getItems();
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
}
