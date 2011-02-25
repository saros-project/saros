package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets;

import java.rmi.RemoteException;

import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotPerspective;

public final class STFBotPerspectiveImp extends AbstractRmoteWidget implements
    STFBotPerspective {

    private static transient STFBotPerspectiveImp self;

    private SWTBotPerspective widget;

    /**
     * {@link STFBotPerspectiveImp} is a singleton, but inheritance is possible.
     */
    public static STFBotPerspectiveImp getInstance() {
        if (self != null)
            return self;
        self = new STFBotPerspectiveImp();
        return self;
    }

    public STFBotPerspective setWidget(SWTBotPerspective pers) {
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
