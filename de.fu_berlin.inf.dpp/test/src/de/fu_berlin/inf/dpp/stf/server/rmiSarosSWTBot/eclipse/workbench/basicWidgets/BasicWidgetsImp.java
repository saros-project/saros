package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.basicWidgets;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotLabel;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.conditions.SarosConditions;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.EclipsePart;
import de.fu_berlin.inf.dpp.stf.server.sarosSWTBot.widgets.ContextMenuHelper;

public class BasicWidgetsImp extends EclipsePart implements BasicWidgets {

    private static transient BasicWidgetsImp eclipseBasicObjectImp;

    /**
     * {@link BasicWidgetsImp} is a singleton, but inheritance is possible.
     */
    public static BasicWidgetsImp getInstance() {
        if (eclipseBasicObjectImp != null)
            return eclipseBasicObjectImp;
        eclipseBasicObjectImp = new BasicWidgetsImp();
        return eclipseBasicObjectImp;
    }

    /**************************************************************
     * 
     * exported functions
     * 
     **************************************************************/

    /**********************************************
     * 
     * actions on the basic widget: {@link SWTBotButton}.
     * 
     **********************************************/

    // actions
    public void clickButton(String mnemonicText) throws RemoteException {
        bot.button(mnemonicText).click();
    }

    public void clickButtonWithTooltip(String tooltip) throws RemoteException {
        bot.buttonWithTooltip(tooltip).click();
    }

    // states
    public boolean isButtonEnabled(String mnemonicText) throws RemoteException {
        return bot.button(mnemonicText).isEnabled();
    }

    public boolean isButtonWithTooltipEnabled(String tooltip)
        throws RemoteException {
        return bot.buttonWithTooltip(tooltip).isEnabled();
    }

    public boolean existsButtonInGroup(String mnemonicText, String inGroup)
        throws RemoteException {
        try {
            bot.buttonInGroup(mnemonicText, inGroup);
            return true;
        } catch (WidgetNotFoundException e) {
            return false;
        }
    }

    // waits until
    public void waitUntilButtonEnabled(String mnemonicText)
        throws RemoteException {
        waitUntil(Conditions.widgetIsEnabled(bot.button(mnemonicText)));
    }

    public void waitUnitButtonWithTooltipIsEnabled(String tooltip)
        throws RemoteException {
        waitUntil(Conditions.widgetIsEnabled(bot.buttonWithTooltip(tooltip)));
    }

    /**********************************************
     * 
     * actions on the basic widget: {@link SWTBotToolbarButton}.
     * 
     **********************************************/
    // actions
    public void clickToolbarButtonWithRegexTooltipInView(String viewTitle,
        String tooltipText) throws RemoteException {
        for (SWTBotToolbarButton toolbarButton : bot.viewByTitle(viewTitle)
            .getToolbarButtons()) {
            if (toolbarButton.getToolTipText().matches(
                ".*" + tooltipText + ".*")) {
                toolbarButton.click();
                return;
            }
        }
        throw new WidgetNotFoundException(
            "The toolbarbutton with the tooltipText "
                + tooltipText
                + " doesn't exist. Are you sure that the passed tooltip text is correct?");
    }

    public void clickToolbarPushButtonWithTooltipInView(String viewTitle,
        String tooltip) throws RemoteException {
        bot.viewByTitle(viewTitle).toolbarPushButton(tooltip).click();
    }

    // states
    public boolean isToolbarButtonInViewEnabled(String viewTitle,
        String tooltipText) throws RemoteException {
        SWTBotToolbarButton button = getToolbarButtonWithTooltipInView(
            viewTitle, tooltipText);
        if (button == null)
            return false;
        return button.isEnabled();
    }

    /**********************************************
     * 
     * actions on the widget: {@link SWTBotText}.
     * 
     **********************************************/

    // actions
    public void setTextInTextWithLabel(String text, String label)
        throws RemoteException {
        bot.textWithLabel(label).setText(text);
    }

    // states
    public String getTextInTextWithLabel(String label) throws RemoteException {
        return bot.textWithLabel(label).getText();
    }

    /**********************************************
     * 
     * actions on the widget: {@link SWTBotLabel}.
     * 
     **********************************************/
    // states
    public String getTextOfLabel() throws RemoteException {
        return bot.label().getText();
    }

    public boolean existsLabel(String mnemonicText) throws RemoteException {
        try {
            bot.label(mnemonicText);
            return true;
        } catch (WidgetNotFoundException e) {
            return false;
        }
    }

    public boolean existsLabelInView(String viewTitle) throws RemoteException {
        try {
            getView(viewTitle).bot().label();
            return true;
        } catch (WidgetNotFoundException e) {
            return false;
        }
    }

    /**********************************************
     * 
     * actions on the widget: {@link SWTBotTree}.
     * 
     **********************************************/
    // actions
    public void selectTreeItem(String... nodes) throws RemoteException {
        selectTreeItem(bot.tree(), nodes);
    }

    public void selectTreeItemInView(String viewTitle, String... nodes)
        throws RemoteException {
        selectTreeItem(getTreeInView(viewTitle), nodes);
    }

    public void selectTreeItemWithRegexs(String... regexNodes)
        throws RemoteException {
        selectTreeItemWithRegexs(bot.tree(), regexNodes);
    }

    public void selectTreeItemWithRegexsInView(String viewTitle,
        String... regexNodes) throws RemoteException {
        selectTreeItemWithRegexs(getTreeInView(viewTitle), regexNodes);
    }

    public void selectTreeItemWithWaitingExpand(String... nodes)
        throws RemoteException {
        selectTreeItemWithWaitingExpand(bot.tree(), nodes);
    }

    public void selectTreeItemWithWaitingExpandInView(String viewTitle,
        String... nodes) throws RemoteException {
        selectTreeItemWithWaitingExpand(getTreeInView(viewTitle), nodes);
    }

    public void clickContextsOfTreeItem(String contextName, String... nodes)
        throws RemoteException {
        clickContextsOfTreeItem(bot.tree(), contextName, nodes);
    }

    public void clickContextsOfTreeItemInView(String viewTitle,
        String contextName, String... nodes) throws RemoteException {
        clickContextsOfTreeItem(getTreeInView(viewTitle), contextName, nodes);
    }

    public void clickSubMenuOfContextsOfTreeItem(String[] contextNames,
        String... nodes) throws RemoteException {
        clickSubMenuOfContextsOfTreeItem(bot.tree(), contextNames, nodes);
    }

    public void clickSubMenuOfContextsOfTreeItemInView(String viewTitle,
        String[] contextNames, String... nodes) throws RemoteException {
        clickSubMenuOfContextsOfTreeItem(getTreeInView(viewTitle),
            contextNames, nodes);
    }

    // states
    public List<String> getAllItemsInTreeNode(String... nodes)
        throws RemoteException {
        SWTBotTree tree = bot.tree();
        SWTBotTreeItem treeNode = tree.expandNode(nodes);
        return getAllItemsInTreeNode(treeNode);
    }

    public List<String> getAllItemsInTreeNodeInView(String viewTitle,
        String... nodes) throws RemoteException {
        SWTBotTreeItem treeNode = getTreeInView(viewTitle).expandNode(nodes);
        return getAllItemsInTreeNode(treeNode);
    }

    public List<String> getAllItemsIntree() throws RemoteException {
        return getAllItemsIntree(bot.tree());
    }

    public List<String> getAllItemsIntreeInView(String viewTitle)
        throws RemoteException {
        return getAllItemsIntree(getTreeInView(viewTitle));
    }

    public boolean existsTreeItemWithRegexs(String... regexs)
        throws RemoteException {
        return existsTreeItemWithRegexs(bot.tree(), regexs);
    }

    public boolean existsTreeItemWithRegexsInView(String viewTitle,
        String... regexs) throws RemoteException {
        return existsTreeItemWithRegexs(getTreeInView(viewTitle), regexs);
    }

    public boolean existsTreeItemInTree(String itemText) throws RemoteException {
        return existsTreeItemInTree(bot.tree(), itemText);
    }

    public boolean existsTreeItemInTreeInView(String viewTitle, String itemText)
        throws RemoteException {
        return existsTreeItemInTree(getTreeInView(viewTitle), itemText);
    }

    public boolean existsTreeItemInTreeNode(String itemText, String... nodes)
        throws RemoteException {
        return getAllItemsInTreeNode(nodes).contains(itemText);
    }

    public boolean existsTreeItemInTreeNodeInView(String viewTitle,
        String itemText, String... nodes) throws RemoteException {
        return getAllItemsInTreeNodeInView(viewTitle, nodes).contains(itemText);
    }

    public boolean existsContextOfTreeItem(String contextName, String... nodes)
        throws RemoteException {
        return existsContextOfTreeItem(bot.tree(), contextName, nodes);
    }

    public boolean existsContextOfTreeItemInView(String viewTitle,
        String contextName, String... nodes) throws RemoteException {
        return existsContextOfTreeItem(getTreeInView(viewTitle), contextName,
            nodes);
    }

    public boolean existsSuMenuOfContextOfTreeItem(String[] contextNames,
        String... nodes) throws RemoteException {
        return existsSubMenuOfContextOfTreeItem(bot.tree(), contextNames, nodes);
    }

    public boolean existsSubmenuOfContextOfTreeItemInView(String viewTitle,
        String[] contextNames, String... nodes) throws RemoteException {
        return existsSubMenuOfContextOfTreeItem(getTreeInView(viewTitle),
            contextNames, nodes);
    }

    public boolean isContextOfTreeItemEnabled(String contextName,
        String... nodes) throws RemoteException {
        return isContextOfTreeItemEnabled(bot.tree(), contextName, nodes);
    }

    public boolean isContextOfTreeItemInViewEnabled(String viewTitle,
        String contextName, String... nodes) throws RemoteException {
        return isContextOfTreeItemEnabled(getTreeInView(viewTitle),
            contextName, nodes);
    }

    public boolean isSuMenuOfContextOfTreeItemEnabled(String[] contextNames,
        String... nodes) throws RemoteException {
        return isSubMenuOfContextOfTreeItemEnabled(bot.tree(), contextNames,
            nodes);
    }

    public boolean isSubmenuOfContextOfTreeItemInViewEnabled(String viewTitle,
        String[] contextNames, String... nodes) throws RemoteException {
        return isSubMenuOfContextOfTreeItemEnabled(getTreeInView(viewTitle),
            contextNames, nodes);
    }

    // waits until
    public void waitUntilTreeItemInTreeExisted(final String itemText)
        throws RemoteException {
        waitUntilTreeItemInTreeExisted(bot.tree(), itemText);
    }

    public void waitUntilTreeItemInTreeExisted(final SWTBotTree tree,
        final String itemText) throws RemoteException {
        waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                return existsTreeItemInTree(tree, itemText);
            }

            public String getFailureMessage() {
                return "Tree " + "doesn't contain the treeItem" + itemText;
            }
        });
    }

    public void waitUntilTreeItemInTreeNodeExisted(final String itemText,
        final String... nodes) throws RemoteException {
        waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                return existsTreeItemInTreeNode(itemText, nodes);
            }

            public String getFailureMessage() {
                return "The tree node" + "doesn't contain the treeItem"
                    + itemText;
            }
        });
    }

    public void waitUntilTreeItemInTreeNodeExisted(
        final SWTBotTreeItem treeNode, final String itemText)
        throws RemoteException {
        waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                return existsTreeItemInTreeNode(treeNode, itemText);
            }

            public String getFailureMessage() {
                return "TreeNode " + treeNode.getText()
                    + "doesn't contain the treeItem" + itemText;
            }
        });
    }

    /**********************************************
     * 
     * actions on the widget: {@link SWTBotView}.
     * 
     **********************************************/
    // actions
    public void openViewById(final String viewId) throws RemoteException {
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

    public void closeViewByTitle(String title) throws RemoteException {
        if (isViewOpen(title)) {
            bot.viewByTitle(title).close();
        }
    }

    public void closeViewById(final String viewId) throws RemoteException {
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

    public void setFocusOnViewByTitle(String title) throws RemoteException {
        try {
            bot.viewByTitle(title).setFocus();
        } catch (WidgetNotFoundException e) {
            log.warn("view not found '" + title + "'", e);
        }
    }

    // states
    public boolean isViewOpen(String title) throws RemoteException {
        return getTitlesOfOpenedViews().contains(title);
    }

    public boolean isViewActive(String title) throws RemoteException {
        if (!isViewOpen(title))
            return false;
        try {
            return bot.activeView().getTitle().equals(title);
        } catch (WidgetNotFoundException e) {
            return false;
        }
    }

    public List<String> getTitlesOfOpenedViews() throws RemoteException {
        ArrayList<String> list = new ArrayList<String>();
        for (SWTBotView view : bot.views())
            list.add(view.getTitle());
        return list;
    }

    // waits until
    public void waitUntilViewActive(String viewName) throws RemoteException {
        waitUntil(SarosConditions.isViewActive(bot, viewName));
    }

    /**********************************************
     * 
     * actions on the widget: {@link SWTBotMenu}.
     * 
     **********************************************/
    public void clickMenuWithTexts(String... texts) throws RemoteException {
        workbenchC.activateEclipseShell();
        SWTBotMenu selectedmenu = null;
        for (String text : texts) {
            try {
                if (selectedmenu == null) {
                    selectedmenu = bot.menu(text);
                } else {
                    selectedmenu = selectedmenu.menu(text);
                }
            } catch (WidgetNotFoundException e) {
                log.error("menu \"" + text + "\" not found!");
                throw e;
            }
        }
        if (selectedmenu != null)
            selectedmenu.click();
    }

    /**************************************************************
     * 
     * inner functions
     * 
     **************************************************************/

    /**
     * 
     * @param viewTitle
     *            the title on the view tab.
     * @return a {@link SWTBotTree} with the specified <code>none</code> in
     *         specified view.
     */
    public SWTBotTree getTreeInView(String viewTitle) {
        return bot.viewByTitle(viewTitle).bot().tree();
    }

    /**
     * 
     * @param nodes
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the node array parameter.
     * @return a {@link SWTBotTreeItem} specified with the given
     *         <code>nodes</code>.
     */
    public SWTBotTreeItem getTreeItem(String... nodes) {
        return bot.tree().expandNode(nodes);
    }

    /**
     * @param viewTitle
     *            the title on the view tab.
     * @param nodes
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the node array parameter.
     * @return a {@link SWTBotTreeItem} specified with the given
     *         <code>nodes</code> in the given view.
     */
    public SWTBotTreeItem getTreeItemInView(String viewTitle, String... nodes) {
        try {
            return getTreeInView(viewTitle).expandNode(nodes).select();
        } catch (WidgetNotFoundException e) {
            return null;
        }
    }

    /**
     * @param viewTitle
     *            the title on the view tab.
     * @return all {@link SWTBotToolbarButton} located in the given view.
     */
    public List<SWTBotToolbarButton> getAllToolbarButtonsOnView(String viewTitle) {
        return getView(viewTitle).getToolbarButtons();
    }

    /**
     * 
     * @param viewTitle
     *            the title on the view tab.
     * @return a {@link SWTBotTable} with the specified <code>none</code> in the
     *         given view.
     */
    public SWTBotTable getTableInView(String viewTitle) {
        return bot.viewByTitle(viewTitle).bot().table();
    }

    public SWTBotToolbarButton getToolbarButtonWithTooltipInView(
        String viewTitle, String tooltipText) {
        for (SWTBotToolbarButton toolbarButton : getView(viewTitle)
            .getToolbarButtons()) {
            if (toolbarButton.getToolTipText().matches(
                ".*" + tooltipText + ".*")) {
                return toolbarButton;
            }
        }
        return null;
    }

    /**
     * @param viewTitle
     *            the title on the view tab.
     * @return the {@link SWTBotView} specified with the given title.
     */
    public SWTBotView getView(String viewTitle) {
        return bot.viewByTitle(viewTitle);
    }

    public SWTBotTreeItem getTreeItemWithRegexs(SWTBotTree tree,
        String... regexNodes) {
        SWTBotTreeItem currentItem = null;
        SWTBotTreeItem[] allChildrenOfCurrentItem;
        for (String regex : regexNodes) {
            if (currentItem == null) {
                allChildrenOfCurrentItem = tree.getAllItems();
            } else {
                allChildrenOfCurrentItem = currentItem.getItems();
            }
            boolean itemFound = false;
            for (SWTBotTreeItem child : allChildrenOfCurrentItem) {
                log.info("treeItem name: " + child.getText());
                if (child.getText().matches(regex)) {
                    currentItem = child;
                    if (!child.isExpanded())
                        child.expand();
                    itemFound = true;
                    continue;
                }
            }
            if (!itemFound) {
                throw new WidgetNotFoundException("Tree item \"" + regex
                    + "\" not found. Nodes: " + Arrays.asList(regexNodes));
            }
        }
        return currentItem;
    }

    public void selectTreeItem(SWTBotTree tree, String... nodes) {
        try {
            tree.expandNode(nodes).select();
        } catch (WidgetNotFoundException e) {
            log.warn("tree item can't be found.", e);
        }
    }

    /**
     * select a treeItem specified with the given regexs. This method ist very
     * helpful, if you are not sure, how exactly is the tree item's name.
     * 
     * @param tree
     *            a {@link SWTBotTree} with the specified <code>none</code>
     * @param regexNodes
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the regex array parameter.e.g.
     *            {"Buddies","bob_stf@saros-con.imp.fu-berlin.de.*" }
     * 
     */
    public void selectTreeItemWithRegexs(SWTBotTree tree, String... regexNodes) {
        assert tree != null : "the passed tree is null.";
        SWTBotTreeItem currentItem = null;
        SWTBotTreeItem[] allChildrenOfCurrentItem;
        for (String regex : regexNodes) {
            if (currentItem == null) {
                allChildrenOfCurrentItem = tree.getAllItems();
            } else {
                allChildrenOfCurrentItem = currentItem.getItems();
            }
            boolean itemWithRegexFound = false;
            for (SWTBotTreeItem child : allChildrenOfCurrentItem) {
                log.info("treeItem name: " + child.getText());
                if (child.getText().matches(regex)) {
                    currentItem = child;
                    if (!child.isExpanded())
                        child.expand();
                    itemWithRegexFound = true;
                    break;
                }
            }
            if (!itemWithRegexFound) {
                throw new WidgetNotFoundException("Tree item matching the \""
                    + regex + "\" can't be found. Nodes: "
                    + Arrays.asList(regexNodes));
            }
        }
        if (currentItem != null)
            currentItem.select();
    }

    public void selectTreeItemWithWaitingExpand(SWTBotTree tree,
        String... nodes) throws RemoteException {
        SWTBotTreeItem selectedTreeItem = null;
        for (String node : nodes) {
            try {
                if (selectedTreeItem == null) {
                    waitUntilTreeItemInTreeExisted(tree, node);
                    selectedTreeItem = tree.expandNode(node);
                    log.info("treeItem name: " + selectedTreeItem.getText());
                } else {
                    waitUntilTreeItemInTreeNodeExisted(selectedTreeItem, node);
                    selectedTreeItem = selectedTreeItem.expandNode(node);
                    log.info("treeItem name: " + selectedTreeItem.getText());
                }
            } catch (WidgetNotFoundException e) {
                log.error("treeitem \"" + node + "\" not found");
            }
        }
        if (selectedTreeItem != null) {
            log.info("treeItem name: " + selectedTreeItem.getText());
            selectedTreeItem.select();

        }
    }

    /**
     * This method ist very helpful, if you are not sure, how exactly is the
     * tree item's name.
     * 
     * @param tree
     *            a {@link SWTBotTree} with the specified <code>none</code>
     * @param regexs
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the regex array parameter.e.g.
     *            {"Buddies","bob_stf@saros-con.imp.fu-berlin.de.*" }
     * @return <tt>true</tt>, if the three item specified with the given regexs
     *         exists
     */
    public boolean existsTreeItemWithRegexs(SWTBotTree tree, String... regexs) {
        try {
            selectTreeItemWithRegexs(tree, regexs);
            return true;
        } catch (WidgetNotFoundException e) {
            return false;
        }
    }

    public boolean existsTreeItemInTree(SWTBotTree tree, String itemText) {
        return getAllItemsIntree(tree).contains(itemText);
    }

    public boolean existsTreeItemInTreeNode(SWTBotTreeItem treeNode,
        String itemText) {
        return getAllItemsInTreeNode(treeNode).contains(itemText);
    }

    public boolean existsContextOfTreeItem(SWTBotTree tree, String contextName,
        String... nodes) {
        selectTreeItemWithRegexs(tree, nodes);
        return ContextMenuHelper.existsContextMenu(tree, contextName);
    }

    public boolean existsSubMenuOfContextOfTreeItem(SWTBotTree tree,
        String[] contextNames, String... nodes) {
        selectTreeItemWithRegexs(tree, nodes);
        return ContextMenuHelper.existsContextMenu(tree, contextNames);
    }

    public boolean isContextOfTreeItemEnabled(SWTBotTree tree,
        String contextName, String... nodes) {
        selectTreeItemWithRegexs(tree, nodes);
        return ContextMenuHelper.isContextMenuEnabled(tree, contextName);
    }

    public boolean isSubMenuOfContextOfTreeItemEnabled(SWTBotTree tree,
        String[] contextNames, String... nodes) {
        selectTreeItemWithRegexs(tree, nodes);
        return ContextMenuHelper.isContextMenuEnabled(tree, contextNames);
    }

    public void clickContextsOfTreeItem(SWTBotTree tree, String contextName,
        String... nodes) {
        selectTreeItemWithRegexs(tree, nodes);
        ContextMenuHelper.clickContextMenu(tree, contextName);
    }

    public void clickSubMenuOfContextsOfTreeItem(SWTBotTree tree,
        String[] contextNames, String... nodes) {
        selectTreeItemWithRegexs(tree, nodes);
        ContextMenuHelper.clickContextMenu(tree, contextNames);
    }

    public List<String> getAllItemsInTreeNode(SWTBotTreeItem treeNode) {
        List<String> allItemTexts = new ArrayList<String>();
        for (SWTBotTreeItem item : treeNode.getItems()) {
            allItemTexts.add(item.getText());
            log.info("existed subTreeItem of the TreeNode "
                + treeNode.getText() + ": " + item.getText());
        }
        return allItemTexts;
    }

    public List<String> getAllItemsIntree(SWTBotTree tree) {
        List<String> allItemTexts = new ArrayList<String>();
        for (SWTBotTreeItem item : tree.getAllItems()) {
            allItemTexts.add(item.getText());
            log.info("existed treeItem of the tree: " + item.getText());
        }
        return allItemTexts;
    }
}
