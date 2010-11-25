package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noExportedObjects;

import static org.eclipse.swtbot.swt.finder.waits.Conditions.shellCloses;

import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;

import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.conditions.SarosConditions;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.EclipseComponent;

public class WindowPart extends EclipseComponent {

    public final static String TEXT_FIELD_TYPE_FILTER_TEXT = "type filter text";

    public String getCurrentActiveShell() {
        final SWTBotShell activeShell = bot.activeShell();
        return activeShell == null ? null : activeShell.getText();
    }

    public boolean isTableItemInWindowExist(String title, String label) {
        activateShellWithText(title);
        return tablePart.existTableItem(label);
    }

    public boolean activateShellWithText(String title) {
        SWTBotShell[] shells = bot.shells();
        for (SWTBotShell shell : shells) {
            if (shell.getText().equals(title)) {
                log.debug("Shell \"" + title + "\" found.");
                if (!shell.isActive()) {
                    shell.activate();
                }
                return true;
            }
        }
        log.error("No shell found matching \"" + title + "\"!");
        return false;
    }

    public void waitUntilShellCloses(SWTBotShell shell) {
        waitUntil(shellCloses(shell));
        bot.sleep(10);
    }

    public void waitUntilShellCloses(String shellText) {
        waitUntil(SarosConditions.isShellClosed(bot, shellText));
        bot.sleep(10);
    }

    public void waitUntilShellActive(String title) {
        waitUntil(SarosConditions.ShellActive(bot, title));
        // if (!isShellActive(title))
        // throw new RemoteException("Couldn't activate shell \"" + title
        // + "\"");
    }

    public void waitUntilShellOpen(String title) {
        waitUntil(SarosConditions.isShellOpen(bot, title));
    }

    public void waitUntilShellClosed(String shellText) {
        waitUntil(SarosConditions.isShellClosed(bot, shellText));
        bot.sleep(10);
    }

    /**
     * confirm a pop-up window with a tree. You should first select a tree node
     * and then confirm with button.
     * 
     * @param title
     *            title of the window
     * @param buttonText
     *            text of the button
     * @param nodes
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the node array parameter.
     * 
     */
    public void confirmWindowWithTree(String title, String buttonText,
        String... nodes) {
        bot.shell(title).activate();
        SWTBotTree tree = bot.tree();
        log.info("allItems " + tree.getAllItems().length);
        tree.expandNode(nodes).select();
        basicPart.waitUntilButtonIsEnabled(buttonText);
        bot.button(buttonText).click();
    }

    public void confirmWindowWithTreeWithWaitingExpand(String title,
        String buttonText, String... nodes) {
        SWTBotTree tree = bot.tree();
        log.info("allItems " + tree.getAllItems().length);
        treePart.selectTreeWithLabelsWithWaitungExpand(tree, nodes);
        bot.button(buttonText).click();

    }

    /**
     * confirm a pop-up window.
     * 
     * @param title
     *            title of the window
     * @param buttonText
     *            text of the button
     * 
     */
    public void confirmWindow(String title, String buttonText) {
        // waitUntilShellActive(title);
        if (windowPart.activateShellWithText(title)) {
            bot.button(buttonText).click();
            bot.sleep(sleepTime);
        }
    }

    public boolean isShellOpen(String title) {
        SWTBotShell[] shells = bot.shells();
        for (SWTBotShell shell : shells)
            if (shell.getText().equals(title))
                return true;
        return false;
    }

    public boolean isShellActive(String title) {
        if (!isShellOpen(title))
            return false;
        SWTBotShell activeShell = bot.activeShell();
        String shellTitle = activeShell.getText();
        return shellTitle.equals(title);
    }

    public void closeShell(String title) {
        bot.shell(title).close();
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
        windowPart.activateShellWithText(title);
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
     * 
     */
    public void confirmWindowWithCheckBox(String title, String buttonText,
        String... itemNames) {
        windowPart.waitUntilShellActive(title);
        for (String itemName : itemNames) {
            tablePart.selectCheckBoxInTable(itemName);
        }
        basicPart.waitUntilButtonIsEnabled(buttonText);
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
            basicPart.waitUntilButtonIsEnabled(buttonText);
            bot.button(buttonText).click();
            // waitUntilShellCloses(shellName);
        } catch (WidgetNotFoundException e) {
            log.error("tableItem" + itemName + "can not be fund!");
        }
    }

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
        bot.text(TEXT_FIELD_TYPE_FILTER_TEXT).setText(teeNode);
        treePart.waitUntilTreeExisted(bot.tree(), rootOfTreeNode);
        SWTBotTreeItem treeItem = bot.tree(0).getTreeItem(rootOfTreeNode);
        treePart.waitUntilTreeNodeExisted(treeItem, teeNode);
        treeItem.getNode(teeNode).select();
        basicPart.waitUntilButtonIsEnabled(buttonText);
        bot.button(buttonText).click();
        // waitUntilShellCloses(shellName);
    }

    @Override
    protected void precondition() throws RemoteException {
        // TODO Auto-generated method stub
    }

    // Title of Shells
    protected final static String CONFIRM_DELETE = "Confirm Delete";

    public void confirmDeleteWindow(String buttonName) {
        windowPart.waitUntilShellActive(CONFIRM_DELETE);
        windowPart.confirmWindow(CONFIRM_DELETE, buttonName);
    }

}
