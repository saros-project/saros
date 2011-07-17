package de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.impl;

import static org.eclipse.swtbot.swt.finder.waits.Conditions.tableHasRows;

import java.rmi.RemoteException;
import java.util.List;

import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTableItem;

import de.fu_berlin.inf.dpp.stf.server.StfRemoteObject;
import de.fu_berlin.inf.dpp.stf.server.bot.condition.SarosConditions;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.impl.RemoteWorkbenchBot;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotMenu;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotTable;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotTableItem;

public final class RemoteBotTable extends StfRemoteObject implements
    IRemoteBotTable {

    private static final RemoteBotTable INSTANCE = new RemoteBotTable();

    private SWTBotTable widget;

    public static RemoteBotTable getInstance() {
        return INSTANCE;
    }

    public IRemoteBotTable setWidget(SWTBotTable table) {
        this.widget = table;
        return this;
    }

    public IRemoteBotMenu contextMenu(String text) throws RemoteException {
        return RemoteBotMenu.getInstance().setWidget(widget.contextMenu(text));
    }

    public IRemoteBotTableItem getTableItem(String itemText)
        throws RemoteException {
        return RemoteBotTableItem.getInstance().setWidget(
            widget.getTableItem(itemText));
    }

    public IRemoteBotTableItem getTableItemWithRegex(String regex)
        throws RemoteException {

        for (int i = 0; i < widget.rowCount(); i++) {
            SWTBotTableItem item = widget.getTableItem(i);
            if (item.getText().matches(regex)) {
                return RemoteBotTableItem.getInstance().setWidget(item);
            }
        }
        throw new WidgetNotFoundException(
            "unable to find table item with regex: " + regex + " on table "
                + widget.getText());
    }

    public IRemoteBotTableItem getTableItem(int row) throws RemoteException {
        return RemoteBotTableItem.getInstance().setWidget(
            widget.getTableItem(row));
    }

    public List<String> getTableColumns() throws RemoteException {
        return widget.columns();
    }

    public boolean containsItem(String item) throws RemoteException {
        return widget.containsItem(item);
    }

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

    public void waitUntilIsEnabled() throws RemoteException {
        RemoteWorkbenchBot.getInstance().waitUntil(
            Conditions.widgetIsEnabled(widget));
    }

    public void waitUntilTableHasRows(int row) throws RemoteException {
        RemoteWorkbenchBot.getInstance().waitUntil(tableHasRows(widget, row));
    }

    public void waitUntilTableItemExists(String itemText)
        throws RemoteException {
        RemoteWorkbenchBot.getInstance().waitUntil(
            SarosConditions.existTableItem(this, itemText));
    }

}
