package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.waits.Conditions;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotLabel;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;

import de.fu_berlin.inf.dpp.stf.client.Tester;
import de.fu_berlin.inf.dpp.stf.client.testProject.helpers.TestPattern;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.conditions.SarosSWTBotPreferences;

/**
 * This interface contains convenience API to perform a action using basic
 * widgets. You can start off as follows:
 * <ol>
 * <li>
 * At first you need to create a {@link Tester} object in your junit-test. (How
 * to do it please look at the javadoc in class {@link TestPattern} or read the
 * user guide in TWiki https://www.inf.fu-berlin.de/w/SE/SarosSTFTests).</li>
 * <li>
 * then you can use the object basic initialized in {@link Tester} to access the
 * API :), e.g.
 * 
 * <pre>
 * alice.basic.clickButton(&quot;Finish&quot;);
 * </pre>
 * 
 * </li>
 * 
 * @author Lin
 */
public interface BasicComponent extends Remote {

    /**********************************************
     * 
     * basic widget: {@link SWTBotButton}.
     * 
     **********************************************/

    /**
     * clicks the button specified with the given mnemonicText.
     * 
     * @param mnemonicText
     *            the mnemonicText on the widget, e.g. Button "Finish" in a
     *            dialog.
     */
    public void clickButton(String mnemonicText) throws RemoteException;

    /**
     * clicks the button specified with the given tooltip.
     * 
     * @param tooltip
     *            the tooltip on the widget,
     * @throws RemoteException
     */
    public void clickButtonWithTooltip(String tooltip) throws RemoteException;

    /**
     * 
     * @param mnemonicText
     *            the mnemonicText on the widget, e.g. Button "Finish" in a
     *            dialog.
     * @return<tt>true</tt>, if the given button is enabled.
     * @throws RemoteException
     */
    public boolean isButtonEnabled(String mnemonicText) throws RemoteException;

    public boolean existsButtonInGroup(String mnemonicText, String inGroup)
        throws RemoteException;

    /**
     * 
     * @param tooltip
     *            the tooltip on the widget,
     * @return<tt>true</tt>, if the button specified with the given tooltip is
     *                       enabled.
     * @throws RemoteException
     */
    public boolean isButtonWithTooltipEnabled(String tooltip)
        throws RemoteException;

    /**
     * Waits until the button is enabled.
     * 
     * @param mnemonicText
     *            the mnemonicText on the widget.
     */
    public void waitUntilButtonEnabled(String mnemonicText)
        throws RemoteException;

    /**
     * Waits until the button is enabled.
     * 
     * @param tooltipText
     *            the tooltip on the widget.
     */
    public void waitUnitButtonWithTooltipIsEnabled(String tooltipText)
        throws RemoteException;

    /**********************************************
     * 
     * basic widget: {@link SWTBotText}.
     * 
     **********************************************/

    /**
     * set the given text into the {@link SWTBotText} with the specified
     * 
     * @param text
     *            the text which you want to insert into the text field
     * @param label
     *            the label on the widget.
     * @throws RemoteException
     */
    public void setTextInTextWithLabel(String text, String label)
        throws RemoteException;

    /**
     * 
     * @param label
     *            the label on the widget.
     * @return the text in the given {@link SWTBotText}
     * @throws RemoteException
     */
    public String getTextInTextWithLabel(String label) throws RemoteException;

    /**********************************************
     * 
     * basic widget: {@link SWTBotLabel}.
     * 
     **********************************************/
    /**
     * 
     * @return the text of the first found {@link SWTBotLabel}
     * @throws RemoteException
     */
    public String geFirstLabelText() throws RemoteException;

    /**
     * 
     * @param label
     *            the text of the label
     * @return<tt>true</tt>, if the given label exists.
     * @throws RemoteException
     */
    public boolean existsLabel(String label) throws RemoteException;

    /**********************************************
     * 
     * actions with basic widget: {@link SWTBotTable}.
     * 
     **********************************************/

    /**
     * Selects the tableItem matching the given itemText.
     * 
     * @param itemText
     *            text of the selected table item.
     * @return the object {@link SWTBotTableItem} specified with the given
     *         itemText.
     * @throws RemoteException
     */
    ;

    public void selectTableItem(String itemText) throws RemoteException;

    public void selectTableItemInView(String viewTitle, String itemText)
        throws RemoteException;

    /**
     * Click the context menu matching the given contextName of the selected
     * table item matching the given itemText.<br/>
     * Operational steps:
     * <ol>
     * <li>select a table item</li>
     * <li>then click the specified context menu on it</li>
     * </ol>
     * 
     * @param itemText
     *            text of the selected table item, whose context menu you want
     *            to click.
     * @param contextName
     *            the text on the context menu
     * 
     */
    public void clickContextMenuOfTable(String itemText, String contextName)
        throws RemoteException;

    /**
     * 
     * @param itemText
     *            text of the selected table item.
     * @param contextName
     *            the text on the context menu.
     * @return<tt>true</tt>, if the specified contextMenu of the selected
     *                       tableItem is visible.
     * @throws RemoteException
     */
    public boolean isContextMenuOfTableVisible(String itemText,
        String contextName) throws RemoteException;

    public boolean isContextMenuOfTableVisibleInView(String viewTitle,
        String itemText, String contextName) throws RemoteException;

    /**
     * 
     * @param itemText
     *            text of the selected table item.
     * @param contextName
     *            the text on the context menu
     * @return<tt>true</tt>, if the specified contextMenu of the selected
     *                       tableItem is enabled.
     * @throws RemoteException
     */
    public boolean isContextMenuOfTableEnabled(String itemText,
        String contextName) throws RemoteException;

    public boolean isContextMenuOfTableEnabledInView(String viewTitle,
        String itemText, String contextName) throws RemoteException;

    /**
     * checks the checkBox of the tableItem specified with the given itemText.
     * 
     * @param itemText
     *            text of the selected table item.
     * @throws RemoteException
     */
    public void selectCheckBoxInTable(String itemText) throws RemoteException;

    /**
     * checks the checkBox of all the tableItems specified with the given
     * itemTexts.
     * 
     * @param itemTexts
     *            the text list of all the tableItems, whose checkbox may be
     *            selected.
     * @throws RemoteException
     */
    public void selectCheckBoxsInTable(List<String> itemTexts)
        throws RemoteException;

    /**
     * 
     * @return the names of all columns of the table.
     * @throws RemoteException
     */
    public List<String> getTableColumns() throws RemoteException;

    /**
     * 
     * @param itemText
     *            text of a table item.
     * @return<tt>true</tt>, if the table contains the specified tableItem.
     * @throws RemoteException
     */
    public boolean existsTableItem(String itemText) throws RemoteException;

    public boolean existsTableItemInView(String viewTitle, String itemText)
        throws RemoteException;

    /**
     * 
     * TODO optimize this function, it takes too long to identify whether a
     * context exist because of waiting until timeout.
     * 
     * @param itemText
     *            name of the table item, whose context menu you want to
     *            check,if it exists.
     * @param contextName
     *            name of the context menu you want to check,if it exists.
     * @return <tt>true</tt>, if the context menu of the specified table item
     *         exists.
     */
    // public boolean existsContextOfTableItem(String itemText, String
    // contextName)
    // throws RemoteException;

    /**
     * 
     * TODO it don't work yet, need to be fixed.
     * 
     * @param itemText
     *            name of the table item, whose context menu you want to
     *            check,if it exists.
     * @param contextName
     *            name of the context menu you want to check,if it exists.
     * @return <tt>true</tt>, if the context menu of the specified table item
     *         exists.
     */
    // public boolean existContextOfTableItem(
    // final AbstractSWTBot<? extends Control> bot, final String itemText,
    // final String contextName) throws RemoteException;

    /**
     * waits until if the specified tableItem exists.
     * 
     * @param basic
     *            the object {@link BasicComponent}
     * @param itemText
     *            name of the table item, which you want to check,if it exists.
     * @throws RemoteException
     */
    public void waitUntilTableItemExisted(BasicComponent basic, String itemText)
        throws RemoteException;

    /**
     * @throws RemoteException
     * @see Conditions#tableHasRows(org.eclipse.swtbot.swt.finder.widgets.SWTBotTable,
     *      int)
     */
    public void waitUntilTableHasRows(int row) throws RemoteException;

    /**
     * 
     * @param basic
     *            the object {@link BasicComponent}
     * @param itemText
     *            name of the table item, whose context menu you want to
     *            check,if it is enabled.
     * @param contextName
     *            , the name on the context menu.
     * @throws RemoteException
     */
    public void waitUntilContextMenuOfTableItemEnabled(BasicComponent basic,
        String itemText, String contextName) throws RemoteException;

    /**********************************************
     * 
     * actions with basic widget: {@link SWTBotTree}.
     * 
     **********************************************/

    public boolean existsTreeItemWithRegexs(String... regexs)
        throws RemoteException;

    public boolean existsTreeItemWithRegexsInView(String viewTitle,
        String... regexs) throws RemoteException;

    /**
     * 
     * @param tree
     *            a {@link SWTBotTree} with the specified <code>none</code>
     * @param regexNodes
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the regex array
     *            parameter.e.g.{"Foo-saros.*", "my.pkg.*", "myClass.*"}
     * @return the {@link SWTBotTree} with the given regexNodes
     * @throws WidgetNotFoundException
     *             If the item wasn't found.
     */

    public void selectTreeItemWithRegexs(String... regexNodes)
        throws RemoteException;

    public void selectTreeItemWithRegexsInView(String viewTitle,
        String... regexNodes) throws RemoteException;

    // public void selectTreeItemWithWaitingExpand(SWTBotTree tree,
    // String... labels) throws RemoteException;

    public void selectTreeItemWithWaitingExpand(String... nodes)
        throws RemoteException;

    public void selectTreeItemWithWaitingExpandInView(String viewTitle,
        String... nodes) throws RemoteException;

    public void selectTreeItem(String... nodes) throws RemoteException;

    public void selectTreeItemInView(String viewTitle, String... nodes)
        throws RemoteException;

    public List<String> getAllItemsInTreeNode(String... paths)
        throws RemoteException;

    public List<String> getAllItemsInTreeNodeInView(String viewTitle,
        String... nodes) throws RemoteException;

    public List<String> getAllItemsIntree() throws RemoteException;

    public List<String> getAllItemsIntree(SWTBotTree tree)
        throws RemoteException;

    public boolean existsTreeItemInTree(String itemText) throws RemoteException;

    public boolean existsTreeItemInTreeInView(String viewTitle, String itemText)
        throws RemoteException;

    public boolean existsTreeItemInTreeNode(String itemText, String... nodes)
        throws RemoteException;

    public boolean existsTreeItemInTreeNodeInView(String viewTitle,
        String itemText, String... nodes) throws RemoteException;

    public void waitUntilTreeItemInTreeExisted(String nodeName)
        throws RemoteException;

    public void waitUntilTreeItemInTreeExisted(final SWTBotTree tree,
        final String itemText) throws RemoteException;

    public void waitUntilTreeItemInTreeNodeExisted(String itemText,
        String... nodes) throws RemoteException;

    public void waitUntilTreeItemInTreeNodeExisted(SWTBotTreeItem treeItem,
        String nodeName) throws RemoteException;

    // public SWTBotTree getTreeInView(String viewName) throws RemoteException;

    // /**
    // * This method is a helper method and should not be exported by rmi. it is
    // * very helpful, when you want to click a context menu of a tree node in a
    // * view.e.g.
    // *
    // * in Package Explorer View: click context menu "open" of the class file
    // * "MyClass", in this case, you should pass the
    // * parameters("Package Explorer", "open", "Foo_Saros","src", "my.pkg",
    // * "MyClass.java".
    // *
    // * in Roster View: click context menu "rename.." of a user
    // * "lin@saros-con.imp.fu-berlin.de" in buddies. In this case, you shuld
    // pass
    // * the parameter ("Roster", "rename...", "Buddies",
    // * "lin@saros-con.imp.fu-berlin.de").
    // *
    // * 1. select the tree node that context menu you want to click.
    // *
    // * 2. then click the context menu.
    // *
    // * @param viewName
    // * the title on the view tab. e.g. Package Explorer view or
    // * Resource Explorer view
    // * @param nodes
    // * node path to expand. Attempts to expand all nodes along the
    // * path specified by the node array parameter.
    // * @throws RemoteException
    // */
    // public void clickContextMenuOfTreeItemInView(String viewName,
    // String context, String... nodes) throws RemoteException;

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
        String contextName) throws RemoteException;

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
    public void clickToolbarButtonWithRegexTooltipInView(String viewName,
        String buttonTooltip) throws RemoteException;

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
        String tooltip) throws RemoteException;

    /**
     * @param viewName
     *            the title on the view tab.
     * @return <tt>true</tt> if the specified table item exists.
     * 
     */
    // public boolean existsTableItemInView(String viewName, String itemName)
    // throws RemoteException;

    public boolean existsContextMenuOfTableItem(String itemName,
        String contextName) throws RemoteException;

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
        String itemName, String contextName) throws RemoteException;

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
        String contextName, String... nodes) throws RemoteException;

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
     * @throws RemoteException
     */
    public boolean existsSubMenuOfContextMenuOfTreeItemInView(String viewTitle,
        String[] nodes, String... contexts) throws RemoteException;

    /**
     * @return<tt>true</tt>, if there are some label texts existed in the given
     *                       view. You can only see the label texts when you are
     *                       not in a session.
     * 
     * @param viewName
     *            the title on the view tab.
     */
    public boolean existsLabelInView(String viewName) throws RemoteException;

    // /**
    // * This method is very useful, if you want to click a sub menu of a
    // context
    // * menu of the treeitem specified with the given nodes. e.g. the sub menu
    // * "Team->Commit...".
    // *
    // * @param viewTitle
    // * the title on the view tab.
    // * @param nodes
    // * node path to expand. Attempts to expand all nodes along the
    // * path specified by the node array parameter.e.g.{"Foo-saros",
    // * "my.pkg", "myClass.java"}
    // * @param contexts
    // * context menu of the given treeitem and his sub menus. e.g.
    // * {"Team", "Commit..."}
    // * @throws RemoteException
    // */
    // public void clickSubmenusOfContextMenuOfTreeItemInView(String viewTitle,
    // String[] nodes, String... contexts) throws RemoteException;

    /**
     * 
     * @param viewName
     *            the title on the view tab.
     * @param buttonTooltip
     *            the tooltip of the toolbar button which you want to know, if
     *            it is enabled.
     * @return <tt>true</tt>, if the given toolbar button is enabled.
     */
    public boolean isToolbarButtonInViewEnabled(String viewName,
        String buttonTooltip) throws RemoteException;

    /**
     * 
     * Waits until the {@link SarosSWTBotPreferences#SAROS_TIMEOUT} is reached
     * or the view is active.
     * 
     * @param viewName
     *            name of the view, which should be active.
     */
    public void waitUntilViewActive(String viewName) throws RemoteException;

    /**
     * open the given view specified with the viewId.
     * 
     * @param viewId
     *            the id of the view, which you want to open.
     */
    public void openViewById(final String viewId) throws RemoteException;

    /**
     * @param title
     *            the title on the view tab.
     * @return <tt>true</tt> if the specified view is open.
     * @see ViewPart#getTitlesOfOpenedViews()
     */
    public boolean isViewOpen(String title) throws RemoteException;

    /**
     * Set focus on the specified view. It should be only called if View is
     * open.
     * 
     * @param title
     *            the title on the view tab.
     * @see SWTBotView#setFocus()
     */
    public void setFocusOnViewByTitle(String title) throws RemoteException;

    /**
     * @param title
     *            the title on the view tab.
     * @return <tt>true</tt> if the specified view is active.
     */
    public boolean isViewActive(String title) throws RemoteException;

    /**
     * close the specified view
     * 
     * @param title
     *            the title on the view tab.
     */
    public void closeViewByTitle(String title) throws RemoteException;

    /**
     * close the given view specified with the viewId.
     * 
     * @param viewId
     *            the id of the view, which you want to close.
     */
    public void closeViewById(final String viewId) throws RemoteException;

    /**
     * @return the title list of all the views which are opened currently.
     * @see SWTWorkbenchBot#views()
     */
    public List<String> getTitlesOfOpenedViews() throws RemoteException;

    public boolean existsContextOfTreeItem(String contextName, String... nodes)
        throws RemoteException;

    public boolean existsContextOfTreeItemInView(String viewTitle,
        String contextName, String... nodes) throws RemoteException;

    public boolean existsSuMenuOfContextOfTreeItem(String[] contextNames,
        String... nodes) throws RemoteException;

    public boolean existsSubmenuOfContextOfTreeItemInView(String viewTitle,
        String[] contextNames, String... nodes) throws RemoteException;

    public boolean isContextOfTreeItemEnabled(String contextName,
        String... nodes) throws RemoteException;

    public boolean isContextOfTreeItemInViewEnabled(String viewTitle,
        String contextName, String... nodes) throws RemoteException;

    public boolean isSuMenuOfContextOfTreeItemEnabled(String[] contextNames,
        String... nodes) throws RemoteException;

    public boolean isSubmenuOfContextOfTreeItemInViewEnabled(String viewTitle,
        String[] contextNames, String... nodes) throws RemoteException;

    public void clickContextsOfTreeItem(String contextName, String... nodes)
        throws RemoteException;

    public void clickContextsOfTreeItemInView(String viewTitle,
        String contextName, String... nodes) throws RemoteException;

    public void clickSubMenuOfContextsOfTreeItem(String[] contextNames,
        String... nodes) throws RemoteException;

    public void clickSubMenuOfContextsOfTreeItemInView(String viewTitle,
        String[] contextNames, String... nodes) throws RemoteException;

}
