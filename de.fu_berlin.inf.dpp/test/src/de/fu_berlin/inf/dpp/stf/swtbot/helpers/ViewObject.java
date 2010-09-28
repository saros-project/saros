package de.fu_berlin.inf.dpp.stf.swtbot.helpers;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTableItem;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.swtbot.swt.finder.widgets.TimeoutException;

import de.fu_berlin.inf.dpp.stf.sarosswtbot.SarosConstant;
import de.fu_berlin.inf.dpp.stf.swtbot.ContextMenuHelper;
import de.fu_berlin.inf.dpp.stf.swtbot.RmiSWTWorkbenchBot;
import de.fu_berlin.inf.dpp.stf.swtbot.SarosSWTWorkbenchBot;

public class ViewObject {
    private static final transient Logger log = Logger
        .getLogger(ViewObject.class);
    private RmiSWTWorkbenchBot rmiBot;
    private WaitUntilObject wUntil;
    private TableObject tableObject;
    private MenuObject menuObject;
    private TreeObject treeObject;
    private static SarosSWTWorkbenchBot bot = new SarosSWTWorkbenchBot();

    public ViewObject(RmiSWTWorkbenchBot rmiBot) {
        this.rmiBot = rmiBot;
        this.wUntil = rmiBot.wUntilObject;
        this.tableObject = rmiBot.tableObject;
        this.menuObject = rmiBot.menuObject;
        this.treeObject = rmiBot.treeObject;
    }

    /**
     * Should be only called if View is open
     */
    public void activateViewWithTitle(String title) {
        try {
            if (!isViewActive(title)) {
                bot.viewByTitle(title).setFocus();
                // waitUntil(SarosConditions.isViewActive(delegate, title));
            }
        } catch (WidgetNotFoundException e) {
            log.warn("Widget not found '" + title + "'", e);
        }
    }

    public boolean isViewActive(String title) {
        if (!isViewOpen(title))
            return false;
        return bot.activeView().getTitle().equals(title);
        // SWTBotView activeView;
        // try {
        // activeView = delegate.activeView();
        // } catch (WidgetNotFoundException e) {
        // // no active view
        // return false;
        // }
        // return activeView.getTitle().equals(title);
    }

    public boolean isViewOpen(String title) {
        return getViewTitles().contains(title);
        // try {
        // return delegate.viewByTitle(title) != null;
        // } catch (WidgetNotFoundException e) {
        // log.info("view " + title + "can not be fund!");
        // return false;
        // }
    }

    public List<String> getViewTitles() {
        ArrayList<String> list = new ArrayList<String>();
        for (SWTBotView view : bot.views())
            list.add(view.getTitle());
        return list;
    }

    /**
     * Open a view using Window->Show View->Other... The method is defined as
     * helper method for other showView* methods and should not be exported
     * using rmi.
     * 
     * 1. if the view already exist, return.
     * 
     * 2. activate the saros-instance-window(alice / bob / carl). If the
     * workbench isn't active, delegate can't find the main menus.
     * 
     * 3. click main menus Window -> Show View -> Other....
     * 
     * 4. confirm the pop-up window "Show View"
     * 
     * @param category
     *            example: "General"
     * @param nodeName
     *            example: "Console"
     */
    public void openViewWithName(String viewTitle, String category,
        String nodeName) throws RemoteException {
        if (!isViewOpen(viewTitle)) {
            rmiBot.activateEclipseShell();
            menuObject.clickMenuWithTexts(SarosConstant.MENU_TITLE_WINDOW,
                SarosConstant.MENU_TITLE_SHOW_VIEW,
                SarosConstant.MENU_TITLE_OTHER);
            rmiBot.confirmWindowWithTreeWithFilterText(
                SarosConstant.MENU_TITLE_SHOW_VIEW, category, nodeName,
                SarosConstant.BUTTON_OK);
        }
    }

    public SWTBotTree getTreeInView(String viewName) {
        return bot.viewByTitle(viewName).bot().tree();
    }

    public SWTBotTable getTableInView(String viewName) {
        return bot.viewByTitle(viewName).bot().table();
    }

    public SWTBotToolbarButton getToolbarButtonWithTooltipInView(
        String viewName, String buttonTooltip) {
        for (SWTBotToolbarButton toolbarButton : bot.viewByTitle(viewName)
            .getToolbarButtons()) {
            if (toolbarButton.getToolTipText().matches(buttonTooltip)) {
                return toolbarButton;
            }
        }
        return null;
    }

    public void closeViewWithText(String title) {
        if (isViewOpen(title)) {
            bot.viewByTitle(title).close();
        }
    }

    public void clickContextMenuOfTableInView(String viewName, String itemName,
        String contextName) {
        try {
            SWTBotTableItem item = selectTableItemWithLabelInView(viewName,
                itemName);
            item.contextMenu(contextName).click();
        } catch (WidgetNotFoundException e) {
            log.warn("contextmenu " + contextName + " of table item "
                + itemName + " on View " + viewName + " not found.", e);
        }
    }

    public SWTBotTableItem selectTableItemWithLabelInView(String viewName,
        String label) {
        try {
            SWTBotView view = bot.viewByTitle(viewName);
            SWTBotTable table = view.bot().table();
            return tableObject.selectTableItemWithLabel(table, label).select();
        } catch (WidgetNotFoundException e) {
            log.warn(" table item " + label + " on View " + viewName
                + " not found.", e);
        }
        return null;
    }

    public void clickMenusOfContextMenuOfTreeItemInView(String viewTitle,
        String[] matchTexts, String... contexts) {

        try {
            SWTBotTree tree = bot
                .viewByTitle(SarosConstant.VIEW_TITLE_PACKAGE_EXPLORER).bot()
                .tree();
            // you should first select the project,whose context you want to
            // click.
            SWTBotTreeItem treeItem = treeObject.getTreeItemWithMatchText(tree,
                matchTexts);
            treeItem.select();
            ContextMenuHelper.clickContextMenu(tree, contexts);
        } catch (WidgetNotFoundException e) {
            log.error("context menu can't be found.", e);
        }
    }

    public boolean istableItemInViewExist(String viewName, String itemName) {
        return tableObject.isTableItemExist(getTableInView(viewName), itemName);
    }

    public boolean isContextMenuOfTableItemInViewExist(String viewName,
        String itemName, String contextName) {
        activateViewWithTitle(viewName);
        SWTBotTableItem item = selectTableItemWithLabelInView(viewName,
            itemName);
        try {
            item.contextMenu(contextName);
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    public boolean isContextMenuOfTreeItemInViewExist(String viewName,
        String contextName, String... labels) throws RemoteException {
        activateViewWithTitle(viewName);
        SWTBotTreeItem item = treeObject.selectTreeWithLabelsInView(viewName,
            labels);
        try {
            item.contextMenu(contextName);
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    public boolean isMenusOfContextMenuOfTreeItemInViewExist(String viewTitle,
        String[] matchTexts, String... contexts) {
        try {
            SWTBotTree tree = bot
                .viewByTitle(SarosConstant.VIEW_TITLE_PACKAGE_EXPLORER).bot()
                .tree();
            // you should first select the project,whose context you want to
            // click.
            SWTBotTreeItem treeItem = treeObject.getTreeItemWithMatchText(tree,
                contexts);
            treeItem.select();
            ContextMenuHelper.clickContextMenu(tree, contexts);
        } catch (WidgetNotFoundException e) {
            return false;
        }
        return true;
    }

    /**
     * This method is only a helper method for "openClass". Later you can define
     * e.g. openXml, openText, openTHML using it. Make sure, the path is
     * completely defined, e.g. in openClass you should pass parameter "nodes"
     * such as Foo_Saros, src, my.pkg, MyClass.java to the method.
     * 
     * @param viewName
     *            e.g. Package Explorer view or Resource Explorer view
     * @param nodes
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the node array parameter.
     * @throws RemoteException
     */
    public void openFileInView(String viewName, String... nodes)
        throws RemoteException {
        treeObject.clickContextMenuOfTreeInView(viewName,
            SarosConstant.CONTEXT_MENU_OPEN, nodes);
    }

    public SWTBotToolbarButton clickToolbarButtonWithTooltipInView(
        String viewName, String buttonTooltip) {
        // return
        // delegate.viewByTitle(title).toolbarButton(buttonTooltip).click();
        for (SWTBotToolbarButton toolbarButton : bot.viewByTitle(viewName)
            .getToolbarButtons()) {
            if (toolbarButton.getToolTipText().matches(buttonTooltip)) {
                return toolbarButton.click();
            }
        }
        return null;

        // throw new RemoteException("Button with tooltip '" + buttonTooltip
        // + "' was not found on view with title '" + title + "'");
    }

    public void clickToolbarPushButtonWithTooltipInView(String viewName,
        String tooltip) {
        bot.viewByTitle(viewName).toolbarPushButton(tooltip).click();
    }

}
