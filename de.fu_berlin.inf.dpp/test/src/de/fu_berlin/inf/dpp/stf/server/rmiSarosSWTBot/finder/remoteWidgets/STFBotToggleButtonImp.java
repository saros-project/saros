package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets;

import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToggleButton;

public class STFBotToggleButtonImp extends AbstractRmoteWidget implements
    STFBotToggleButton {
    private static transient STFBotToggleButtonImp self;

    private SWTBotToggleButton widget;

    /**
     * {@link STFBotToggleButtonImp} is a singleton, but inheritance is
     * possible.
     */
    public static STFBotToggleButtonImp getInstance() {
        if (self != null)
            return self;
        self = new STFBotToggleButtonImp();
        return self;
    }

    public STFBotToggleButton setWidget(SWTBotToggleButton toggleButton) {
        this.widget = toggleButton;
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

    public void press() throws RemoteException {
        widget.press();
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

    public boolean isPressed() throws RemoteException {
        return widget.isPressed();
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
