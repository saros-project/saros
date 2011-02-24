package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarDropDownButton;
import org.hamcrest.Matcher;

public class STFBotToolbarDropDownButtonImp extends AbstractRmoteWidget
    implements STFBotToolbarDropDownButton {
    private static transient STFBotToolbarDropDownButtonImp self;

    private SWTBotToolbarDropDownButton toolbarDropDownButton;
    private static STFBotMenuImp menu;

    /**
     * {@link STFBotButtonImp} is a singleton, but inheritance is possible.
     */
    public static STFBotToolbarDropDownButtonImp getInstance() {
        if (self != null)
            return self;
        self = new STFBotToolbarDropDownButtonImp();
        menu = STFBotMenuImp.getInstance();
        return self;
    }

    public void setSwtBotToolbarDropDownButton(
        SWTBotToolbarDropDownButton toolbarDropDownButton) {
        this.toolbarDropDownButton = toolbarDropDownButton;
    }

    /**************************************************************
     * 
     * exported functions
     * 
     **************************************************************/

    public List<STFBotMenu> menuItems(Matcher<MenuItem> matcher)
        throws RemoteException {
        List<STFBotMenu> menus = new ArrayList<STFBotMenu>();
        for (SWTBotMenu m : toolbarDropDownButton.menuItems(matcher)) {
            menu.setWidget(m);
            menus.add(menu);
        }
        return menus;

    }

    public STFBotMenuImp menuItem(String menuItem) throws RemoteException {
        menu.setWidget(toolbarDropDownButton.menuItem(menuItem));
        return menu;
    }

    public STFBotMenuImp menuItem(Matcher<MenuItem> matcher)
        throws RemoteException {

        menu.setWidget(toolbarDropDownButton.menuItem(matcher));
        return menu;
    }

    public void pressShortcut(KeyStroke... keys) throws RemoteException {
        toolbarDropDownButton.pressShortcut(keys);
    }
    /**********************************************
     * 
     * actions
     * 
     **********************************************/
}
