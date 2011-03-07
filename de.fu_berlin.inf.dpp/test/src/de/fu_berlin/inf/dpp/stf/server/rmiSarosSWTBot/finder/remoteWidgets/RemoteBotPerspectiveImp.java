package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets;

import java.rmi.RemoteException;

import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotPerspective;

public final class RemoteBotPerspectiveImp extends AbstractRmoteWidget implements
    RemoteBotPerspective {

    private static transient RemoteBotPerspectiveImp self;

    private SWTBotPerspective widget;

    /**
     * {@link RemoteBotPerspectiveImp} is a singleton, but inheritance is possible.
     */
    public static RemoteBotPerspectiveImp getInstance() {
        if (self != null)
            return self;
        self = new RemoteBotPerspectiveImp();
        return self;
    }

    public RemoteBotPerspective setWidget(SWTBotPerspective pers) {
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
