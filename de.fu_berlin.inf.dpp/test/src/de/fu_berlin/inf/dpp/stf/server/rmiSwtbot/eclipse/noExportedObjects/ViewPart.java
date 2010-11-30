package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noExportedObjects;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTableItem;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.swtbot.swt.finder.widgets.TimeoutException;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import de.fu_berlin.inf.dpp.stf.sarosSWTBot.SarosSWTBot;
import de.fu_berlin.inf.dpp.stf.sarosSWTBot.widgets.ContextMenuHelper;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.conditions.SarosConditions;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.conditions.SarosSWTBotPreferences;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.EclipseComponent;

/**
 * This class contains basic API to find widgets based on a view in SWTBot and
 * to perform the operations on it, which is only used by rmi server side and
 * not exported.
 * 
 * @author lchen
 */
public class ViewPart extends EclipseComponent {

    public SWTBotView getView(String title) {
        return bot.viewByTitle(title);
    }

    public List<SWTBotToolbarButton> getToolbarButtonsOnView(String title) {
        return getView(title).getToolbarButtons();
    }

    /**
     * Set focus on the specified view. It should be only called if View is
     * open.
     * 
     * @param title
     *            the title on the view tab.
     * @see SWTBotView#setFocus()
     */
    public void setFocusOnViewByTitle(String title) {
        try {
            bot.viewByTitle(title).setFocus();
        } catch (WidgetNotFoundException e) {
            log.warn("view not found '" + title + "'", e);
        }
    }

    /**
     * @param title
     *            the title on the view tab.
     * @return <tt>true</tt> if the specified view is active.
     */
    public boolean isViewActive(String title) {
        if (!isViewOpen(title))
            return false;
        try {
            return bot.activeView().getTitle().equals(title);
        } catch (WidgetNotFoundException e) {
            return false;
        }
    }

    /**
     * @param title
     *            the title on the view tab.
     * @return <tt>true</tt> if the specified view is open.
     * @see ViewPart#getTitlesOfAllOpenedViews()
     */
    public boolean isViewOpen(String title) {
        return getTitlesOfAllOpenedViews().contains(title);
    }

    /**
     * @return all titles of the views which are opened currently.
     * @see SWTWorkbenchBot#views()
     */
    public List<String> getTitlesOfAllOpenedViews() {
        ArrayList<String> list = new ArrayList<String>();
        for (SWTBotView view : bot.views())
            list.add(view.getTitle());
        return list;
    }

    /**
     * 
     * @param viewName
     *            the title on the view tab.
     * @return a {@link SWTBotTree} with the specified <code>none</code> in
     *         specified view.
     */
    public SWTBotTree getTreeInView(String viewName) {
        return bot.viewByTitle(viewName).bot().tree();
    }

    /**
     * 
     * @param title
     *            the title on the view tab.
     * @return {@link SWTBotTable} with the specified <code>none</code> in
     *         specified view.
     */
    public SWTBotTable getTableInView(String title) {
        return bot.viewByTitle(title).bot().table();
    }

    /**
     * @param title
     *            the title on the view tab.
     * @param tooltipText
     *            the tooltip text of the button.
     * @return the toolbar_button with the specified tooltipText.
     */
    public SWTBotToolbarButton getToolbarButtonWithTooltipInView(String title,
        String tooltipText) {
        for (SWTBotToolbarButton toolbarButton : bot.viewByTitle(title)
            .getToolbarButtons()) {
            if (toolbarButton.getToolTipText().matches(tooltipText)) {
                return toolbarButton;
            }
        }
        return null;
    }

    /**
     * close the specified view
     * 
     * @param title
     *            the title on the view tab.
     */
    public void closeViewByTitle(String title) {
        if (isViewOpen(title)) {
            bot.viewByTitle(title).close();
        }
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

    /**
     * Select a table item in a view. The method is defined as helper method for
     * other selectTB*In*View methods in {@link SarosSWTBot} and should not be
     * exported by rmi. <br/>
     * 
     * @param viewName
     * 
     * @param label
     *            the table item' name, which you want to select.
     */
    public SWTBotTableItem selectTableItemWithLabelInView(String viewName,
        String label) {
        try {
            SWTBotTable table = getTableInView(viewName);
            return tablePart.selectTableItemWithLabel(table, label).select();
        } catch (WidgetNotFoundException e) {
            log.warn(" table item " + label + " on View " + viewName
                + " not found.", e);
        }
        return null;
    }

    /**
     * This method is very useful, if you want to click a submenu of the context
     * menu of a selected treeitem.e.g. the context menu "Team->Commit...". You
     * should first select the project that his context menu you want to click.
     * 
     * @param viewTitle
     *            the view title
     * @param nodes
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the node array parameter.e.g.{"Foo-saros",
     *            "my.pkg", "myClass.java"}
     * @param contexts
     *            all context menus'name.e.g. {"Team", "Commit..."}
     * @throws RemoteException
     */
    public void clickContextMenusOfTreeItemInView(String viewTitle,
        String[] nodes, String... contexts) throws RemoteException {
        SWTBotTree tree = getTreeInView(viewTitle);
        SWTBotTreeItem treeItem = treePart
            .getTreeItemWithMatchText(tree, nodes);
        treeItem.select();

        ContextMenuHelper.clickContextMenu(tree, contexts);

    }

    /**
     * @param viewName
     *            the view title.
     * @return <tt>true</tt> if the specified table item exists.
     * 
     */
    public boolean isTableItemInViewExist(String viewName, String itemName) {
        return tablePart.existTableItem(itemName);
    }

    /**
     * 
     * @param viewName
     *            the view title.
     * @param itemName
     *            the table item name.
     * @param contextName
     * @return <tt>true</tt> if the specified context menu of the select table
     *         item exists.
     */
    public boolean isContextMenuOfTableItemInViewExist(String viewName,
        String itemName, String contextName) {
        setFocusOnViewByTitle(viewName);
        SWTBotTableItem item = selectTableItemWithLabelInView(viewName,
            itemName);
        try {
            item.contextMenu(contextName);
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    /**
     * 
     * @param viewName
     * @param contextName
     * @param nodes
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the node array parameter.e.g.{"Foo-saros",
     *            "my.pkg", "myClass.java"}
     * @return <tt>true</tt> if the specified context menu of the select tree
     *         item exists.
     * @throws RemoteException
     */
    public boolean isContextMenuOfTreeItemInViewExist(String viewName,
        String contextName, String... nodes) throws RemoteException {
        setFocusOnViewByTitle(viewName);
        SWTBotTreeItem item = selectTreeWithLabelsInView(viewName, nodes);
        try {
            item.contextMenu(contextName);
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    /**
     * @param viewTitle
     *            the view title
     * @param nodes
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the node array parameter.e.g.{"Foo-saros",
     *            "my.pkg", "myClass.java"}.
     * @param contexts
     *            all context menus'name.e.g. {"Team", "Commit..."}.
     * @return <tt>true</tt>, if the submenus of the selected treeitem's context
     *         exists.
     */
    public boolean isMenusOfContextMenuOfTreeItemInViewExist(String viewTitle,
        String[] nodes, String... contexts) {
        try {
            SWTBotTree tree = getTreeInView(viewTitle);
            // you should first select the project,whose context you want to
            // click.
            SWTBotTreeItem treeItem = treePart.getTreeItemWithMatchText(tree,
                nodes);
            treeItem.select();
            ContextMenuHelper.clickContextMenu(tree, contexts);
        } catch (WidgetNotFoundException e) {
            return false;
        }
        return true;
    }

    /**
     * click toolbar button in view. e.g. connect. if you don't know the tooltip
     * exactly, please use this method.
     * 
     * @param viewName
     * @param buttonTooltip
     * @return
     */
    public SWTBotToolbarButton clickToolbarButtonWithTooltipInView(
        String viewName, String buttonTooltip) {

        for (SWTBotToolbarButton toolbarButton : bot.viewByTitle(viewName)
            .getToolbarButtons()) {
            if (toolbarButton.getToolTipText().matches(buttonTooltip)) {

                return toolbarButton.click();
            }
        }
        return null;
    }

    /**
     * click toolbar button in view. e.g. connect. You need to pass the full
     * name of the tooltip. exactly.
     * 
     * @param viewName
     * @param tooltip
     */
    public void clickToolbarPushButtonWithTooltipInView(String viewName,
        String tooltip) {
        bot.viewByTitle(viewName).toolbarPushButton(tooltip).click();
    }

    public void closeViewById(final String viewId) {
        Display.getDefault().syncExec(new Runnable() {
            public void run() {
                final IWorkbench wb = PlatformUI.getWorkbench();
                final IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
                IWorkbenchPage page = win.getActivePage();
                final IViewPart view = page.findView(viewId);
                if (view != null) {
                    page.hideView(view);
                }
            }
        });
    }

    public void openViewById(final String viewId) {
        try {
            Display.getDefault().syncExec(new Runnable() {
                public void run() {
                    final IWorkbench wb = PlatformUI.getWorkbench();
                    final IWorkbenchWindow win = wb.getActiveWorkbenchWindow();

                    IWorkbenchPage page = win.getActivePage();
                    try {
                        IViewReference[] registeredViews = page
                            .getViewReferences();
                        for (IViewReference registeredView : registeredViews) {
                            log.debug("registered view ID: "
                                + registeredView.getId());
                        }

                        page.showView(viewId);
                    } catch (PartInitException e) {
                        throw new IllegalArgumentException(e);
                    }
                }
            });
        } catch (IllegalArgumentException e) {
            log.debug("Couldn't initialize " + viewId, e.getCause());
        }
    }

    /**
     * @param viewName
     *            the title of the specified view
     * @param labels
     *            all labels on the widget
     * @return a {@link SWTBotTreeItem} with the specified <code>label</code>.
     */

    public SWTBotTreeItem selectTreeWithLabelsInView(String viewName,
        String... labels) {
        try {
            return getTreeInView(viewName).expandNode(labels).select();
        } catch (WidgetNotFoundException e) {
            return null;
        }
    }

    public SWTBotTreeItem selectTreeWithLabelsInViewWithWaitungExpand(
        String viewName, String... labels) {
        return treePart.selectTreeWithLabelsWithWaitungExpand(
            getTreeInView(viewName), labels);
    }

    /**
     * This method is a helper method and should not be exported by rmi. it is
     * very helpful, when you want to click a context menu of a tree node in a
     * view.e.g.
     * 
     * in Package Explorer View: click context menu "open" of the class file
     * "MyClass", in this case, you should pass the
     * parameters("Package Explorer", "open", "Foo_Saros","src", "my.pkg",
     * "MyClass.java".
     * 
     * in Roster View: click context menu "rename.." of a user
     * "lin@saros-con.imp.fu-berlin.de" in buddies. In this case, you shuld pass
     * the parameter ("Roster", "rename...", "Buddies",
     * "lin@saros-con.imp.fu-berlin.de").
     * 
     * 1. select the tree node that context menu you want to click.
     * 
     * 2. then click the context menu.
     * 
     * @param viewName
     *            e.g. Package Explorer view or Resource Explorer view
     * @param nodes
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the node array parameter.
     * @throws RemoteException
     */
    public void clickContextMenuOfTreeInView(String viewName, String context,
        String... nodes) throws RemoteException {
        String[] contexts = { context };
        clickContextMenusOfTreeItemInView(viewName, nodes, contexts);
    }

    public boolean isToolbarInViewEnabled(String viewName, String buttonTooltip) {
        SWTBotToolbarButton button = getToolbarButtonWithTooltipInView(
            viewName, buttonTooltip);
        if (button == null)
            return false;
        return button.isEnabled();
    }

    /**
     * 
     * Waits until the {@link SarosSWTBotPreferences#SAROS_TIMEOUT} is reached
     * or the view is active.
     * 
     * @param viewName
     *            name of the view, which should be active.
     */
    public void waitUntilViewActive(String viewName) {
        waitUntil(SarosConditions.isViewActive(bot, viewName));
    }

}
