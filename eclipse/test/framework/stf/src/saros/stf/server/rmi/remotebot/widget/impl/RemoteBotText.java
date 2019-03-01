package saros.stf.server.rmi.remotebot.widget.impl;

import java.rmi.RemoteException;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;
import saros.stf.server.StfRemoteObject;
import saros.stf.server.rmi.remotebot.widget.IRemoteBotMenu;
import saros.stf.server.rmi.remotebot.widget.IRemoteBotText;

public final class RemoteBotText extends StfRemoteObject implements IRemoteBotText {

  private static final RemoteBotText INSTANCE = new RemoteBotText();

  private SWTBotText widget;

  public static RemoteBotText getInstance() {
    return INSTANCE;
  }

  public IRemoteBotText setWidget(SWTBotText text) {
    this.widget = text;
    return this;
  }

  @Override
  public IRemoteBotMenu contextMenu(String text) throws RemoteException {
    return RemoteBotMenu.getInstance().setWidget(widget.contextMenu(text));
  }

  @Override
  public IRemoteBotText selectAll() throws RemoteException {
    return setWidget(widget.selectAll());
  }

  @Override
  public void setFocus() throws RemoteException {
    widget.setFocus();
  }

  @Override
  public IRemoteBotText setText(String text) throws RemoteException {
    return setWidget(widget.setText(text));
  }

  @Override
  public IRemoteBotText typeText(String text) throws RemoteException {
    return setWidget(widget.typeText(text));
  }

  @Override
  public String getText() throws RemoteException {
    return widget.getText();
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
}
