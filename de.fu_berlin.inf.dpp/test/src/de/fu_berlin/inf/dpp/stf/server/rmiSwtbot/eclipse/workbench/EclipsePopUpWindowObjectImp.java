package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench;

import static org.eclipse.swtbot.swt.finder.waits.Conditions.shellCloses;

import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;

import de.fu_berlin.inf.dpp.stf.server.SarosConstant;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.conditions.SarosConditions;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.EclipseObject;

public class EclipsePopUpWindowObjectImp extends EclipseObject implements
    EclipsePopUpWindowObject {

    public void waitUntilShellActive(String title) throws RemoteException {
        waitUntil(SarosConditions.ShellActive(bot, title));
        // if (!isShellActive(title))
        // throw new RemoteException("Couldn't activate shell \"" + title
        // + "\"");
    }

    public void waitUntilShellCloses(SWTBotShell shell) throws RemoteException {
        waitUntil(shellCloses(shell));
        bot.sleep(10);
    }

    public void waitUntilShellCloses(String shellText) throws RemoteException {
        waitUntil(SarosConditions.isShellClosed(bot, shellText));
        bot.sleep(10);
    }

    public void closeShell(String title) throws RemoteException {
        bot.shell(title).close();
    }

    public boolean isShellOpen(String title) throws RemoteException {
        SWTBotShell[] shells = bot.shells();
        for (SWTBotShell shell : shells)
            if (shell.getText().equals(title))
                return true;
        return false;
    }

    public boolean isShellActive(String title) throws RemoteException {
        if (!isShellOpen(title))
            return false;
        SWTBotShell activeShell = bot.activeShell();
        String shellTitle = activeShell.getText();
        return shellTitle.equals(title);
    }

    public boolean activateShellWithMatchText(String matchText)
        throws RemoteException {
        SWTBotShell[] shells = bot.shells();
        for (SWTBotShell shell : shells) {
            if (shell.getText().matches(matchText)) {
                log.debug("shell found matching \"" + matchText + "\"");
                if (!shell.isActive()) {
                    shell.activate();
                }
                return shell.isActive();
            }
        }
        final String message = "No shell found matching \"" + matchText + "\"!";
        log.error(message);
        throw new RemoteException(message);
    }

    /**
     * confirm a pop-up window.
     * 
     * @param title
     *            title of the window
     * @param buttonText
     *            text of the button
     * @throws RemoteException
     */
    public void confirmWindow(String title, String buttonText)
        throws RemoteException {
        // waitUntilShellActive(title);
        if (windowO.activateShellWithText(title)) {
            bot.button(buttonText).click();
            bot.sleep(sleepTime);
        }
    }

    /**
     * confirm a pop-up window with a checkbox.
     * 
     * @param title
     *            title of the window
     * @param buttonText
     *            text of the button
     * @param isChecked
     *            if the checkbox selected or not.
     * @throws RemoteException
     */
    public void confirmWindowWithCheckBox(String title, String buttonText,
        boolean isChecked) throws RemoteException {
        windowO.activateShellWithText(title);
        if (isChecked)
            bot.checkBox().click();
        bot.button(buttonText).click();
        bot.sleep(sleepTime);
    }

    /**
     * confirm a pop-up window with more than one checkbox.
     * 
     * @param title
     *            title of the window
     * @param buttonText
     *            text of the button
     * @param itemNames
     *            the labels of the checkboxs, which you want to select.
     * @throws RemoteException
     */
    public void confirmWindowWithCheckBox(String title, String buttonText,
        String... itemNames) throws RemoteException {
        windowO.waitUntilShellActive(title);
        for (String itemName : itemNames) {
            tableO.selectCheckBoxInTable(itemName);
        }
        basicO.waitUntilButtonIsEnabled(buttonText);
        bot.button(buttonText).click();
        // waitUntilShellCloses(shellName);
    }

    /**
     * confirm a pop-up window with a table. You should first select a table
     * item and then confirm with button.
     * 
     * @param title
     *            title of the window
     * @param buttonText
     *            text of the button
     * @param itemName
     *            the name of the table item, which you want to select.
     * @throws RemoteException
     */
    public void confirmWindowWithTable(String title, String itemName,
        String buttonText) throws RemoteException {
        // waitUntilShellActive(shellName);
        try {
            bot.table().select(itemName);
            basicO.waitUntilButtonIsEnabled(buttonText);
            bot.button(buttonText).click();
            // waitUntilShellCloses(shellName);
        } catch (WidgetNotFoundException e) {
            log.error("tableItem" + itemName + "can not be fund!");
        }
    }

    // /**
    // * confirm a pop-up window with a tree. You should first select a tree
    // node
    // * and then confirm with button.
    // *
    // * @param title
    // * title of the window
    // * @param buttonText
    // * text of the button
    // * @param nodes
    // * node path to expand. Attempts to expand all nodes along the
    // * path specified by the node array parameter.
    // * @throws RemoteException
    // */
    // public void confirmWindowWithTree(String title, String buttonText,
    // String... nodes) throws RemoteException {
    // // waitUntilShellActive(shellName);
    // SWTBotTree tree = delegate.tree();
    // log.info("allItems " + tree.getAllItems().length);
    // treeObject.selectTreeWithLabelsWithWaitungExpand(tree, nodes);
    // basicObject.waitUntilButtonEnabled(buttonText);
    // delegate.button(buttonText).click();
    // // waitUntilShellCloses(shellName);
    // }

    /**
     * confirm a pop-up window with a tree using filter text. You should first
     * input a filter text in the text field and then select a tree node,
     * confirm with button.
     * 
     * @param title
     *            title of the window
     * @param buttonText
     *            text of the button
     * @param teeNode
     *            tree node, which you want to select.
     * @param rootOfTreeNode
     *            root of the tree node.
     * @throws RemoteException
     */
    public void confirmWindowWithTreeWithFilterText(String title,
        String rootOfTreeNode, String teeNode, String buttonText)
        throws RemoteException {
        // waitUntilShellActive(shellName);
        bot.text(SarosConstant.TEXT_FIELD_TYPE_FILTER_TEXT).setText(teeNode);
        treeO.waitUntilTreeExisted(bot.tree(), rootOfTreeNode);
        SWTBotTreeItem treeItem = bot.tree(0).getTreeItem(rootOfTreeNode);
        treeO.waitUntilTreeNodeExisted(treeItem, teeNode);
        treeItem.getNode(teeNode).select();
        basicO.waitUntilButtonIsEnabled(buttonText);
        bot.button(buttonText).click();
        // waitUntilShellCloses(shellName);
    }

    public String getSecondLabelOfProblemOccurredWindow()
        throws RemoteException {
        SWTBotShell activeShell = bot.activeShell();
        return activeShell.bot().label(2).getText();
    }

}