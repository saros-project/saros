package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets;

import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarPushButton;

public class STFBotToolbarPushButtonImp extends AbstractRmoteWidget implements
    STFBotToolbarPushButton {

    private static transient STFBotToolbarPushButtonImp self;

    private SWTBotToolbarPushButton widget;

    /**
     * {@link STFBotToolbarPushButtonImp} is a singleton, but inheritance is
     * possible.
     */
    public static STFBotToolbarPushButtonImp getInstance() {
        if (self != null)
            return self;
        self = new STFBotToolbarPushButtonImp();
        return self;
    }

    public STFBotToolbarPushButton setWidget(
        SWTBotToolbarPushButton toolbarPushButton) {
        this.widget = toolbarPushButton;
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
    public void click() throws RemoteException {
        widget.click();
    }

    public void clickAndWait() throws RemoteException {
        waitUntilIsEnabled();
        click();
    }

    public void setFocus() throws RemoteException {
        widget.setFocus();
    }

    /**********************************************
     * 
     * states
     * 
     **********************************************/
    public boolean isEnabled() throws RemoteException {
        return widget.isEnabled();
    }

    public boolean isVisible() throws RemoteException {
        return widget.isVisible();
    }

    public boolean isActive() throws RemoteException {
        return widget.isActive();
    }

    public String getText() throws RemoteException {
        return widget.getText();
    }

    public String getToolTipText() throws RemoteException {
        return widget.getText();
    }

    /**********************************************
     * 
     * waits until
     * 
     **********************************************/
    public void waitUntilIsEnabled() throws RemoteException {
        stfBot.waitUntil(Conditions.widgetIsEnabled(widget));
    }

}
