package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.contextMenu;

import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotTreeItem;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.Component;

public class SarosCImp extends Component implements SarosC {

    private static transient SarosCImp self;

    private STFBotTreeItem treeItem;

    /**
     * {@link SarosCImp} is a singleton, but inheritance is possible.
     */
    public static SarosCImp getInstance() {
        if (self != null)
            return self;
        self = new SarosCImp();
        return self;
    }

    public void setTreeItem(STFBotTreeItem treeItem) {
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

    public void shareProjectWith(String howToshareProject,
        String[] baseJIDOfInvitees) throws RemoteException {
        treeItem.contextMenu(CM_SAROS, howToshareProject).click();
        sarosBot().confirmShellInvitation(baseJIDOfInvitees);
    }

    public void multipleBuddies(String... baseJIDOfInvitees)
        throws RemoteException {
        // treeItem.contextMenu(CM_SAROS).contextMenu(CM_SHARE_PROJECT).click();
        treeItem.contextMenu(CM_SAROS, baseJIDOfInvitees[0]).click();
        sarosBot().confirmShellInvitation(baseJIDOfInvitees);
    }

}
