package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets;

import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.EclipseComponentImp;

public class STFBotMenuImp extends EclipseComponentImp implements STFBotMenu {

    private static transient STFBotMenuImp menuImp;

    private SWTBotMenu widget;

    /**
     * {@link STFBotTableImp} is a singleton, but inheritance is possible.
     */
    public static STFBotMenuImp getInstance() {
        if (menuImp != null)
            return menuImp;
        menuImp = new STFBotMenuImp();
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

    public STFBotMenuImp contextMenu(String text) throws RemoteException {
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
