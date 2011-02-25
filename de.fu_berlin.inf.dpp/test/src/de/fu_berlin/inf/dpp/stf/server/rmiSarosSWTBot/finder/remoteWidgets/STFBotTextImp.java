package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets;

import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;

public class STFBotTextImp extends AbstractRmoteWidget implements STFBotText {

    private static transient STFBotTextImp self;

    private SWTBotText widget;

    /**
     * {@link STFBotTextImp} is a singleton, but inheritance is possible.
     */
    public static STFBotTextImp getInstance() {
        if (self != null)
            return self;
        self = new STFBotTextImp();
        return self;
    }

    public STFBotText setWidget(SWTBotText text) {
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

    public STFBotMenu contextMenu(String text) throws RemoteException {
        return stfBotMenu.setWidget(widget.contextMenu(text));

    }

    /**********************************************
     * 
     * actions
     * 
     **********************************************/

    public STFBotText selectAll() throws RemoteException {
        return setWidget(widget.selectAll());
    }

    public void setFocus() throws RemoteException {
        widget.setFocus();
    }

    public STFBotText setText(String text) throws RemoteException {
        return setWidget(widget.setText(text));
    }

    public STFBotText typeText(String text) throws RemoteException {
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
