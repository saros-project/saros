package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets;

import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;

public class RemoteBotTextImp extends AbstractRmoteWidget implements RemoteBotText {

    private static transient RemoteBotTextImp self;

    private SWTBotText widget;

    /**
     * {@link RemoteBotTextImp} is a singleton, but inheritance is possible.
     */
    public static RemoteBotTextImp getInstance() {
        if (self != null)
            return self;
        self = new RemoteBotTextImp();
        return self;
    }

    public RemoteBotText setWidget(SWTBotText text) {
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

    public RemoteBotMenu contextMenu(String text) throws RemoteException {
        return stfBotMenu.setWidget(widget.contextMenu(text));

    }

    /**********************************************
     * 
     * actions
     * 
     **********************************************/

    public RemoteBotText selectAll() throws RemoteException {
        return setWidget(widget.selectAll());
    }

    public void setFocus() throws RemoteException {
        widget.setFocus();
    }

    public RemoteBotText setText(String text) throws RemoteException {
        return setWidget(widget.setText(text));
    }

    public RemoteBotText typeText(String text) throws RemoteException {
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
