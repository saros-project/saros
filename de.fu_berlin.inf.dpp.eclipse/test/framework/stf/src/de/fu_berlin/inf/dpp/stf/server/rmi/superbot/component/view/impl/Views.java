package de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.view.impl;

import de.fu_berlin.inf.dpp.stf.server.StfRemoteObject;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.impl.RemoteWorkbenchBot;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.view.IViews;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.view.eclipse.IConsoleView;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.view.eclipse.IPackageExplorerView;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.view.eclipse.IProgressView;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.view.eclipse.impl.ConsoleView;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.view.eclipse.impl.PackageExplorerView;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.view.eclipse.impl.ProgressView;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.view.saros.ISarosView;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.view.saros.impl.SarosView;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.view.whiteboard.ISarosWhiteboardView;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.view.whiteboard.impl.SarosWhiteboardView;
import java.rmi.RemoteException;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;

public final class Views extends StfRemoteObject implements IViews {

  private static final Views INSTANCE = new Views();

  public static Views getInstance() {
    return INSTANCE;
  }

  @Override
  public ISarosView sarosView() throws RemoteException {
    SWTWorkbenchBot bot = new SWTWorkbenchBot();
    RemoteWorkbenchBot.getInstance().openViewById(VIEW_SAROS_ID);
    bot.viewByTitle(VIEW_SAROS).show();
    return SarosView.getInstance().setView(bot.viewByTitle(VIEW_SAROS));
  }

  @Override
  public ISarosWhiteboardView sarosWhiteboardView() throws RemoteException {
    SWTWorkbenchBot bot = new SWTWorkbenchBot();
    RemoteWorkbenchBot.getInstance().openViewById(VIEW_SAROS_WHITEBOARD_ID);
    bot.viewByTitle(VIEW_SAROS_WHITEBOARD).show();
    return SarosWhiteboardView.getInstance().setView(bot.viewByTitle(VIEW_SAROS_WHITEBOARD), bot);
  }

  @Override
  public IConsoleView consoleView() throws RemoteException {
    SWTWorkbenchBot bot = new SWTWorkbenchBot();
    RemoteWorkbenchBot.getInstance().openViewById(VIEW_CONSOLE_ID);
    RemoteWorkbenchBot.getInstance().view(VIEW_CONSOLE).show();
    bot.viewByTitle(VIEW_CONSOLE).show();
    return ConsoleView.getInstance().setView(bot.viewByTitle(VIEW_CONSOLE));
  }

  @Override
  public IPackageExplorerView packageExplorerView() throws RemoteException {

    SWTWorkbenchBot bot = new SWTWorkbenchBot();
    RemoteWorkbenchBot.getInstance().openViewById(VIEW_PACKAGE_EXPLORER_ID);
    bot.viewByTitle(VIEW_PACKAGE_EXPLORER).show();
    return PackageExplorerView.getInstance().setView(bot.viewByTitle(VIEW_PACKAGE_EXPLORER));
  }

  @Override
  public IProgressView progressView() throws RemoteException {
    RemoteWorkbenchBot.getInstance().openViewById(VIEW_PROGRESS_ID);

    SWTWorkbenchBot bot = new SWTWorkbenchBot();
    bot.viewByTitle(VIEW_PROGRESS).show();
    return ProgressView.getInstance().setView(bot.viewByTitle(VIEW_PROGRESS));
  }
}
