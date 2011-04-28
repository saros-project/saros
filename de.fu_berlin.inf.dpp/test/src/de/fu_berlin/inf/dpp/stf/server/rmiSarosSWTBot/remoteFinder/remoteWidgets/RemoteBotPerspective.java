package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.remoteFinder.remoteWidgets;

import java.rmi.RemoteException;

import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotPerspective;

public final class RemoteBotPerspective extends AbstractRmoteWidget implements
    IRemoteBotPerspective {

    private static transient RemoteBotPerspective self;

    private SWTBotPerspective widget;

    /**
     * {@link RemoteBotPerspective} is a singleton, but inheritance is possible.
     */
    public static RemoteBotPerspective getInstance() {
        if (self != null)
            return self;
        self = new RemoteBotPerspective();
        return self;
    }

    public IRemoteBotPerspective setWidget(SWTBotPerspective pers) {
        this.widget = pers;
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

    public void activate() throws RemoteException {
        widget.activate();
    }

    /**********************************************
     * 
     * states
     * 
     **********************************************/

    public String getLabel() throws RemoteException {
        return widget.getLabel();
    }

    public boolean isActive() throws RemoteException {
        return widget.isActive();
    }
}
