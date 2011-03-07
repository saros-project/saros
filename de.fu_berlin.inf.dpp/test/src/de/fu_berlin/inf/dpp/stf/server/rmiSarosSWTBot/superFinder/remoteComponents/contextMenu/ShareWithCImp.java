package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.contextMenu;

import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.RemoteBotTreeItem;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.Component;

public class ShareWithCImp extends Component implements ShareWithC {

    private static transient ShareWithCImp self;

    private RemoteBotTreeItem treeItem;

    /**
     * {@link ShareWithCImp} is a singleton, but inheritance is possible.
     */
    public static ShareWithCImp getInstance() {
        if (self != null)
            return self;
        self = new ShareWithCImp();
        return self;
    }

    public void setTreeItem(RemoteBotTreeItem treeItem) {
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
        treeItem.contextMenu(CM_SHARE_WITH, CM_MULTIPLE_BUDDIES).click();
        sarosBot().confirmShellShareProject(projectName, baseJIDOfInvitees);
    }

    public void buddy(JID jid) throws RemoteException {
        treeItem.contextMenu(CM_SHARE_WITH, jid.getBase()).click();
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
