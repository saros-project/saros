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

    /**********************************************
     * 
     * open/activate/close view
     * 
     **********************************************/
    /**
     * open the given view specified with the viewId.
     * 
     * @param viewId
     *            the id of the view, which you want to open.
     */
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
     * @param title
     *            the title on the view tab.
     * @return <tt>true</tt> if the specified view is open.
     * @see ViewPart#getTitlesOfOpenedViews()
     */
    public boolean isViewOpen(String title) {
        return getTitlesOfOpenedViews().contains(title);
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
     * close the given view specified with the viewId.
     * 
     * @param viewId
     *            the id of the view, which you want to close.
     */
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

    /**********************************************
     * 
     * get Widget on the given view
     * 
     **********************************************/
    /**
     * @param title
     *            the title on the view tab.
     * @return the {@link SWTBotView} specified with the given title.
     */
    public SWTBotView getView(String title) {
        return bot.viewByTitle(title);
    }

    /**
     * @return the title list of all the views which are opened currently.
     * @see SWTWorkbenchBot#views()
     */
    public List<String> getTitlesOfOpenedViews() {
        ArrayList<String> list = new ArrayList<String>();
        for (SWTBotView view : bot.views())
            list.add(view.getTitle());
        return list;
    }

    /**
     * @param title
     *            the title on the view tab.
     * @param tooltipText
     *            the tool tip text of the toolbar_button.
     * @return the toolbar_button with the specified tooltipText on the given
     *         view.
     */
    public SWTBotToolbarButton getToolbarButtonWithTooltipInView(String title,
        String tooltipText) {
        for (SWTBotToolbarButton toolbarButton : bot.viewByTitle(title)
            .getToolbarButtons()) {
            if (toolbarButton.getToolTipText().matches(
                ".*" + tooltipText + ".*")) {
                return toolbarButton;
            }
        }
        return null;
    }

    /**
     * @param title
     *            the title on the view tab.
     * @return all {@link SWTBotToolbarButton} located in the given view.
     */
    public List<SWTBotToolbarButton> getAllToolbarButtonsOnView(String title) {
        return getView(title).getToolbarButtons();
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

    /**********************************************
     * 
     * select Widget on the given view
     * 
     **********************************************/
    /**
     * Select a table item specified with the given label in the given view.
     * 
     * @param viewName
     *            the title on the view tab.
     * @param label
     *            the table item' name, which you want to select.
     */
    public SWTBotTableItem selectTableItemWithLabelInView(String viewName,
        String label) {
        try {
            SWTBotTable table = getTableInView(viewName);
            return table.getTableItem(label).select();
        } catch (WidgetNotFoundException e) {
            log.warn(" table item " + label + " on View " + viewName
                + " not found.", e);
        }
        return null;
    }

    /**
     * @param viewName
     *            the title on the view tab.
     * @param labels
     *            all labels on the treeitem widget
     * @return a {@link SWTBotTreeItem} with the specified <code>label</code>.
     */

    public SWTBotTreeItem selectTreeItemWithLabelsInView(String viewName,
        String... labels) {
        try {
            return getTreeInView(viewName).expandNode(labels).select();
        } catch (WidgetNotFoundException e) {
            return null;
        }
    }

    /**
     * {@link ViewPart#selectTreeItemWithLabelsInView(String, String...)}is
     * different as this method, which need to wait until a selected treeitem is
     * really expanded and then going on.
     * 
     * @param viewName
     *            the title on the view tab.
     * @param labels
     *            all labels on the treeitem widget
     * @return a {@link SWTBotTreeItem} with the specified <code>label</code>.
     */
    public SWTBotTreeItem selectTreeItemWithLabelsInViewWithWaitungExpand(
        String viewName, String... labels) {
        return treePart.selectTreeWithLabelsWithWaitungExpand(
            getTreeInView(viewName), labels);
    }

    /**********************************************
     * 
     * click Widget on the given view
     * 
     **********************************************/
    /**
     * Click a context menu of the selected table item in the given view..
     * <p>
     * Operational steps:
     * <ol>
     * <li>select a table item</li>
     * <li>then click the specified context menu on it</li>
     * </ol>
     * 
     * @param viewName
     *            the title on the view tab.
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
     * This method is very useful, if you want to click a sub menu of a context
     * menu of the treeitem specified with the given nodes. e.g. the sub menu
     * "Team->Commit...".
     * 
     * @param viewTitle
     *            the title on the view tab.
     * @param nodes
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the node array parameter.e.g.{"Foo-saros",
     *            "my.pkg", "myClass.java"}
     * @param contexts
     *            context menu of the given treeitem and his sub menus. e.g.
     *            {"Team", "Commit..."}
     * @throws RemoteException
     */
    public void clickSubmenusOfContextMenuOfTreeItemInView(String viewTitle,
        String[] nodes, String... contexts) throws RemoteException {
        SWTBotTree tree = getTreeInView(viewTitle);
        SWTBotTreeItem treeItem = treePart.getTreeItemWithRegexNodes(tree,
            nodes);
        treeItem.select();
        ContextMenuHelper.clickContextMenu(tree, contexts);

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
     *            the title on the view tab. e.g. Package Explorer view or
     *            Resource Explorer view
     * @param nodes
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the node array parameter.
     * @throws RemoteException
     */
    public void clickContextMenuOfTreeItemInView(String viewName,
        String context, String... nodes) throws RemoteException {
        String[] contexts = { context };
        clickSubmenusOfContextMenuOfTreeItemInView(viewName, nodes, contexts);
    }

    /**
     * click the toolbar button specified with the given buttonTooltip in the
     * passed view.<br/>
     * 
     * <b>NOTE</b>, when you are not sure about the full tooltipText of the
     * toolbarButton, please use this method.
     * 
     * @param viewName
     *            the title on the view tab.
     * @param buttonTooltip
     *            the tooltip of the toolbar button which you want to click.
     * @return
     */
    public SWTBotToolbarButton clickToolbarButtonWithTooltipInView(
        String viewName, String buttonTooltip) {
        for (SWTBotToolbarButton toolbarButton : bot.viewByTitle(viewName)
            .getToolbarButtons()) {
            if (toolbarButton.getToolTipText().matches(
                ".*" + buttonTooltip + ".*")) {
                return toolbarButton.click();
            }
        }
        throw new WidgetNotFoundException(
            "The toolbarbutton with the tooltipText "
                + buttonTooltip
                + " doesn't exist. Are you sure that the passed tooltip text is correct?");
    }

    /**
     * click the toolbar button specified with the given tooltip in the given
     * view. e.g. connect. You need to pass the full tooltiptext.
     * 
     * @param viewName
     *            the title on the view tab.
     * @param tooltip
     *            the tooltip of the toolbar button which you want to click.
     */
    public void clickToolbarPushButtonWithTooltipInView(String viewName,
        String tooltip) {
        bot.viewByTitle(viewName).toolbarPushButton(tooltip).click();
    }

    /**********************************************
     * 
     * Does the given widget exist on the given view?
     * 
     **********************************************/
    /**
     * @param viewName
     *            the title on the view tab.
     * @return <tt>true</tt> if the specified table item exists.
     * 
     */
    public boolean existsTableItemInView(String viewName, String itemName) {
        return getTableInView(viewName).containsItem(itemName);
    }

    /**
     * 
     * @param viewName
     *            the title on the view tab.
     * @param itemName
     *            the name of the table item.
     * @param contextName
     *            the name of the context menu of the given tableItem specified
     *            with the given itemName
     * @return <tt>true</tt> if the specified context menu of the select table
     *         item exists.
     */
    public boolean existsContextMenuOfTableItemInView(String viewName,
        String itemName, String contextName) {

        if (!existsTableItemInView(viewName, itemName))
            return false;
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
     *            the title on the view tab.
     * @param contextName
     *            then name of the context menu of the selected treeitem
     *            specified with the given nodes.
     * @param nodes
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the node array parameter.e.g.{"Foo-saros",
     *            "my.pkg", "myClass.java"}
     * @return <tt>true</tt> if the specified context menu of the select tree
     *         item exists.
     * @throws RemoteException
     */
    public boolean existsContextMenuOfTreeItemInView(String viewName,
        String contextName, String... nodes) throws RemoteException {
        setFocusOnViewByTitle(viewName);
        SWTBotTreeItem item = selectTreeItemWithLabelsInView(viewName, nodes);
        try {
            item.contextMenu(contextName);
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    /**
     * @param viewTitle
     *            the title on the view tab.
     * @param nodes
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the node array parameter.e.g.{"Foo-saros",
     *            "my.pkg", "myClass.java"}.
     * @param contexts
     *            context menu of the given treeitem and his sub menus. e.g.
     *            {"Team", "Commit..."}
     * @return <tt>true</tt>, if the submenus of the selected treeitem's context
     *         exists.
     */
    public boolean existsSubmenusOfContextMenuOfTreeItemInView(
        String viewTitle, String[] nodes, String... contexts) {
        try {
            SWTBotTree tree = getTreeInView(viewTitle);
            // you should first select the project,whose context you want to
            // click.
            SWTBotTreeItem treeItem = treePart.getTreeItemWithRegexNodes(tree,
                nodes);
            treeItem.select();
            ContextMenuHelper.clickContextMenu(tree, contexts);
        } catch (WidgetNotFoundException e) {
            return false;
        }
        return true;
    }

    /**
     * @return<tt>true</tt>, if there are some label texts existed in the given
     *                       view. You can only see the label texts when you are
     *                       not in a session.
     * 
     * @param viewName
     *            the title on the view tab.
     */
    public boolean existsLabelTextInView(String viewName) {
        try {
            viewPart.getView(viewName).bot().label();
            return true;
        } catch (WidgetNotFoundException e) {
            return false;
        }
    }

    /**********************************************
     * 
     * Is the given widget enabled on the given view?
     * 
     **********************************************/
    /**
     * 
     * @param viewName
     *            the title on the view tab.
     * @param buttonTooltip
     *            the tooltip of the toolbar button which you want to know, if
     *            it is enabled.
     * @return <tt>true</tt>, if the given toolbar button is enabled.
     */
    public boolean isToolbarInViewEnabled(String viewName, String buttonTooltip) {
        SWTBotToolbarButton button = getToolbarButtonWithTooltipInView(
            viewName, buttonTooltip);
        if (button == null)
            return false;
        return button.isEnabled();
    }

    /**********************************************
     * 
     * wait until widget is *
     * 
     **********************************************/

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
