package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets;

import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTableItem;

public class STFBotTableItemImp extends AbstractRmoteWidget implements
    STFBotTableItem {

    private static transient STFBotTableItemImp tableItemImp;
    private static STFBotMenuImp stfBotMenu;

    private SWTBotTableItem tableItem;

    /**
     * {@link STFBotTableImp} is a singleton, but inheritance is possible.
     */
    public static STFBotTableItemImp getInstance() {
        if (tableItemImp != null)
            return tableItemImp;
        tableItemImp = new STFBotTableItemImp();
        stfBotMenu = STFBotMenuImp.getInstance();
        return tableItemImp;
    }

    public void setSwtBotTable(SWTBotTableItem tableItem) {
        this.tableItem = tableItem;
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

    public STFBotMenu contextMenu(String text) throws RemoteException {
        stfBotMenu.setWidget(tableItem.contextMenu(text));
        return stfBotMenu;
    }

    public void select() throws RemoteException {
        tableItem.select();
    }

    public void check() throws RemoteException {
        tableItem.check();
    }

    public void uncheck() throws RemoteException {
        tableItem.uncheck();
    }

    public void toggleCheck() throws RemoteException {
        tableItem.toggleCheck();
    }

    public void click() throws RemoteException {
        tableItem.click();
    }

    public void clickAndWait() throws RemoteException {
        waitUntilIsEnabled();
        click();
    }

    public void setFocus() throws RemoteException {
        tableItem.setFocus();
    }

    /**********************************************
     * 
     * states
     * 
     **********************************************/
    public boolean existsContextMenu(String contextName) throws RemoteException {
        try {
            tableItem.contextMenu(contextName);

            return true;
        } catch (WidgetNotFoundException e) {
            return false;
        }

    }

    public boolean isEnabled() throws RemoteException {
        return tableItem.isEnabled();
    }

    public boolean isVisible() throws RemoteException {
        return tableItem.isVisible();
    }

    public boolean isActive() throws RemoteException {
        return tableItem.isActive();
    }

    public boolean isChecked() throws RemoteException {
        return tableItem.isChecked();
    }

    public boolean isGrayed() throws RemoteException {
        return tableItem.isGrayed();
    }

    public String getText(int index) throws RemoteException {
        return tableItem.getText(index);
    }

    public String getText() throws RemoteException {
        return tableItem.getText();
    }

    public String getToolTipText() throws RemoteException {
        return tableItem.getText();
    }

    /**********************************************
     * 
     * waits until
     * 
     **********************************************/
    public void waitUntilIsEnabled() throws RemoteException {
        bot.waitUntil(Conditions.widgetIsEnabled(tableItem));
    }
}
