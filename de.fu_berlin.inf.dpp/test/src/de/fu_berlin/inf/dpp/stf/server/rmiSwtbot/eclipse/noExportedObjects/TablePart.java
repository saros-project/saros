package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noExportedObjects;

import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.withMnemonic;
import static org.eclipse.swtbot.swt.finder.waits.Conditions.tableHasRows;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;

import java.util.List;

import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.results.BoolResult;
import org.eclipse.swtbot.swt.finder.widgets.AbstractSWTBot;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTableItem;
import org.eclipse.swtbot.swt.finder.widgets.TimeoutException;
import org.hamcrest.Matcher;

import de.fu_berlin.inf.dpp.stf.sarosSWTBot.SarosSWTBot;
import de.fu_berlin.inf.dpp.stf.sarosSWTBot.widgets.ContextMenuHelper;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.conditions.SarosConditions;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.EclipseComponent;

public class TablePart extends EclipseComponent {

    public SWTBotTable getTable() {
        return bot.table();
    }

    /**
     * Click a context menu of the selected table item. The method is defined as
     * helper method for other clickTB*In*View methods in {@link SarosSWTBot}
     * and should not be exported by rmi. <br/>
     * Operational steps:
     * <ol>
     * <li>select a table item</li>
     * 
     * <li>then click the specified context menu on it</li>
     * </ol>
     * 
     * @param viewName
     * 
     * @param itemName
     *            the table item' name, whose context menu you want to click.
     * @see #selectTableItemWithLabelInView(String, String)
     */
    public void clickContextMenuOfTable(String itemName, String contextName) {
        getTable().getTableItem(itemName).contextMenu(contextName).click();
    }

    public boolean isContextMenuOfTableVisible(String itemName,
        String contextName) {
        return bot.table().getTableItem(itemName).contextMenu(contextName)
            .isVisible();
    }

    public boolean isContextMenuOfTableEnabled(String itemName,
        String contextName) {
        return bot.table().getTableItem(itemName).contextMenu(contextName)
            .isEnabled();
    }

    public SWTBotTableItem selectTableItemWithLabel(String label) {
        try {
            SWTBotTable table = bot.table();
            waitUntilTableItemExisted(table, label);
            return table.getTableItem(label);
        } catch (WidgetNotFoundException e) {
            log.warn("table item " + label + " not found.", e);
        }
        return null;
    }

    public SWTBotTableItem selectTableItemWithLabel(SWTBotTable table,
        String label) {
        try {
            waitUntilTableItemExisted(table, label);
            return table.getTableItem(label);

        } catch (WidgetNotFoundException e) {
            log.warn("table item " + label + " not found.", e);
        }
        return null;
    }

    public void selectCheckBoxInTable(String text) {
        for (int i = 0; i < bot.table().rowCount(); i++) {
            if (bot.table().getTableItem(i).getText(0).equals(text)) {
                bot.table().getTableItem(i).check();
                log.debug("Found checkbox item \"" + text + "\".");
                return;
            }
        }
        throw new WidgetNotFoundException(
            "No checkbox item found with label \"" + text + "\".");
    }

    public void selectCheckBoxsInTable(List<String> invitees) {
        for (int i = 0; i < bot.table().rowCount(); i++) {
            String next = bot.table().getTableItem(i).getText(0);
            if (invitees.contains(next)) {
                bot.table().getTableItem(i).check();
            }
        }
    }

    public List<String> getAllTableItem() {
        return bot.table().columns();
    }

    public boolean existTableItem(String itemName) {
        try {
            // waitUntilTableItemExsited(table, itemName);
            bot.table().getTableItem(itemName);
            return true;
        } catch (WidgetNotFoundException e) {
            log.warn("table item " + itemName + " not found.", e);
        }
        return false;
    }

    /**
     * 
     * TODO optimize this function, it takes too long to identify whether a
     * context exist because of waiting until timeout.
     * 
     * @param itemName
     *            name of the table item, whose context menu you want to
     *            check,if it exists.
     * @param contextName
     *            name of the context menu you want to check,if it exists.
     * @return <tt>true</tt>, if the context menu of the specified table item
     *         exists.
     */
    public boolean existsContextOfTableItem(String itemName, String contextName) {
        try {
            bot.table().getTableItem(itemName).contextMenu(contextName);
            return true;
        } catch (TimeoutException e) {
            log.warn("context menu " + contextName + " not found.", e);
        }
        return false;
    }

    /**
     * 
     * TODO it don't work yet, need to be fixed.
     * 
     * @param itemName
     *            name of the table item, whose context menu you want to
     *            check,if it exists.
     * @param contextName
     *            name of the context menu you want to check,if it exists.
     * @return <tt>true</tt>, if the context menu of the specified table item
     *         exists.
     */
    public boolean existContextOfTableItem(
        final AbstractSWTBot<? extends Control> bot, final String itemName,
        final String contextName) {
        final boolean existContext = UIThreadRunnable
            .syncExec(new BoolResult() {
                MenuItem menuItem = null;

                public Boolean run() {
                    Control control = bot.widget;
                    Menu menu = control.getMenu();

                    @SuppressWarnings("unchecked")
                    Matcher<?> matcher = allOf(instanceOf(MenuItem.class),
                        withMnemonic(itemName));
                    menuItem = ContextMenuHelper.show(menu, matcher);
                    if (menuItem != null) {
                        menu = menuItem.getMenu();
                    } else {
                        ContextMenuHelper.hide(menu);
                    }
                    for (MenuItem item : menu.getItems()) {
                        if (item.getText().equals(contextName))
                            return true;
                    }
                    return false;
                }
            });
        return existContext;
    }

    public void waitUntilTableItemExisted(SWTBotTable table,
        String tableItemName) {
        waitUntil(SarosConditions.existTableItem(table, tableItemName));
    }

    public void waitUntilTableHasRows(int row) {
        waitUntil(tableHasRows(bot.table(), row));
    }

    public void waitUntilContextMenuOfTableItemEnabled(
        SWTBotTableItem tableItem, String context) {
        waitUntil(SarosConditions.ExistContextMenuOfTableItem(tableItem,
            context));
    }

    // /**
    // *
    // * @param itemName
    // * the table item name.
    // * @param contextName
    // * @return <tt>true</tt> if the specified context menu of the select table
    // * item exists.
    // */
    // public boolean isContextMenuOfTableItemExist(String itemName,
    // String contextName) {
    // SWTBotTableItem item = selectTableItemWithLabel(itemName);
    // try {
    // item.contextMenu(contextName);
    // return true;
    // } catch (TimeoutException e) {
    // return false;
    // }
    // }

}
