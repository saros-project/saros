package de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.impl;

import static org.eclipse.swtbot.swt.finder.waits.Conditions.tableHasRows;

import de.fu_berlin.inf.dpp.stf.server.StfRemoteObject;
import de.fu_berlin.inf.dpp.stf.server.bot.condition.SarosConditions;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.impl.RemoteWorkbenchBot;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotMenu;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotTable;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotTableItem;
import java.rmi.RemoteException;
import java.util.List;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTableItem;

public final class RemoteBotTable extends StfRemoteObject implements IRemoteBotTable {

  private static final RemoteBotTable INSTANCE = new RemoteBotTable();

  private SWTBotTable widget;

  public static RemoteBotTable getInstance() {
    return INSTANCE;
  }

  public IRemoteBotTable setWidget(SWTBotTable table) {
    this.widget = table;
    return this;
  }

  @Override
  public IRemoteBotMenu contextMenu(String text) throws RemoteException {
    return RemoteBotMenu.getInstance().setWidget(widget.contextMenu(text));
  }

  @Override
  public IRemoteBotTableItem getTableItem(String itemText) throws RemoteException {
    return RemoteBotTableItem.getInstance().setWidget(widget.getTableItem(itemText));
  }

  @Override
  public IRemoteBotTableItem getTableItemWithRegex(String regex) throws RemoteException {

    for (int i = 0; i < widget.rowCount(); i++) {
      SWTBotTableItem item = widget.getTableItem(i);
      if (item.getText().matches(regex)) {
        return RemoteBotTableItem.getInstance().setWidget(item);
      }
    }
    throw new WidgetNotFoundException(
        "unable to find table item with regex: " + regex + " on table " + widget.getText());
  }

  @Override
  public IRemoteBotTableItem getTableItem(int row) throws RemoteException {
    return RemoteBotTableItem.getInstance().setWidget(widget.getTableItem(row));
  }

  @Override
  public List<String> getTableColumns() throws RemoteException {
    return widget.columns();
  }

  @Override
  public boolean containsItem(String item) throws RemoteException {
    return widget.containsItem(item);
  }

  @Override
  public void select(String... items) throws RemoteException {
    widget.select(items);
  }

  @Override
  public void click(int row, int column) throws RemoteException {
    widget.click(row, column);
  }

  @Override
  public void unselect() throws RemoteException {
    widget.unselect();
  }

  @Override
  public void selectionCount() throws RemoteException {
    widget.selectionCount();
  }

  @Override
  public void check(int row, int column) throws RemoteException {
    widget.cell(row, column);
  }

  @Override
  public void check(int row, String columnName) throws RemoteException {
    widget.cell(row, columnName);
  }

  @Override
  public void setFocus() throws RemoteException {
    widget.setFocus();
  }

  @Override
  public int indexOfColumn(String column) throws RemoteException {
    return widget.indexOfColumn(column);
  }

  @Override
  public int indexOf(String item) throws RemoteException {
    return widget.indexOf(item);
  }

  @Override
  public int indexOf(String item, int column) throws RemoteException {
    return widget.indexOf(item, column);
  }

  @Override
  public int indexOf(String item, String column) throws RemoteException {
    return widget.indexOf(item, column);
  }

  @Override
  public int rowCount() throws RemoteException {
    return widget.rowCount();
  }

  @Override
  public int columnCount() throws RemoteException {
    return widget.columnCount();
  }

  @Override
  public List<String> columns() throws RemoteException {
    return widget.columns();
  }

  // FIXME this method doesn't work.
  @Override
  public boolean existsContextMenu(String contextName) throws RemoteException {
    try {
      widget.contextMenu(contextName);
      return true;
    } catch (WidgetNotFoundException e) {
      return false;
    }
  }

  @Override
  public boolean isEnabled() throws RemoteException {
    return widget.isEnabled();
  }

  @Override
  public boolean isVisible() throws RemoteException {
    return widget.isVisible();
  }

  @Override
  public boolean isActive() throws RemoteException {
    return widget.isActive();
  }

  @Override
  public String getText() throws RemoteException {
    return widget.getText();
  }

  @Override
  public String getToolTipText() throws RemoteException {
    return widget.getText();
  }

  @Override
  public void waitUntilIsEnabled() throws RemoteException {
    RemoteWorkbenchBot.getInstance().waitUntil(Conditions.widgetIsEnabled(widget));
  }

  @Override
  public void waitUntilTableHasRows(int row) throws RemoteException {
    RemoteWorkbenchBot.getInstance().waitUntil(tableHasRows(widget, row));
  }

  @Override
  public void waitUntilTableItemExists(String itemText) throws RemoteException {
    RemoteWorkbenchBot.getInstance().waitUntil(SarosConditions.existTableItem(this, itemText));
  }
}
