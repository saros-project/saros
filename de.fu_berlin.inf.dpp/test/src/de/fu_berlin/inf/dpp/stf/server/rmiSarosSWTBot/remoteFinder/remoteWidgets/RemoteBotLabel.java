package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.remoteFinder.remoteWidgets;

import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.widgets.SWTBotLabel;

public class RemoteBotLabel extends AbstractRmoteWidget implements IRemoteBotLabel {

    private static transient RemoteBotLabel self;

    private SWTBotLabel widget;

    /**
     * {@link RemoteBotTable} is a singleton, but inheritance is possible.
     */
    public static RemoteBotLabel getInstance() {
        if (self != null)
            return self;
        self = new RemoteBotLabel();
        return self;
    }

    public IRemoteBotLabel setWidget(SWTBotLabel label) {
        this.widget = label;
        return this;
    }

    /**************************************************************
     * 
     * exported functions
     * 
     **************************************************************/

    /**********************************************
     * 
     * finders
     * 
     **********************************************/
    public IRemoteBotMenu contextMenu(String text) throws RemoteException {
        return stfBotMenu.setWidget(widget.contextMenu(text));
    }

    /**********************************************
     * 
     * actions
     * 
     **********************************************/

    public void setFocus() throws RemoteException {
        widget.setFocus();
    }

    /**********************************************
     * 
     * states
     * 
     **********************************************/

    public int alignment() throws RemoteException {
        return widget.alignment();
    }

    public boolean isEnabled() throws RemoteException {
        return widget.isEnabled();
    }

    public boolean isVisible() throws RemoteException {
        return widget.isVisible();
    }

    public boolean isActive() throws RemoteException {
        return widget.isActive();
    }

    public String getToolTipText() throws RemoteException {
        return widget.getText();
    }

    public String getText() throws RemoteException {
        return widget.getText();
    }

}
