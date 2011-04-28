package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.remoteFinder.remoteWidgets;

import java.rmi.RemoteException;

import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotViewMenu;

public class RemoteBotViewMenu extends AbstractRmoteWidget implements
    IRemoteBotViewMenu {

    private static transient RemoteBotViewMenu self;

    private SWTBotViewMenu widget;

    /**
     * {@link RemoteBotViewMenu} is a singleton, but inheritance is possible.
     */
    public static RemoteBotViewMenu getInstance() {
        if (self != null)
            return self;
        self = new RemoteBotViewMenu();
        return self;
    }

    public IRemoteBotViewMenu setWidget(SWTBotViewMenu viewMenu) {
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
