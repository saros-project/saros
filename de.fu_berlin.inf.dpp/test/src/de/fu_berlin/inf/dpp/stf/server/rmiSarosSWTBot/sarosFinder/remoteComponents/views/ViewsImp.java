package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.views;

import java.rmi.RemoteException;
import java.util.List;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotTree;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotTreeItem;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotView;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.Component;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.contextMenu.ContextMenuWrapperImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.contextMenu.SarosContextMenuWrapperImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.views.sarosViews.BuddiesView;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.views.sarosViews.BuddiesViewImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.views.sarosViews.ChatView;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.views.sarosViews.ChatViewImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.views.sarosViews.RSView;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.views.sarosViews.RSViewImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.views.sarosViews.SessionView;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.views.sarosViews.SessionViewImp;

public class ViewsImp extends Component implements Views {

    private static transient ViewsImp self;

    protected STFBotView view;
    protected STFBotTree tree;
    protected STFBotTreeItem treeItem;

    protected static ContextMenuWrapperImp contextMenu;
    protected static SarosContextMenuWrapperImp sarosContextMenu = SarosContextMenuWrapperImp
        .getInstance();

    private static ChatViewImp chatV;
    private static BuddiesViewImp rosterV;
    private static RSViewImp rsV;
    private static SessionViewImp sessionV;
    private static ConsoleViewImp consoleV;
    private static PEViewImp pEV;
    private static ProgressViewImp progressvV;

    /**
     * {@link ViewsImp} is a singleton, but inheritance is possible.
     */
    public static ViewsImp getInstance() {
        if (self != null)
            return self;
        self = new ViewsImp();
        chatV = ChatViewImp.getInstance();
        rosterV = BuddiesViewImp.getInstance();
        rsV = RSViewImp.getInstance();
        sessionV = SessionViewImp.getInstance();
        consoleV = ConsoleViewImp.getInstance();
        pEV = PEViewImp.getInstance();
        progressvV = ProgressViewImp.getInstance();

        return self;
    }

    public ChatView chatView() throws RemoteException {
        bot().openViewById(VIEW_SAROS_CHAT_ID);
        bot().view(VIEW_SAROS_CHAT).show();
        return chatV.setView(bot().view(VIEW_SAROS_CHAT));
    }

    public BuddiesView buddiesView() throws RemoteException {
        bot().openViewById(VIEW_SAROS_BUDDIES_ID);
        bot().view(VIEW_SAROS_BUDDIES).show();
        return rosterV.setView(bot().view(VIEW_SAROS_BUDDIES));
    }

    public RSView remoteScreenView() throws RemoteException {
        bot().openViewById(VIEW_REMOTE_SCREEN_ID);
        bot().view(VIEW_REMOTE_SCREEN).show();
        return rsV.setView(bot().view(VIEW_REMOTE_SCREEN));
    }

    public SessionView sessionView() throws RemoteException {
        bot().openViewById(VIEW_SAROS_SESSION_ID);
        bot().view(VIEW_SAROS_SESSION).show();
        return sessionV.setView(bot().view(VIEW_SAROS_SESSION));
    }

    public ConsoleView consoleView() throws RemoteException {
        return consoleV;
    }

    public PEView packageExplorerView() throws RemoteException {
        bot().openViewById(VIEW_PACKAGE_EXPLORER_ID);
        bot().view(VIEW_PACKAGE_EXPLORER).show();
        return pEV.setView(bot().view(VIEW_PACKAGE_EXPLORER));
    }

    public ProgressView progressView() throws RemoteException {
        bot().openViewById(VIEW_PROGRESS_ID);
        bot().view(VIEW_PROGRESS).show();
        return progressvV.setView(bot().view(VIEW_PROGRESS));
    }

    protected static void setContextMenu() {
        contextMenu = ContextMenuWrapperImp.getInstance();
    }

    protected void setWidgets(STFBotView view) throws RemoteException {
        this.view = view;
        tree = view.bot().tree();
        treeItem = null;
    }

    protected void initContextMenuWrapper(STFBotTreeItem treeItem,
        TreeItemType type) {
        this.treeItem = treeItem;
        contextMenu.setTree(tree);
        contextMenu.setTreeItem(treeItem);
        contextMenu.setTreeItemType(type);
    }

    protected void initContextMenuWrapper(STFBotTreeItem treeItem) {
        this.treeItem = treeItem;
        contextMenu.setTree(tree);
        contextMenu.setTreeItem(treeItem);
    }

    protected void initSarosContextMenuWrapper(STFBotTreeItem treeItem) {
        this.treeItem = treeItem;
        sarosContextMenu.setTree(tree);
        sarosContextMenu.setTreeItem(treeItem);
    }

    protected void clickToolbarButtonWithTooltip(String tooltipText)
        throws RemoteException {
        if (!view.existsToolbarButton(tooltipText))
            throw new RuntimeException("The toolbarbutton " + tooltipText
                + " doesn't exist!");
        view.toolbarButton(tooltipText).click();
    }

    protected boolean isToolbarButtonEnabled(String tooltip)
        throws RemoteException {
        if (!view.existsToolbarButton(tooltip))
            return false;
        return view.toolbarButton(tooltip).isEnabled();

    }

    protected List<String> getToolTipTextOfToolbarButtons()
        throws RemoteException {
        return view.getToolTipTextOfToolbarButtons();
    }

}
