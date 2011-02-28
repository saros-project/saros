package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.contextMenu;

import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotTreeItem;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.SarosComponentImp;

public class SarosCImp extends SarosComponentImp implements SarosC {

    private static transient SarosCImp self;

    private STFBotTreeItem view;

    /**
     * {@link SarosCImp} is a singleton, but inheritance is possible.
     */
    public static SarosCImp getInstance() {
        if (self != null)
            return self;
        self = new SarosCImp();
        return self;
    }

    public void setTreeItem(STFBotTreeItem view) {
        this.view = view;
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
        view.contextMenu(CM_SAROS, howToshareProject).click();
        sarosBot().confirmShellInvitation(baseJIDOfInvitees);
    }

    public void shareProject(String... baseJIDOfInvitees)
        throws RemoteException {
        view.contextMenu(CM_SAROS, CM_SHARE_PROJECT).click();
        sarosBot().confirmShellInvitation(baseJIDOfInvitees);
    }

}
