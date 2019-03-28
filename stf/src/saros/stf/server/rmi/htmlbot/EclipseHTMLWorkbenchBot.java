package saros.stf.server.rmi.htmlbot;

import java.rmi.RemoteException;
import saros.stf.server.rmi.remotebot.IRemoteWorkbenchBot;
import saros.stf.server.rmi.remotebot.impl.RemoteWorkbenchBot;
import saros.ui.views.SarosViewBrowserVersion;

public class EclipseHTMLWorkbenchBot implements IHTMLWorkbenchBot {

  private final IRemoteWorkbenchBot workbenchBot;

  private static final EclipseHTMLWorkbenchBot INSTANCE = new EclipseHTMLWorkbenchBot();

  public static EclipseHTMLWorkbenchBot getInstance() {
    return INSTANCE;
  }

  public EclipseHTMLWorkbenchBot() {
    workbenchBot = RemoteWorkbenchBot.getInstance();
  }

  @Override
  public void openSarosBrowserView() throws RemoteException {
    workbenchBot.openViewById(SarosViewBrowserVersion.ID);
  }

  @Override
  public boolean isSarosBrowserViewOpen() throws RemoteException {
    return workbenchBot.isViewOpen("Saros HTML GUI");
  }

  @Override
  public void closeSarosBrowserView() throws RemoteException {
    if (isSarosBrowserViewOpen()) workbenchBot.viewById(SarosViewBrowserVersion.ID).close();
  }
}
