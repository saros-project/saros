package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench;

import java.rmi.RemoteException;

import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.results.StringResult;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;

import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.conditions.SarosConditions;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.EclipseComponent;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.SessionViewComponentImp;

public class ShellComponentImp extends EclipseComponent implements
    ShellComponent {
    private static transient ShellComponentImp self;

    public final static String TEXT_FIELD_TYPE_FILTER_TEXT = "type filter text";
    // Title of Shells
    protected final static String CONFIRM_DELETE = "Confirm Delete";

    /**
     * {@link SessionViewComponentImp} is a singleton, but inheritance is
     * possible.
     */
    public static ShellComponentImp getInstance() {
        if (self != null)
            return self;
        self = new ShellComponentImp();
        return self;
    }

    /**************************************************************
     * 
     * exported functions
     * 
     **************************************************************/

    /**********************************************
     * 
     * open/close/activate the view
     * 
     **********************************************/

    public boolean activateShellWithText(String title) throws RemoteException {
        waitUntilShellOpen(title);
        SWTBotShell[] shells = bot.shells();
        for (SWTBotShell shell : shells) {
            if (shell.getText().equals(title)) {
                log.debug("Shell \"" + title + "\" found.");
                if (!shell.isActive()) {
                    shell.activate();
                    // waitUntilShellActive(title);
                }
                return true;
            }
        }
        log.error("No shell found matching \"" + title + "\"!");
        return false;
    }

    public boolean activateShellWithRegexText(String matchText)
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

    public boolean activateShellWaitingUntilOpened(String title)
        throws RemoteException {
        if (!isShellOpen(title)) {
            waitUntilShellOpen(title);
        }
        return activateShellWithText(title);
    }

    public boolean isShellActive(String title) throws RemoteException {
        if (!isShellOpen(title))
            return false;
        try {

            SWTBotShell activeShell = bot.activeShell();

            String shellTitle = activeShell.getText();
            return shellTitle.equals(title);
        } catch (WidgetNotFoundException e) {
            return false;
        }
    }

    public boolean isShellOpen(String title) throws RemoteException {
        SWTBotShell[] shells = bot.shells();
        for (SWTBotShell shell : shells)
            if (shell.getText().equals(title))
                return true;
        return false;
    }

    public void closeShell(String title) throws RemoteException {
        bot.shell(title).close();
    }

    /**********************************************
     * 
     * get
     * 
     **********************************************/

    public String getTextOfActiveShell() throws RemoteException {
        final SWTBotShell activeShell = bot.activeShell();
        return activeShell == null ? null : activeShell.getText();
    }

    public String getErrorMessageInShell(final String title)
        throws RemoteException {
        final SWTBotShell shell = bot.shell(title);
        final String errorMessage = UIThreadRunnable
            .syncExec(new StringResult() {
                public String run() {
                    WizardDialog dialog = (WizardDialog) shell.widget.getData();
                    return dialog.getErrorMessage();
                }
            });
        if (errorMessage == null) {
            throw new WidgetNotFoundException("Could not find errorMessage!");
        }
        return errorMessage;
    }

    /**********************************************
     * 
     * exists the given widget in the shell
     * 
     **********************************************/
    public boolean existsTableItemInShell(String title, String label)
        throws RemoteException {
        activateShellWithText(title);
        return basicC.existsTableItem(label);
    }

    /**********************************************
     * 
     * waits until the widget...
     * 
     **********************************************/
    public void waitUntilShellOpen(String title) throws RemoteException {
        waitUntil(SarosConditions.isShellOpen(bot, title));
    }

    public void waitUntilShellActive(String title) throws RemoteException {
        waitUntil(SarosConditions.ShellActive(bot, title));
        // if (!isShellActive(title))
        // throw new RemoteException("Couldn't activate shell \"" + title
        // + "\"");
    }

    public void waitUntilShellClosed(String title) throws RemoteException {
        waitUntil(SarosConditions.isShellClosed(bot, title));
        bot.sleep(10);
    }

    public void waitLongUntilShellClosed(String title)
        throws RemoteException {
        waitLongUntil(SarosConditions.isShellClosed(bot, title));
    }

    /**********************************************
     * 
     * confirm shell
     * 
     **********************************************/
    /**
     * confirm a pop-up window.
     * 
     * @param title
     *            title of the window
     * @param buttonText
     *            text of the button
     * 
     */
    public void confirmShell(String title, String buttonText)
        throws RemoteException {
        if (activateShellWithText(title)) {
            bot.button(buttonText).click();
            bot.sleep(sleepTime);
        }
    }

    public void confirmShellWithTree(String title, String buttonText,
        String... nodes) throws RemoteException {
        bot.shell(title).activate();
        basicC.selectTreeItem(nodes);
        basicC.waitUntilButtonEnabled(buttonText);
        bot.button(buttonText).click();
    }

    public void confirmShellWithTreeWithWaitingExpand(String title,
        String buttonText, String... nodes) throws RemoteException {
        SWTBotTree tree = bot.tree();
        log.info("allItems " + tree.getAllItems().length);
        basicC.selectTreeItemWithWaitingExpand(tree, nodes);
        bot.button(buttonText).click();
    }

    public void confirmWindowWithCheckBox(String title, String buttonText,
        boolean isChecked) throws RemoteException {
        activateShellWithText(title);
        if (isChecked)
            bot.checkBox().click();
        bot.button(buttonText).click();
        bot.sleep(sleepTime);
    }

    public void confirmWindowWithCheckBoxs(String title, String buttonText,
        String... itemNames) throws RemoteException {
        waitUntilShellActive(title);
        for (String itemName : itemNames) {
            basicC.selectCheckBoxInTable(itemName);
        }
        basicC.waitUntilButtonEnabled(buttonText);
        bot.button(buttonText).click();
        // waitUntilShellCloses(shellName);
    }

    public void confirmShellWithTable(String title, String itemName,
        String buttonText) throws RemoteException {
        // waitUntilShellActive(shellName);
        try {
            bot.shell(title).bot().table().select(itemName);
            basicC.waitUntilButtonEnabled(buttonText);
            bot.button(buttonText).click();
            // waitUntilShellCloses(shellName);
        } catch (WidgetNotFoundException e) {
            log.error("tableItem" + itemName + "can not be fund!");
        }
    }

    public void confirmShellWithTreeWithFilterText(String title,
        String rootOfTreeNode, String teeNode, String buttonText)
        throws RemoteException {
        // waitUntilShellActive(shellName);
        bot.text(TEXT_FIELD_TYPE_FILTER_TEXT).setText(teeNode);
        basicC.waitUntilTreeItemInTreeExisted(rootOfTreeNode);
        SWTBotTreeItem treeItem = bot.tree(0).getTreeItem(rootOfTreeNode);
        basicC.waitUntilTreeItemInTreeNodeExisted(treeItem, teeNode);
        treeItem.getNode(teeNode).select();
        basicC.waitUntilButtonEnabled(buttonText);
        bot.button(buttonText).click();
        // waitUntilShellCloses(shellName);
    }

    public void confirmShellDelete(String buttonName) throws RemoteException {
        if (!activateShellWithText(CONFIRM_DELETE))
            waitUntilShellActive(CONFIRM_DELETE);
        confirmShell(CONFIRM_DELETE, buttonName);
    }

}
