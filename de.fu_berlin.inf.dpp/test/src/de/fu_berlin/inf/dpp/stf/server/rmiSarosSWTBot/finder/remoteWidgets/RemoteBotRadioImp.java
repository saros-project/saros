package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets;

import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotRadio;

public class RemoteBotRadioImp extends AbstractRmoteWidget implements RemoteBotRadio {
    private static transient RemoteBotRadioImp self;

    private SWTBotRadio swtBotRadio;

    /**
     * {@link RemoteBotButtonImp} is a singleton, but inheritance is possible.
     */
    public static RemoteBotRadioImp getInstance() {
        if (self != null)
            return self;
        self = new RemoteBotRadioImp();
        return self;
    }

    public RemoteBotRadio setWidget(SWTBotRadio radio) {
        this.swtBotRadio = radio;
        return this;

    }

    /**************************************************************
     * 
     * exported functions
     * 
     **************************************************************/

    public RemoteBotMenu contextMenu(String text) throws RemoteException {
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
