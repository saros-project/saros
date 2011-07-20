package de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.view.impl;

import java.rmi.RemoteException;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;

import de.fu_berlin.inf.dpp.stf.server.StfRemoteObject;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.impl.RemoteWorkbenchBot;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.view.IViews;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.view.eclipse.IConsoleView;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.view.eclipse.IPackageExplorerView;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.view.eclipse.IProgressView;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.view.eclipse.impl.ConsoleView;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.view.eclipse.impl.PackageExplorerView;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.view.eclipse.impl.ProgressView;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.view.saros.IRSView;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.view.saros.ISarosView;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.view.saros.impl.RSView;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.view.saros.impl.SarosView;

public final class Views extends StfRemoteObject implements IViews {

    private static final Views INSTANCE = new Views();

    public static Views getInstance() {
        return INSTANCE;
    }

    public ISarosView sarosView() throws RemoteException {
        SWTWorkbenchBot bot = new SWTWorkbenchBot();
        RemoteWorkbenchBot.getInstance().openViewById(VIEW_SAROS_ID);
        bot.viewByTitle(VIEW_SAROS).show();
        return SarosView.getInstance().setView(bot.viewByTitle(VIEW_SAROS));
    }

    public IRSView remoteScreenView() throws RemoteException {

        SWTWorkbenchBot bot = new SWTWorkbenchBot();
        RemoteWorkbenchBot.getInstance().openViewById(VIEW_REMOTE_SCREEN_ID);
        RemoteWorkbenchBot.getInstance().view(VIEW_REMOTE_SCREEN).show();
        bot.viewByTitle(VIEW_REMOTE_SCREEN).show();
        return RSView.getInstance()
            .setView(bot.viewByTitle(VIEW_REMOTE_SCREEN));
    }

    public IConsoleView consoleView() throws RemoteException {
        SWTWorkbenchBot bot = new SWTWorkbenchBot();
        RemoteWorkbenchBot.getInstance().openViewById(VIEW_CONSOLE_ID);
        RemoteWorkbenchBot.getInstance().view(VIEW_CONSOLE).show();
        bot.viewByTitle(VIEW_CONSOLE).show();
        return ConsoleView.getInstance().setView(bot.viewByTitle(VIEW_CONSOLE));
    }

    public IPackageExplorerView packageExplorerView() throws RemoteException {

        SWTWorkbenchBot bot = new SWTWorkbenchBot();
        RemoteWorkbenchBot.getInstance().openViewById(VIEW_PACKAGE_EXPLORER_ID);
        bot.viewByTitle(VIEW_PACKAGE_EXPLORER).show();
        return PackageExplorerView.getInstance().setView(
            bot.viewByTitle(VIEW_PACKAGE_EXPLORER));
    }

    public IProgressView progressView() throws RemoteException {
        RemoteWorkbenchBot.getInstance().openViewById(VIEW_PROGRESS_ID);
        RemoteWorkbenchBot.getInstance().view(VIEW_PROGRESS).show();
        return ProgressView.getInstance().setView(
            RemoteWorkbenchBot.getInstance().view(VIEW_PROGRESS));
    }

}
