package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.remoteFinder.remoteWidgets;

import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotRadio;

public class RemoteBotRadio extends AbstractRmoteWidget implements IRemoteBotRadio {
    private static transient RemoteBotRadio self;

    private SWTBotRadio swtBotRadio;

    /**
     * {@link RemoteBotButton} is a singleton, but inheritance is possible.
     */
    public static RemoteBotRadio getInstance() {
        if (self != null)
            return self;
        self = new RemoteBotRadio();
        return self;
    }

    public IRemoteBotRadio setWidget(SWTBotRadio radio) {
        this.swtBotRadio = radio;
        return this;

    }

    /**************************************************************
     * 
     * exported functions
     * 
     **************************************************************/

    public IRemoteBotMenu contextMenu(String text) throws RemoteException {
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
