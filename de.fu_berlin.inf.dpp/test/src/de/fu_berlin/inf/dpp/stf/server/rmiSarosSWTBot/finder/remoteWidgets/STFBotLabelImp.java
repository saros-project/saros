package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets;

import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.widgets.SWTBotLabel;

public class STFBotLabelImp extends AbstractRmoteWidget implements STFBotLabel {

    private static transient STFBotLabelImp self;

    private SWTBotLabel swtBotLabel;

    /**
     * {@link STFBotTableImp} is a singleton, but inheritance is possible.
     */
    public static STFBotLabelImp getInstance() {
        if (self != null)
            return self;
        self = new STFBotLabelImp();
        return self;
    }

    public void setSwtBotLabel(SWTBotLabel label) {
        this.swtBotLabel = label;
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

    public void setFocus() throws RemoteException {
        swtBotLabel.setFocus();
    }

    /**********************************************
     * 
     * states
     * 
     **********************************************/

    public boolean isEnabled() throws RemoteException {
        return swtBotLabel.isEnabled();
    }

    public boolean isVisible() throws RemoteException {
        return swtBotLabel.isVisible();
    }

    public boolean isActive() throws RemoteException {
        return swtBotLabel.isActive();
    }

    public String getToolTipText() throws RemoteException {
        return swtBotLabel.getText();
    }

    public String getText() throws RemoteException {
        return swtBotLabel.getText();
    }

}
