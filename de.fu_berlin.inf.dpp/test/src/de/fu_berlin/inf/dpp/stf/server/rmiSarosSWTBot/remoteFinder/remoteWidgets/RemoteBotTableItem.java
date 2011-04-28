package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.remoteFinder.remoteWidgets;

import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTableItem;
import org.eclipse.swtbot.swt.finder.widgets.TimeoutException;

public class RemoteBotTableItem extends AbstractRmoteWidget implements
    IRemoteBotTableItem {

    private static transient RemoteBotTableItem self;

    private SWTBotTableItem widget;

    /**
     * {@link RemoteBotTableItem} is a singleton, but inheritance is possible.
     */
    public static RemoteBotTableItem getInstance() {
        if (self != null)
            return self;
        self = new RemoteBotTableItem();
        return self;
    }

    public IRemoteBotTableItem setWidget(SWTBotTableItem tableItem) {
        this.widget = tableItem;
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
        stfBotMenu.setWidget(widget.contextMenu(text));
        return stfBotMenu;
    }

    /**********************************************
     * 
     * actions
     * 
     **********************************************/

    public void select() throws RemoteException {
        widget.select();
    }

    public void check() throws RemoteException {
        widget.check();
    }

    public void uncheck() throws RemoteException {
        widget.uncheck();
    }

    public void toggleCheck() throws RemoteException {
        widget.toggleCheck();
    }

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
    public boolean existsContextMenu(String contextName) throws RemoteException {
        long oldTimeout = SWTBotPreferences.TIMEOUT;
        // increase the timeout
        SWTBotPreferences.TIMEOUT = 1000;

        try {
            widget.contextMenu(contextName);
            SWTBotPreferences.TIMEOUT = oldTimeout;
            return true;

        } catch (TimeoutException e) {
            SWTBotPreferences.TIMEOUT = oldTimeout;
            return false;
        }
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

    public boolean isChecked() throws RemoteException {
        return widget.isChecked();
    }

    public boolean isGrayed() throws RemoteException {
        return widget.isGrayed();
    }

    public String getText(int index) throws RemoteException {
        return widget.getText(index);
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
