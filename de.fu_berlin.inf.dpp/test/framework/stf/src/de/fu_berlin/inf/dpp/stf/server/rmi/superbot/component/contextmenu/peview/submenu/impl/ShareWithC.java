package de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.contextmenu.peview.submenu.impl;

import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.server.StfRemoteObject;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotTreeItem;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.contextmenu.peview.submenu.IShareWithC;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.impl.SuperBot;

public final class ShareWithC extends StfRemoteObject implements IShareWithC {

    private static final ShareWithC INSTANCE = new ShareWithC();

    private IRemoteBotTreeItem treeItem;

    public static ShareWithC getInstance() {
        return INSTANCE;
    }

    public void setTreeItem(IRemoteBotTreeItem treeItem) {
        this.treeItem = treeItem;
    }

    /**************************************************************
     * 
     * exported functions
     * 
     **************************************************************/

    /**********************************************
     * 
     * actions
     * 
     **********************************************/

    /**
     * FIXME can not click the context menu.
     */
    public void multipleBuddies(String projectName, JID... baseJIDOfInvitees)
        throws RemoteException {
        treeItem.contextMenus(CM_SHARE_WITH, CM_MULTIPLE_BUDDIES).click();
        SuperBot.getInstance().confirmShellShareProjects(projectName,
            baseJIDOfInvitees);
    }

    public void buddy(JID jid) throws RemoteException {
        treeItem.contextMenus(CM_SHARE_WITH, jid.getBase()).click();
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
