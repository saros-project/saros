package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets;

import static org.eclipse.swtbot.swt.finder.waits.Conditions.tableHasRows;

import java.rmi.RemoteException;
import java.util.List;

import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.conditions.SarosConditions;

public class STFBotTableImp extends AbstractRmoteWidget implements STFBotTable {

    private static transient STFBotTableImp tableImp;
    private static STFBotMenuImp stfBotMenu;
    private static STFBotTableItemImp stfBotTableItem;

    private SWTBotTable table;

    /**
     * {@link STFBotTableImp} is a singleton, but inheritance is possible.
     */
    public static STFBotTableImp getInstance() {
        if (tableImp != null)
            return tableImp;
        tableImp = new STFBotTableImp();
        stfBotMenu = STFBotMenuImp.getInstance();
        stfBotTableItem = STFBotTableItemImp.getInstance();
        return tableImp;
    }

    public void setSwtBotTable(SWTBotTable table) {
        this.table = table;
    }

    /**************************************************************
     * 
     * exported functions
     * 
     **************************************************************/

    /**********************************************
     * 
     * states
     * 
     **********************************************/
    public boolean existsTableItem(String itemText) throws RemoteException {
        return table.containsItem(itemText);
    }

    public List<String> getTableColumns() throws RemoteException {
        return table.columns();
    }

    public boolean containsItem(String item) throws RemoteException {
        return table.containsItem(item);
    }

    /**********************************************
     * 
     * finder
     * 
     **********************************************/

    public STFBotMenu contextMenu(String text) throws RemoteException {
        stfBotMenu.setWidget(table.contextMenu(text));
        return stfBotMenu;
    }

    public STFBotTableItem getTableItem(String itemText) throws RemoteException {
        stfBotTableItem.setSwtBotTable(table.getTableItem(itemText));
        return stfBotTableItem;
    }

    public STFBotTableItem getTableItem(int row) throws RemoteException {
        stfBotTableItem.setSwtBotTable(table.getTableItem(row));
        return stfBotTableItem;
    }

    /**********************************************
     * 
     * actions
     * 
     **********************************************/

    public void select(String... items) throws RemoteException {
        table.select(items);
    }

    public void click(int row, int column) throws RemoteException {
        table.click(row, column);
    }

    public void unselect() throws RemoteException {
        table.unselect();
    }

    public void selectionCount() throws RemoteException {
        table.selectionCount();
    }

    public void check(int row, int column) throws RemoteException {
        table.cell(row, column);
    }

    public void check(int row, String columnName) throws RemoteException {
        table.cell(row, columnName);
    }

    public void setFocus() throws RemoteException {
        table.setFocus();
    }

    /**********************************************
     * 
     * states
     * 
     **********************************************/
    public int indexOfColumn(String column) throws RemoteException {
        return table.indexOfColumn(column);
    }

    public int indexOf(String item) throws RemoteException {
        return table.indexOf(item);
    }

    public int indexOf(String item, int column) throws RemoteException {
        return table.indexOf(item, column);
    }

    public int indexOf(String item, String column) throws RemoteException {
        return table.indexOf(item, column);
    }

    public int rowCount() throws RemoteException {
        return table.rowCount();
    }

    public int columnCount() throws RemoteException {
        return table.columnCount();
    }

    public List<String> columns() throws RemoteException {
        return table.columns();
    }

    // FIXME this method doesn't work.
    public boolean existsContextMenu(String contextName) throws RemoteException {
        try {
            table.contextMenu(contextName);
            return true;
        } catch (WidgetNotFoundException e) {
            return false;
        }
    }

    public boolean isEnabled() throws RemoteException {
        return table.isEnabled();
    }

    public boolean isVisible() throws RemoteException {
        return table.isVisible();
    }

    public boolean isActive() throws RemoteException {
        return table.isActive();
    }

    public String getText() throws RemoteException {
        return table.getText();
    }

    public String getToolTipText() throws RemoteException {
        return table.getText();
    }

    /**********************************************
     * 
     * waits until
     * 
     **********************************************/
    public void waitUntilIsEnabled() throws RemoteException {
        stfBot.waitUntil(Conditions.widgetIsEnabled(table));
    }

    public void waitUntilTableHasRows(int row) throws RemoteException {
        stfBot.waitUntil(tableHasRows(table, row));
    }

    public void waitUntilTableItemExists(String itemText)
        throws RemoteException {
        stfBot.waitUntil(SarosConditions.existTableItem(this, itemText));
    }

}
