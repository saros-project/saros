package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.remoteFinder.remoteWidgets;

import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;

public class RemoteBotMenu extends AbstractRmoteWidget implements IRemoteBotMenu {

    private static transient RemoteBotMenu menuImp;

    private SWTBotMenu widget;

    /**
     * {@link RemoteBotMenu} is a singleton, but inheritance is possible.
     */
    public static RemoteBotMenu getInstance() {
        if (menuImp != null)
            return menuImp;
        menuImp = new RemoteBotMenu();
        return menuImp;
    }

    public IRemoteBotMenu setWidget(SWTBotMenu widget) {
        this.widget = widget;
        return this;
    }

    /**************************************************************
     * 
     * exported functions
     * 
     **************************************************************/

    /**********************************************
     * 
     * finder
     * 
     **********************************************/

    public RemoteBotMenu contextMenu(String text) throws RemoteException {
        widget = widget.contextMenu(text);
        return this;

    }

    public IRemoteBotMenu menu(String menuName) throws RemoteException {
        widget = widget.menu(menuName);
        return this;
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

    public boolean isChecked() throws RemoteException {
        return widget.isChecked();
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
