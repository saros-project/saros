package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.remoteFinder.remoteWidgets;

import static org.eclipse.swtbot.swt.finder.waits.Conditions.tableHasRows;

import java.rmi.RemoteException;
import java.util.List;

import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTableItem;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.conditions.SarosConditions;

public class RemoteBotTable extends AbstractRmoteWidget implements
    IRemoteBotTable {

    private static transient RemoteBotTable self;

    private SWTBotTable widget;

    /**
     * {@link RemoteBotTable} is a singleton, but inheritance is possible.
     */
    public static RemoteBotTable getInstance() {
        if (self != null)
            return self;
        self = new RemoteBotTable();
        return self;
    }

    public IRemoteBotTable setWidget(SWTBotTable table) {
        this.widget = table;
        return this;
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

    public IRemoteBotMenu contextMenu(String text) throws RemoteException {
        stfBotMenu.setWidget(widget.contextMenu(text));
        return stfBotMenu;
    }

    public IRemoteBotTableItem getTableItem(String itemText)
        throws RemoteException {
        stfBotTableItem.setWidget(widget.getTableItem(itemText));
        return stfBotTableItem;
    }

    public IRemoteBotTableItem getTableItemWithRegex(String regex)
        throws RemoteException {
        boolean hasItem = false;

        for (int i = 0; i < widget.rowCount(); i++) {
            SWTBotTableItem item = widget.getTableItem(i);
            if (item.getText().matches(regex)) {
                hasItem = true;
                stfBotTableItem.setWidget(item);
                break;
            }
        }
        if (!hasItem)
            throw new RuntimeException("Not found the tableItem!");
        return stfBotTableItem;
    }

    public IRemoteBotTableItem getTableItem(int row) throws RemoteException {
        stfBotTableItem.setWidget(widget.getTableItem(row));
        return stfBotTableItem;
    }

    /**********************************************
     * 
     * states
     * 
     **********************************************/

    public List<String> getTableColumns() throws RemoteException {
        return widget.columns();
    }

    public boolean containsItem(String item) throws RemoteException {
        return widget.containsItem(item);
    }

    /**********************************************
     * 
     * actions
     * 
     **********************************************/

    public void select(String... items) throws RemoteException {
        widget.select(items);
    }

    public void click(int row, int column) throws RemoteException {
        widget.click(row, column);
    }

    public void unselect() throws RemoteException {
        widget.unselect();
    }

    public void selectionCount() throws RemoteException {
        widget.selectionCount();
    }

    public void check(int row, int column) throws RemoteException {
        widget.cell(row, column);
    }

    public void check(int row, String columnName) throws RemoteException {
        widget.cell(row, columnName);
    }

    public void setFocus() throws RemoteException {
        widget.setFocus();
    }

    /**********************************************
     * 
     * states
     * 
     **********************************************/
    public int indexOfColumn(String column) throws RemoteException {
        return widget.indexOfColumn(column);
    }

    public int indexOf(String item) throws RemoteException {
        return widget.indexOf(item);
    }

    public int indexOf(String item, int column) throws RemoteException {
        return widget.indexOf(item, column);
    }

    public int indexOf(String item, String column) throws RemoteException {
        return widget.indexOf(item, column);
    }

    public int rowCount() throws RemoteException {
        return widget.rowCount();
    }

    public int columnCount() throws RemoteException {
        return widget.columnCount();
    }

    public List<String> columns() throws RemoteException {
        return widget.columns();
    }

    // FIXME this method doesn't work.
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

    public void waitUntilTableHasRows(int row) throws RemoteException {
        stfBot.waitUntil(tableHasRows(widget, row));
    }

    public void waitUntilTableItemExists(String itemText)
        throws RemoteException {
        stfBot.waitUntil(SarosConditions.existTableItem(this, itemText));
    }

}
