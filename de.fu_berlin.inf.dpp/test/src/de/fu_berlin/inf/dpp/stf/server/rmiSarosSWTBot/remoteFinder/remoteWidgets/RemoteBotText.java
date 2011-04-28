package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.remoteFinder.remoteWidgets;

import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;

public class RemoteBotText extends AbstractRmoteWidget implements IRemoteBotText {

    private static transient RemoteBotText self;

    private SWTBotText widget;

    /**
     * {@link RemoteBotText} is a singleton, but inheritance is possible.
     */
    public static RemoteBotText getInstance() {
        if (self != null)
            return self;
        self = new RemoteBotText();
        return self;
    }

    public IRemoteBotText setWidget(SWTBotText text) {
        this.widget = text;
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

    public IRemoteBotText selectAll() throws RemoteException {
        return setWidget(widget.selectAll());
    }

    public void setFocus() throws RemoteException {
        widget.setFocus();
    }

    public IRemoteBotText setText(String text) throws RemoteException {
        return setWidget(widget.setText(text));
    }

    public IRemoteBotText typeText(String text) throws RemoteException {
        return setWidget(widget.typeText(text));
    }

    /**********************************************
     * 
     * states
     * 
     **********************************************/

    public String getText() throws RemoteException {
        return widget.getText();
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

}
