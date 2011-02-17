package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets;

import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
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

    public void setWidget(SWTBotMenu widget) {
        this.widget = widget;
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

    public STFBotMenuImp contextMenu(String text) throws RemoteException {
        widget = widget.contextMenu(text);
        return this;

    }

    public STFBotMenu menu(String menuName) throws RemoteException {
        widget = widget.menu(menuName);
        return this;
    }

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
        waitUntil(Conditions.widgetIsEnabled(widget));
    }
}
