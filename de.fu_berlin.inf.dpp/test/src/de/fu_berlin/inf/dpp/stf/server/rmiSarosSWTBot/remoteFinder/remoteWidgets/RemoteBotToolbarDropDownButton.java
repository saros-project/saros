package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.remoteFinder.remoteWidgets;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarDropDownButton;
import org.hamcrest.Matcher;

public class RemoteBotToolbarDropDownButton extends AbstractRmoteWidget
    implements IRemoteBotToolbarDropDownButton {
    private static transient RemoteBotToolbarDropDownButton self;

    private SWTBotToolbarDropDownButton widget;

    /**
     * {@link RemoteBotToolbarDropDownButton} is a singleton, but inheritance is
     * possible.
     */
    public static RemoteBotToolbarDropDownButton getInstance() {
        if (self != null)
            return self;
        self = new RemoteBotToolbarDropDownButton();
        return self;
    }

    public IRemoteBotToolbarDropDownButton setWidget(
        SWTBotToolbarDropDownButton toolbarDropDownButton) {
        this.widget = toolbarDropDownButton;
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

    public List<IRemoteBotMenu> menuItems(Matcher<MenuItem> matcher)
        throws RemoteException {
        List<IRemoteBotMenu> menus = new ArrayList<IRemoteBotMenu>();
        for (SWTBotMenu m : widget.menuItems(matcher)) {
            stfBotMenu.setWidget(m);
            menus.add(stfBotMenu);
        }
        return menus;
    }

    public IRemoteBotMenu menuItem(String menuItem) throws RemoteException {
        stfBotMenu.setWidget(widget.menuItem(menuItem));
        return stfBotMenu;
    }

    public IRemoteBotMenu menuItem(Matcher<MenuItem> matcher)
        throws RemoteException {

        stfBotMenu.setWidget(widget.menuItem(matcher));
        return stfBotMenu;
    }

    public IRemoteBotMenu contextMenu(String text) throws RemoteException {
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
        stfBot.waitUntil(Conditions.widgetIsEnabled(widget));
    }

}
