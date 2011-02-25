package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets;

import java.rmi.RemoteException;

import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotViewMenu;

public class STFBotViewMenuImp extends AbstractRmoteWidget implements
    STFBotViewMenu {

    private static transient STFBotViewMenuImp self;

    private SWTBotViewMenu widget;

    /**
     * {@link STFBotViewMenuImp} is a singleton, but inheritance is possible.
     */
    public static STFBotViewMenuImp getInstance() {
        if (self != null)
            return self;
        self = new STFBotViewMenuImp();
        return self;
    }

    public STFBotViewMenu setWidget(SWTBotViewMenu viewMenu) {
        this.widget = viewMenu;
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
    public void click() throws RemoteException {
        widget.click();
    }

    /**********************************************
     * 
     * states
     * 
     **********************************************/

    public String getToolTipText() throws RemoteException {
        return widget.getText();
    }

    public String getText() throws RemoteException {
        return widget.getText();
    }

    public boolean isChecked() throws RemoteException {
        return widget.isChecked();
    }

}
