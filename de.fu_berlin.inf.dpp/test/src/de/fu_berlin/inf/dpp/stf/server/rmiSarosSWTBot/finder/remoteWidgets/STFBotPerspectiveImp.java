package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets;

import java.rmi.RemoteException;

import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotPerspective;

public final class STFBotPerspectiveImp extends AbstractRmoteWidget implements
    STFBotPerspective {

    private static transient STFBotPerspectiveImp self;

    private SWTBotPerspective swtbotPerspective;

    /**
     * {@link STFBotPerspectiveImp} is a singleton, but inheritance is possible.
     */
    public static STFBotPerspectiveImp getInstance() {
        if (self != null)
            return self;
        self = new STFBotPerspectiveImp();
        return self;
    }

    public void setWidget(SWTBotPerspective pers) {
        this.swtbotPerspective = pers;
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
        swtbotPerspective.activate();
    }

    /**********************************************
     * 
     * states
     * 
     **********************************************/

    public String getLabel() throws RemoteException {
        return swtbotPerspective.getLabel();
    }

    public boolean isActive() throws RemoteException {
        return swtbotPerspective.isActive();
    }
}
