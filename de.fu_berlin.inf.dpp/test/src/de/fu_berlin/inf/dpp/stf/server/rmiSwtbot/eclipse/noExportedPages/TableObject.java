package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noExportedPages;

import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.withMnemonic;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;

import java.util.List;

import org.apache.log4j.Logger;
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
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.RmiSWTWorkbenchBot;

public class TableObject {
    private static final transient Logger log = Logger
        .getLogger(TableObject.class);
    private RmiSWTWorkbenchBot rmiBot;
    private WaitUntilObject wUntil;
    private SarosSWTBot bot;

    public TableObject(RmiSWTWorkbenchBot rmiBot) {
        this.rmiBot = rmiBot;
        this.bot = RmiSWTWorkbenchBot.delegate;
        this.wUntil = rmiBot.wUntilObject;

    }

    public SWTBotTable getTable() {
        return bot.table();
    }

    public void clickContextMenuOfTable(String itemName, String contextName) {
        bot.table().getTableItem(itemName).contextMenu(contextName).click();
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
            wUntil.waitUntilTableItemExisted(table, label);
            return table.getTableItem(label);
        } catch (WidgetNotFoundException e) {
            log.warn("table item " + label + " not found.", e);
        }
        return null;
    }

    public SWTBotTableItem selectTableItemWithLabel(SWTBotTable table,
        String label) {
        try {
            wUntil.waitUntilTableItemExisted(table, label);
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
                log.debug("found invitee: " + text);
                return;
            }
        }
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
    public boolean existContextOfTableItem(String itemName, String contextName) {
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
}
