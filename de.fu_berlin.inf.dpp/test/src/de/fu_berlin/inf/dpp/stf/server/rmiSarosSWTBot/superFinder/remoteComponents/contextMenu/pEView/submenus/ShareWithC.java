package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.contextMenu.pEView.submenus;

import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.remoteFinder.remoteWidgets.IRemoteBotTreeItem;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.Component;

public class ShareWithC extends Component implements IShareWithC {

    private static transient ShareWithC self;

    private IRemoteBotTreeItem treeItem;

    /**
     * {@link ShareWithC} is a singleton, but inheritance is possible.
     */
    public static ShareWithC getInstance() {
        if (self != null)
            return self;
        self = new ShareWithC();
        return self;
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
        superBot().confirmShellShareProject(projectName, baseJIDOfInvitees);
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
