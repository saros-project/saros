package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets;

import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTableItem;

public class STFBotTableItemImp extends AbstractRmoteWidget implements
    STFBotTableItem {

    private static transient STFBotTableItemImp self;

    private SWTBotTableItem widget;

    /**
     * {@link STFBotTableItemImp} is a singleton, but inheritance is possible.
     */
    public static STFBotTableItemImp getInstance() {
        if (self != null)
            return self;
        self = new STFBotTableItemImp();
        return self;
    }

    public STFBotTableItem setWidget(SWTBotTableItem tableItem) {
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

    public STFBotMenu contextMenu(String text) throws RemoteException {
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
        try {
            widget.contextMenu(contextName);

            return true;
        } catch (WidgetNotFoundException e) {
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
