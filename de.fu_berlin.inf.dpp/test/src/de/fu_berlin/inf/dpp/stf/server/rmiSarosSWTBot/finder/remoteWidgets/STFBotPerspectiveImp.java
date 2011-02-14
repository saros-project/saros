package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets;

import java.rmi.RemoteException;

import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotPerspective;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.EclipseComponentImp;

public final class STFBotPerspectiveImp extends EclipseComponentImp implements
    STFBotPerspective {

    private static transient STFBotPerspectiveImp self;
    private String label;

    private String id;

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

    public void setLabel(String label) {
        if (this.label == null || !this.label.equals(label)) {
            this.label = label;
            swtbotPerspective = bot.perspectiveByLabel(label);
        }
    }

    public void setId(String id) {
        if (this.id == null || !this.id.equals(id)) {
            this.id = id;
            swtbotPerspective = bot.perspectiveById(id);
        }
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
