package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.views.sarosViews;

import java.rmi.RemoteException;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.IRemoteBotTable;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.IRemoteBotView;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.views.Views;

/**
 * This implementation of {@link ISessionView}
 * 
 * @author lchen
 */
public class SessionView extends Views implements ISessionView {

    private static transient SessionView self;

    private IRemoteBotView view;

    private IRemoteBotTable table;

    /**
     * {@link SessionView} is a singleton, but inheritance is possible.
     */
    public static SessionView getInstance() {
        if (self != null)
            return self;
        self = new SessionView();
        return self;
    }

    public ISessionView setView(IRemoteBotView view) throws RemoteException {
        this.view = view;
        if (this.view.bot().existsTableInGroup("Session"))
            this.table = this.view.bot().tableInGroup("Session");
        return this;
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

    /**********************************************
     * 
     * States
     * 
     **********************************************/

    /**********************************************
     * 
     * Wait until
     * 
     **********************************************/

    /**************************************************************
     * 
     * Inner functions
     * 
     **************************************************************/

}
