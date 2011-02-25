package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets;

import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotRadio;

public class STFBotRadioImp extends AbstractRmoteWidget implements STFBotRadio {
    private static transient STFBotRadioImp self;

    private SWTBotRadio swtBotRadio;

    /**
     * {@link STFBotButtonImp} is a singleton, but inheritance is possible.
     */
    public static STFBotRadioImp getInstance() {
        if (self != null)
            return self;
        self = new STFBotRadioImp();
        return self;
    }

    public STFBotRadio setWidget(SWTBotRadio radio) {
        this.swtBotRadio = radio;
        return this;

    }

    /**************************************************************
     * 
     * exported functions
     * 
     **************************************************************/

    public STFBotMenu contextMenu(String text) throws RemoteException {
        return stfBotMenu.setWidget(swtBotRadio.contextMenu(text));

    }

    /**********************************************
     * 
     * actions
     * 
     **********************************************/
    public void click() throws RemoteException {
        swtBotRadio.click();
    }

    public void clickAndWait() throws RemoteException {
        waitUntilIsEnabled();
        click();
    }

    public void setFocus() throws RemoteException {
        swtBotRadio.setFocus();
    }

    /**********************************************
     * 
     * states
     * 
     **********************************************/
    public boolean isEnabled() throws RemoteException {
        return swtBotRadio.isEnabled();
    }

    public boolean isVisible() throws RemoteException {
        return swtBotRadio.isVisible();
    }

    public boolean isActive() throws RemoteException {
        return swtBotRadio.isActive();
    }

    public String getText() throws RemoteException {
        return swtBotRadio.getText();
    }

    public String getToolTipText() throws RemoteException {
        return swtBotRadio.getText();
    }

    public boolean isSelected() throws RemoteException {
        return swtBotRadio.isSelected();
    }

    /**********************************************
     * 
     * waits until
     * 
     **********************************************/
    public void waitUntilIsEnabled() throws RemoteException {
        stfBot.waitUntil(Conditions.widgetIsEnabled(swtBotRadio));
    }
}
