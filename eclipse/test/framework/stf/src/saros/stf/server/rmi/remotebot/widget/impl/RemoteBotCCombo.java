package saros.stf.server.rmi.remotebot.widget.impl;

import java.rmi.RemoteException;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCCombo;
import saros.stf.server.StfRemoteObject;
import saros.stf.server.rmi.remotebot.widget.IRemoteBotCCombo;
import saros.stf.server.rmi.remotebot.widget.IRemoteBotMenu;

public final class RemoteBotCCombo extends StfRemoteObject implements IRemoteBotCCombo {

  private static final RemoteBotCCombo INSTANCE = new RemoteBotCCombo();

  private SWTBotCCombo widget;

  public static RemoteBotCCombo getInstance() {
    return INSTANCE;
  }

  public IRemoteBotCCombo setWidget(SWTBotCCombo ccomb) {
    this.widget = ccomb;
    return this;
  }

  @Override
  public IRemoteBotMenu contextMenu(String text) throws RemoteException {
    return RemoteBotMenu.getInstance().setWidget(widget.contextMenu(text));
  }

  @Override
  public void setSelection(int indexOfSelection) throws RemoteException {
    widget.setSelection(indexOfSelection);
  }

  @Override
  public String selection() throws RemoteException {
    return widget.selection();
  }

  @Override
  public int selectionIndex() throws RemoteException {
    return widget.selectionIndex();
  }

  @Override
  public void setSelection(String text) throws RemoteException {
    widget.setSelection(text);
  }

  @Override
  public void setText(String text) throws RemoteException {
    widget.setText(text);
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
  public int itemCount() throws RemoteException {
    return widget.itemCount();
  }

  @Override
  public String[] items() throws RemoteException {
    return widget.items();
  }

  @Override
  public int textLimit() throws RemoteException {
    return widget.textLimit();
  }
}
