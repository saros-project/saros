package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets;

import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;

public class STFBotButtonImp extends AbstractRmoteWidget implements
    STFBotButton {

    private static transient STFBotButtonImp self;

    private SWTBotButton swtBotButton;

    /**
     * {@link STFBotButtonImp} is a singleton, but inheritance is possible.
     */
    public static STFBotButtonImp getInstance() {
        if (self != null)
            return self;
        self = new STFBotButtonImp();
        return self;
    }

    public void setSwtBotButton(SWTBotButton button) {
        this.swtBotButton = button;
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
        swtBotButton.click();
    }

    public void clickAndWait() throws RemoteException {
        waitUntilIsEnabled();
        click();
    }

    public void setFocus() throws RemoteException {
        swtBotButton.setFocus();
    }

    /**********************************************
     * 
     * states
     * 
     **********************************************/
    public boolean isEnabled() throws RemoteException {
        return swtBotButton.isEnabled();
    }

    public boolean isVisible() throws RemoteException {
        return swtBotButton.isVisible();
    }

    public boolean isActive() throws RemoteException {
        return swtBotButton.isActive();
    }

    public String getText() throws RemoteException {
        return swtBotButton.getText();
    }

    public String getToolTipText() throws RemoteException {
        return swtBotButton.getText();
    }

    /**********************************************
     * 
     * waits until
     * 
     **********************************************/
    public void waitUntilIsEnabled() throws RemoteException {
        bot.waitUntil1(Conditions.widgetIsEnabled(swtBotButton));
    }

}
