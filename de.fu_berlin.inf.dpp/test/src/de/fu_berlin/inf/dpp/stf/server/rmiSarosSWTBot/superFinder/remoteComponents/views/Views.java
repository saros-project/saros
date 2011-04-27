package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.views;

import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.Component;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.contextMenu.BuddiesContextMenuWrapper;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.contextMenu.ContextMenuWrapper;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.contextMenu.SarosContextMenuWrapper;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.views.sarosViews.ChatView;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.views.sarosViews.IChatView;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.views.sarosViews.IRSView;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.views.sarosViews.ISarosView;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.views.sarosViews.ISessionView;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.views.sarosViews.RSView;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.views.sarosViews.SarosView;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.views.sarosViews.SessionView;

public class Views extends Component implements IViews {

    private static transient Views self;

    protected static ContextMenuWrapper contextMenu;
    protected static SarosContextMenuWrapper sarosContextMenu = SarosContextMenuWrapper
        .getInstance();
    protected static BuddiesContextMenuWrapper buddiesContextMenu = BuddiesContextMenuWrapper
        .getInstance();

    private static ChatView chatV;
    private static SarosView rosterV;
    private static RSView rsV;
    private static SessionView sessionV;
    private static ConsoleView consoleV;
    private static PEView pEV;
    private static ProgressView progressvV;

    /**
     * {@link Views} is a singleton, but inheritance is possible.
     */
    public static Views getInstance() {
        if (self != null)
            return self;
        self = new Views();
        chatV = ChatView.getInstance();
        rosterV = SarosView.getInstance();
        rsV = RSView.getInstance();
        sessionV = SessionView.getInstance();
        consoleV = ConsoleView.getInstance();
        pEV = PEView.getInstance();
        progressvV = ProgressView.getInstance();
        contextMenu = ContextMenuWrapper.getInstance();
        return self;
    }

    public IChatView chatView() throws RemoteException {
        bot().openViewById(VIEW_SAROS_ID);
        bot().view(VIEW_SAROS).show();
        return chatV.setView(bot().view(VIEW_SAROS));
    }

    public ISarosView sarosView() throws RemoteException {
        bot().openViewById(VIEW_SAROS_ID);
        bot().view(VIEW_SAROS).show();
        return rosterV.setView(bot().view(VIEW_SAROS));
    }

    public IRSView remoteScreenView() throws RemoteException {
        bot().openViewById(VIEW_REMOTE_SCREEN_ID);
        bot().view(VIEW_REMOTE_SCREEN).show();
        return rsV.setView(bot().view(VIEW_REMOTE_SCREEN));
    }

    public ISessionView sessionView() throws RemoteException {
        bot().openViewById(VIEW_SAROS_ID);
        bot().view(VIEW_SAROS).show();
        return sessionV.setView(bot().view(VIEW_SAROS));
    }

    public IConsoleView consoleView() throws RemoteException {
        return consoleV;
    }

    public IPEView packageExplorerView() throws RemoteException {
        bot().openViewById(VIEW_PACKAGE_EXPLORER_ID);
        bot().view(VIEW_PACKAGE_EXPLORER).show();
        return pEV.setView(bot().view(VIEW_PACKAGE_EXPLORER));
    }

    public IProgressView progressView() throws RemoteException {
        bot().openViewById(VIEW_PROGRESS_ID);
        bot().view(VIEW_PROGRESS).show();
        return progressvV.setView(bot().view(VIEW_PROGRESS));
    }

}
