package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.basicWidgets;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotLabel;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.ui.part.ViewPart;

import de.fu_berlin.inf.dpp.stf.client.Tester;
import de.fu_berlin.inf.dpp.stf.client.testProject.helpers.TestPattern;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.conditions.SarosSWTBotPreferences;

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
public interface BasicWidgets extends Remote {

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
    public String getTextOfLabel() throws RemoteException;

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
     * actions with basic widget: {@link SWTBotTree}.
     * 
     **********************************************/

    /***************** select tree item ****************** */

    /**
     * Selects the treeItem matching the given nodes in the tree: bot.tree().
     * This method is suitable for Shell widget.
     * 
     * @param nodes
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the node array parameter.
     * @throws RemoteException
     */
    public void selectTreeItem(String... nodes) throws RemoteException;

    /**
     * Selects the treeItem matching the given nodes in the tree:
     * bot.getViewTitle(viewTitle).bot().tree(). This method is suitable for
     * view widget.
     * 
     * @param viewTitle
     *            the title on the view tab.
     * @param nodes
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the node array parameter.
     * @throws RemoteException
     */
    public void selectTreeItemInView(String viewTitle, String... nodes)
        throws RemoteException;

    /***************** select tree item with regexs ******************/

    /**
     * Selects the treeItem matching the given Regexnodes in the tree:
     * bot.tree(). This method is suitable for shell widget.
     * 
     * @param regexNodes
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the regex array
     *            parameter.e.g.{"Foo-saros.*", "my.pkg.*", "myClass.*"}
     * 
     * @throws WidgetNotFoundException
     *             If the item wasn't found.
     */
    public void selectTreeItemWithRegexs(String... regexNodes)
        throws RemoteException;

    /**
     * Selects the treeItem matching the given Regexnodes in the tree:
     * bot.getViewTitle(viewTitle).bot().tree(). This method is suitable for
     * view widget.
     * 
     * @param viewTitle
     *            the title on the view tab.
     * @param regexNodes
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the regex array
     *            parameter.e.g.{"Foo-saros.*", "my.pkg.*", "myClass.*"}
     * @throws RemoteException
     */
    public void selectTreeItemWithRegexsInView(String viewTitle,
        String... regexNodes) throws RemoteException;

    /***************** select tree item with waiting expand ****************** */
    /**
     * Selects the treeItem matching the given nodes in the tree: bot.tree()
     * with waiting until the parentTreeItem is expanded. This method is
     * suitable for shell widget.
     * 
     * @param nodes
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the node array parameter.
     * @throws RemoteException
     */
    public void selectTreeItemWithWaitingExpand(String... nodes)
        throws RemoteException;;

    /**
     * Selects the treeItem matching the given nodes in the tree:
     * bot.getViewTitle(viewTitle).bot().tree() with waiting until the
     * parentTreeItem is expanded. This method is suitable for view widget.
     * 
     * @param viewTitle
     *            the title on the view tab.
     * @param nodes
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the node array parameter.
     * @throws RemoteException
     */
    public void selectTreeItemWithWaitingExpandInView(String viewTitle,
        String... nodes) throws RemoteException;

    /***************** exist tree item with regexs ****************** */

    /**
     * This method is suitable for shell widget.
     * 
     * @param regexNodes
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the regex array
     *            parameter.e.g.{"Foo-saros.*", "my.pkg.*", "myClass.*"}
     * @return<tt>true</tt>, if the treeItem specified with the given regexNodes
     *                       exists.
     * @throws RemoteException
     */
    public boolean existsTreeItemWithRegexs(String... regexNodes)
        throws RemoteException;

    /**
     * This method is suitable for view widget.
     * 
     * @param viewTitle
     *            the title on the view tab.
     * @param regexNodes
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the regex array
     *            parameter.e.g.{"Foo-saros.*", "my.pkg.*", "myClass.*"}
     * @return<tt>true</tt>, if the treeItem specified with the given regexNodes
     *                       exists.
     * @throws RemoteException
     */
    public boolean existsTreeItemWithRegexsInView(String viewTitle,
        String... regexNodes) throws RemoteException;

    /***************** exists tree item ****************** */
    /**
     * This method is suitable for shell widget.
     * 
     * @param itemText
     *            name of the tree item.
     * @return<tt>true</tt>, if the treeItem in the bot.tree() exists.
     * @throws RemoteException
     */
    public boolean existsTreeItemInTree(String itemText) throws RemoteException;

    /**
     * This method is suitable for view widget.
     * 
     * @param viewTitle
     *            the title on the view tab.
     * @param itemText
     *            name of the tree item.
     * @return<tt>true</tt>, if the treeItem in the
     *                       bot.viewByTitle(viewTitle).bot().tree() exists.
     * @throws RemoteException
     */
    public boolean existsTreeItemInTreeInView(String viewTitle, String itemText)
        throws RemoteException;

    /**
     * This method is suitable for shell widget.
     * 
     * @param itemText
     *            name of the tree item.
     * @param nodes
     *            parent node path of the treeItem to expand. Attempts to expand
     *            all nodes along the path specified by the node array
     *            parameter.
     * @return<tt>true</tt>, if the treeItem in the treeNode specified with the
     *                       given nodes exists.
     * @throws RemoteException
     */
    public boolean existsTreeItemInTreeNode(String itemText, String... nodes)
        throws RemoteException;

    /**
     * This method is suitable for view widget.
     * 
     * @param viewTitle
     *            the title on the view tab.
     * @param itemText
     *            name of the tree item.
     * @param nodes
     *            parent node path of the treeItem to expand. Attempts to expand
     *            all nodes along the path specified by the node array
     *            parameter.
     * @return<tt>true</tt>, if the treeItem in the treeNode specified with the
     *                       given nodes exists.
     * @throws RemoteException
     */
    public boolean existsTreeItemInTreeNodeInView(String viewTitle,
        String itemText, String... nodes) throws RemoteException;

    /**
     * This method is suitable for shell widget.
     * 
     * @param contextName
     *            the name on the context menu.
     * @param nodes
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the node array parameter.
     * @return<tt>true</tt>, if the contextMenu of the selected TreeItem exists.
     * @throws RemoteException
     */
    public boolean existsContextOfTreeItem(String contextName, String... nodes)
        throws RemoteException;

    /**
     * This method is suitable for view widget.
     * 
     * @param viewTitle
     *            the title on the view tab.
     * @param contextName
     *            the name on the context menu.
     * @param nodes
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the node array parameter.
     * @return<tt>true</tt>, if the contextMenu of the selected TreeItem exists.
     * @throws RemoteException
     */
    public boolean existsContextOfTreeItemInView(String viewTitle,
        String contextName, String... nodes) throws RemoteException;

    /**
     * This method is suitable for shell widget.
     * 
     * @param contextNames
     *            all menus'name along the path specified by the contentNames
     *            array parameter.
     * @param nodes
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the node array parameter.
     * @return<tt>true</tt>, if the subMenu of the selected TreeItem exists.
     * @throws RemoteException
     */
    public boolean existsSuMenuOfContextOfTreeItem(String[] contextNames,
        String... nodes) throws RemoteException;

    /**
     * This method is suitable for view widget.
     * 
     * @param viewTitle
     *            the title on the view tab.
     * @param contextNames
     *            all menus'name along the path specified by the contentNames
     *            array parameter.
     * @param nodes
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the node array parameter.
     * @return<tt>true</tt>, if the subMenu of the selected TreeItem exists.
     * @throws RemoteException
     */
    public boolean existsSubmenuOfContextOfTreeItemInView(String viewTitle,
        String[] contextNames, String... nodes) throws RemoteException;

    /***************** is context of tree item enabled****************** */
    /**
     * This method is suitable for sehll widget.
     * 
     * @param contextName
     *            the name on the context menu.
     * @param nodes
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the node array parameter.
     * @return<tt>true</tt>, if the contextMenu of the selected TreeItem is
     *                       enabled.
     * @throws RemoteException
     */
    public boolean isContextOfTreeItemEnabled(String contextName,
        String... nodes) throws RemoteException;

    /**
     * This method is suitable for view widget.
     * 
     * @param viewTitle
     *            the title on the view tab.
     * @param contextName
     *            the name on the context menu.
     * @param nodes
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the node array parameter.
     * @return<tt>true</tt>, if the contextMenu of the selected TreeItem is
     *                       enabled.
     * @throws RemoteException
     */
    public boolean isContextOfTreeItemInViewEnabled(String viewTitle,
        String contextName, String... nodes) throws RemoteException;

    /**
     * This method is suitable for shell widget.
     * 
     * @param contextNames
     *            all menus'name along the path specified by the contentNames
     *            array parameter.
     * @param nodes
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the node array parameter.
     * @return<tt>true</tt>, if the subMenu of the selected TreeItem is enabled.
     * @throws RemoteException
     */
    public boolean isSuMenuOfContextOfTreeItemEnabled(String[] contextNames,
        String... nodes) throws RemoteException;

    /**
     * This method is suitable for view widget.
     * 
     * @param viewTitle
     *            the title on the view tab.
     * @param contextNames
     *            all menus'name along the path specified by the contentNames
     *            array parameter.
     * @param nodes
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the node array parameter.
     * @return<tt>true</tt>, if the subMenu of the selected TreeItem is enabled.
     * @throws RemoteException
     */
    public boolean isSubmenuOfContextOfTreeItemInViewEnabled(String viewTitle,
        String[] contextNames, String... nodes) throws RemoteException;

    /***************** click context of tree item ****************** */
    /**
     * 
     * This method is suitable for shell widget.
     * 
     * Clicks the contextMenu of the selected TreeItem.
     * 
     * @param contextName
     *            the name on the context menu.
     * @param nodes
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the node array parameter.
     * @throws RemoteException
     */
    public void clickContextsOfTreeItem(String contextName, String... nodes)
        throws RemoteException;

    /**
     * 
     * This method is suitable for view widget.
     * 
     * Clicks the contextMenu of the selected TreeItem.
     * 
     * @param viewTitle
     *            the title on the view tab.
     * @param contextName
     *            the name on the context menu.
     * @param nodes
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the node array parameter.
     * @throws RemoteException
     */
    public void clickContextsOfTreeItemInView(String viewTitle,
        String contextName, String... nodes) throws RemoteException;

    /**
     * 
     * This method is suitable for shell widget.
     * 
     * Clicks the subMenu of the selected TreeItem.
     * 
     * @param contextNames
     *            all menus'name along the path specified by the contentNames
     *            array parameter.
     * @param nodes
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the node array parameter.
     * @throws RemoteException
     */
    public void clickSubMenuOfContextsOfTreeItem(String[] contextNames,
        String... nodes) throws RemoteException;

    /**
     * 
     * This method is suitable for view widget.
     * 
     * Clicks the subMenu of the selected TreeItem.
     * 
     * @param viewTitle
     *            the title on the view tab.
     * @param contextNames
     *            all menus'name along the path specified by the contentNames
     *            array parameter.
     * @param nodes
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the node array parameter.
     * @throws RemoteException
     */
    public void clickSubMenuOfContextsOfTreeItemInView(String viewTitle,
        String[] contextNames, String... nodes) throws RemoteException;

    /***************** get allItems in treeNode ****************** */

    /**
     * This method is suitable for shell widget.
     * 
     * @param nodes
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the node array parameter.
     * @return all the treeItem'name of the given TreeNode.
     * @throws RemoteException
     */
    public List<String> getAllItemsInTreeNode(String... nodes)
        throws RemoteException;

    /**
     * This method is suitable for view widget.
     * 
     * @param viewTitle
     *            the title on the view tab.
     * @param nodes
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the node array parameter.
     * @return all the treeItem'name of the given TreeNode.
     * @throws RemoteException
     */
    public List<String> getAllItemsInTreeNodeInView(String viewTitle,
        String... nodes) throws RemoteException;

    /***************** get allItems in tree ****************** */

    /**
     * This method is suitable for shell widget.
     * 
     * @return all the treeItem'name of the bot.tree().
     * @throws RemoteException
     */
    public List<String> getAllItemsIntree() throws RemoteException;

    /**
     * This method is suitable for view widget.
     * 
     * @param viewTitle
     *            the title on the view tab.
     * @return all the treeItem'name of the
     *         bot.getViewByTitle(viewTitle).bot().tree().
     * @throws RemoteException
     */
    public List<String> getAllItemsIntreeInView(String viewTitle)
        throws RemoteException;

    public void waitUntilTreeItemInTreeExisted(String nodeName)
        throws RemoteException;

    public void waitUntilTreeItemInTreeExisted(final SWTBotTree tree,
        final String itemText) throws RemoteException;

    public void waitUntilTreeItemInTreeNodeExisted(String itemText,
        String... nodes) throws RemoteException;

    public void waitUntilTreeItemInTreeNodeExisted(SWTBotTreeItem treeItem,
        String nodeName) throws RemoteException;

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
     * @return<tt>true</tt>, if there are some label texts existed in the given
     *                       view. You can only see the label texts when you are
     *                       not in a session.
     * 
     * @param viewName
     *            the title on the view tab.
     */
    public boolean existsLabelInView(String viewName) throws RemoteException;

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

    /**********************************************
     * 
     * actions on the widget: {@link SWTBotMenu}.
     * 
     **********************************************/
    /**
     * clicks the main menus with the passed texts.
     * 
     * @param texts
     *            title of the menus, example: Window -> Show View -> Other...
     * 
     * @throws RemoteException
     */
    public void clickMenuWithTexts(String... texts) throws RemoteException;
}
