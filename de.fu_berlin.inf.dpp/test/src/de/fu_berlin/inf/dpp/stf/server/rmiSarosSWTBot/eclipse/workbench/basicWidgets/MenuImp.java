package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.basicWidgets;

import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.EclipseComponentImp;

public class MenuImp extends EclipseComponentImp implements Menu {

    private static transient MenuImp menuImp;

    private SWTBotMenu widget;

    /**
     * {@link TableImp} is a singleton, but inheritance is possible.
     */
    public static MenuImp getInstance() {
        if (menuImp != null)
            return menuImp;
        menuImp = new MenuImp();
        return menuImp;
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
    public void clickMenuWithTexts(String... texts) throws RemoteException {
        workbench.activateWorkbench();
        SWTBotMenu selectedmenu = null;
        for (String text : texts) {
            try {
                if (selectedmenu == null) {
                    selectedmenu = bot.menu(text);
                } else {
                    selectedmenu = selectedmenu.menu(text);
                }
            } catch (WidgetNotFoundException e) {
                log.error("menu \"" + text + "\" not found!");
                throw e;
            }
        }
        if (selectedmenu != null)
            selectedmenu.click();
    }

    public void setWidget(SWTBotMenu widget) throws RemoteException {
        this.widget = widget;
    }

    public MenuImp contextMenu(String text) throws RemoteException {
        widget = widget.contextMenu(text);
        return this;

    }

    public void click() throws RemoteException {
        // ContextMenuHelper.clickContextMenu(widget, text);
        // widget.contextMenu(text).click();
        // ContextMenuHelper.
        //
        // bot.viewById("").bot().tree().expandNode("").contextMenu("").
        widget.click();

    }
}
