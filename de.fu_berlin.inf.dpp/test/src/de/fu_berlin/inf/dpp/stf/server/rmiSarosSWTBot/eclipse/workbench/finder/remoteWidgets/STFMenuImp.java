package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.finder.remoteWidgets;

import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.sarosFinder.remoteComponents.EclipseComponentImp;

public class STFMenuImp extends EclipseComponentImp implements STFMenu {

    private static transient STFMenuImp menuImp;

    private SWTBotMenu widget;

    /**
     * {@link STFTableImp} is a singleton, but inheritance is possible.
     */
    public static STFMenuImp getInstance() {
        if (menuImp != null)
            return menuImp;
        menuImp = new STFMenuImp();
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

    public STFMenuImp contextMenu(String text) throws RemoteException {
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
