package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.remoteFinder.remoteWidgets;

import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.widgets.SWTBotCTabItem;

public class RemoteBotCTabItem extends AbstractRmoteWidget implements
    IRemoteBotCTabItem {

    private static transient RemoteBotCTabItem self;

    private SWTBotCTabItem widget;

    /**
     * {@link RemoteBotCTabItem} is a singleton, but inheritance is possible.
     */
    public static RemoteBotCTabItem getInstance() {
        if (self != null)
            return self;
        self = new RemoteBotCTabItem();
        return self;
    }

    public IRemoteBotCTabItem setWidget(SWTBotCTabItem widget) {
        this.widget = widget;
        return this;

    }

    /**************************************************************
     * 
     * exported functions
     * 
     **************************************************************/

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
}