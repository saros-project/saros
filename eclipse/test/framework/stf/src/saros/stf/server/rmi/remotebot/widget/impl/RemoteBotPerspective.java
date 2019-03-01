package saros.stf.server.rmi.remotebot.widget.impl;

import java.rmi.RemoteException;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotPerspective;
import saros.stf.server.StfRemoteObject;
import saros.stf.server.rmi.remotebot.widget.IRemoteBotPerspective;

public final class RemoteBotPerspective extends StfRemoteObject implements IRemoteBotPerspective {

  private static final RemoteBotPerspective INSTANCE = new RemoteBotPerspective();

  private SWTBotPerspective widget;

  public static RemoteBotPerspective getInstance() {
    return INSTANCE;
  }

  public IRemoteBotPerspective setWidget(SWTBotPerspective perspective) {
    this.widget = perspective;
    return this;
  }

  @Override
  public void activate() throws RemoteException {
    widget.activate();
  }

  @Override
  public String getLabel() throws RemoteException {
    return widget.getLabel();
  }

  @Override
  public boolean isActive() throws RemoteException {
    return widget.isActive();
  }
}
