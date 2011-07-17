package de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.contextmenu.peview.submenu.impl;

import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.server.StfRemoteObject;
import de.fu_berlin.inf.dpp.stf.server.bot.widget.ContextMenuHelper;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.contextmenu.peview.submenu.IShareWithC;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.impl.SuperBot;

public final class ShareWithC extends StfRemoteObject implements IShareWithC {

    private static final ShareWithC INSTANCE = new ShareWithC();

    private SWTBotTree tree;
    private SWTBotTreeItem treeItem;

    public static ShareWithC getInstance() {
        return INSTANCE;
    }

    public void setTree(SWTBotTree tree) {
        this.tree = tree;
    }

    public void setTreeItem(SWTBotTreeItem treeItem) {
        this.treeItem = treeItem;
    }

    // FIXME can not click the context menu.
    public void multipleBuddies(String projectName, JID... baseJIDOfInvitees)
        throws RemoteException {
        treeItem.select();
        ContextMenuHelper.clickContextMenu(tree, CM_SHARE_WITH,
            CM_MULTIPLE_BUDDIES);
        SuperBot.getInstance().confirmShellShareProjects(projectName,
            baseJIDOfInvitees);
    }

    public void buddy(JID jid) throws RemoteException {
        treeItem.select();
        ContextMenuHelper.clickContextMenu(tree, CM_SHARE_WITH, jid.getBase());
    }

    public void addToSarosSession() {
        /*
         * The menu is only activated if there are project existed in the
         * package explorer view, which is not in the session.
         */
    }

    public void stopToSarosSession() {
        //
    }

}
