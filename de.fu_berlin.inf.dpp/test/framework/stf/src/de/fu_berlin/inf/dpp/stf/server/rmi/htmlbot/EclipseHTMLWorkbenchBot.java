package de.fu_berlin.inf.dpp.stf.server.rmi.htmlbot;

import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.IRemoteWorkbenchBot;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.impl.RemoteWorkbenchBot;
import de.fu_berlin.inf.dpp.ui.views.SarosViewBrowserVersion;
import java.rmi.RemoteException;

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
