package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets;

import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToggleButton;

public class STFBotToggleButtonImp extends AbstractRmoteWidget implements STFBotToggleButton {
    private static transient STFBotToggleButtonImp self;

    private SWTBotToggleButton swtBotToggleButton;

    /**
     * {@link STFBotButtonImp} is a singleton, but inheritance is possible.
     */
    public static STFBotToggleButtonImp getInstance() {
        if (self != null)
            return self;
        self = new STFBotToggleButtonImp();
        return self;
    }

    public void setWidget(SWTBotToggleButton toggleButton) {
        this.swtBotToggleButton = toggleButton;
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
        swtBotToggleButton.click();
    }

    public void press() throws RemoteException {
        swtBotToggleButton.press();
    }

    public void clickAndWait() throws RemoteException {
        waitUntilIsEnabled();
        click();
    }

    public void setFocus() throws RemoteException {
        swtBotToggleButton.setFocus();
    }

    /**********************************************
     * 
     * states
     * 
     **********************************************/

    public boolean isPressed() throws RemoteException {
        return swtBotToggleButton.isPressed();
    }

    public boolean isEnabled() throws RemoteException {
        return swtBotToggleButton.isEnabled();
    }

    public boolean isVisible() throws RemoteException {
        return swtBotToggleButton.isVisible();
    }

    public boolean isActive() throws RemoteException {
        return swtBotToggleButton.isActive();
    }

    public String getText() throws RemoteException {
        return swtBotToggleButton.getText();
    }

    public String getToolTipText() throws RemoteException {
        return swtBotToggleButton.getText();
    }

    /**********************************************
     * 
     * waits until
     * 
     **********************************************/
    public void waitUntilIsEnabled() throws RemoteException {
        stfBot.waitUntil(Conditions.widgetIsEnabled(swtBotToggleButton));
    }

}
