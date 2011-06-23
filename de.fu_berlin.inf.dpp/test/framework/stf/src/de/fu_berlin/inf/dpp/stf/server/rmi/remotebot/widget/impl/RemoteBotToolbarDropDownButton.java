package de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.impl;

import java.rmi.RemoteException;
import java.util.List;

import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarDropDownButton;
import org.hamcrest.Matcher;

import de.fu_berlin.inf.dpp.stf.server.StfRemoteObject;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.impl.RemoteWorkbenchBot;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotMenu;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotToolbarDropDownButton;

public final class RemoteBotToolbarDropDownButton extends StfRemoteObject
    implements IRemoteBotToolbarDropDownButton {

    private static final RemoteBotToolbarDropDownButton INSTANCE = new RemoteBotToolbarDropDownButton();

    private SWTBotToolbarDropDownButton widget;

    public static RemoteBotToolbarDropDownButton getInstance() {
        return INSTANCE;
    }

    public IRemoteBotToolbarDropDownButton setWidget(
        SWTBotToolbarDropDownButton toolbarDropDownButton) {
        this.widget = toolbarDropDownButton;
        return this;
    }

    public List<? extends SWTBotMenu> menuItems(Matcher<MenuItem> matcher) {
        return widget.menuItems(matcher);
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

    public IRemoteBotMenu menuItem(String menuItem) throws RemoteException {
        return RemoteBotMenu.getInstance().setWidget(widget.menuItem(menuItem));
    }

    public IRemoteBotMenu menuItem(Matcher<MenuItem> matcher)
        throws RemoteException {

        return RemoteBotMenu.getInstance().setWidget(widget.menuItem(matcher));
    }

    public IRemoteBotMenu contextMenu(String text) throws RemoteException {
        return RemoteBotMenu.getInstance().setWidget(widget.contextMenu(text));
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

    public void pressShortcut(KeyStroke... keys) throws RemoteException {
        widget.pressShortcut(keys);
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
        RemoteWorkbenchBot.getInstance().waitUntil(
            Conditions.widgetIsEnabled(widget));
    }

}
