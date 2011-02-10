package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.basicWidgets;

import java.rmi.RemoteException;
import java.util.Map;

import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.results.StringResult;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.conditions.SarosConditions;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.EclipseComponentImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.views.sarosViews.SessionViewImp;

public class ShellImp extends EclipseComponentImp implements Shell {
    private static transient ShellImp self;

    public final static String TEXT_FIELD_TYPE_FILTER_TEXT = "type filter text";
    // Title of Shells
    protected final static String CONFIRM_DELETE = "Confirm Delete";

    /**
     * {@link SessionViewImp} is a singleton, but inheritance is possible.
     */
    public static ShellImp getInstance() {
        if (self != null)
            return self;
        self = new ShellImp();
        return self;
    }

    /**************************************************************
     * 
     * exported functions
     * 
     **************************************************************/

    /**********************************************
     * 
     * actions
     * 
     **********************************************/

    public boolean activateShell(String title) throws RemoteException {
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

    public boolean activateShellAndWait(String title) throws RemoteException {
        if (!activateShell(title))
            waitUntilShellOpen(title);
        return activateShell(title);

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

    public boolean activateShellWithWaitingOpen(String title)
        throws RemoteException {
        if (!isShellOpen(title)) {
            waitUntilShellOpen(title);
        }
        return activateShell(title);
    }

    public void closeShell(String title) throws RemoteException {
        bot.shell(title).close();
    }

    public void confirmShell(String title, String buttonText)
        throws RemoteException {
        if (activateShell(title)) {
            bot.button(buttonText).click();
            bot.sleep(sleepTime);
        }
    }

    public void confirmShellAndWait(String title, String buttonText)
        throws RemoteException {
        activateShellAndWait(title);
        confirmShell(title, buttonText);
    }

    public void confirmShellWithTree(String title, String buttonText,
        String... nodes) throws RemoteException {
        bot.shell(title).activate();
        treeW.selectTreeItem(nodes);
        buttonW.waitUntilButtonEnabled(buttonText);
        bot.button(buttonText).click();
    }

    public void confirmShellWithTextField(String title, String textLabel,
        String text, String buttonText) throws RemoteException {
        activateShell(title);
        textW.setTextInTextWithLabel(textLabel, text);
        buttonW.waitUntilButtonEnabled(buttonText);
        buttonW.clickButton(buttonText);
    }

    public void confirmShellWithTextFieldAndWait(String title,
        Map<String, String> labelsAndTexts, String buttonText)
        throws RemoteException {
        activateShellAndWait(title);
        for (String label : labelsAndTexts.keySet()) {
            String text = labelsAndTexts.get(label);
            textW.setTextInTextWithLabel(text, label);
        }
        buttonW.waitUntilButtonEnabled(buttonText);
        buttonW.clickButton(buttonText);

    }

    public void confirmShellWithTreeWithWaitingExpand(String title,
        String buttonText, String... nodes) throws RemoteException {
        SWTBotTree tree = bot.tree();
        log.info("allItems " + tree.getAllItems().length);
        treeW.selectTreeItemWithWaitingExpand(tree, nodes);
        bot.button(buttonText).click();
    }

    public void confirmWindowWithCheckBox(String title, String buttonText,
        boolean isChecked) throws RemoteException {
        activateShell(title);
        if (isChecked)
            bot.checkBox().click();
        bot.button(buttonText).click();
        bot.sleep(sleepTime);
    }

    public void confirmWindowWithCheckBoxs(String title, String buttonText,
        String... itemNames) throws RemoteException {
        waitUntilShellActive(title);
        for (String itemName : itemNames) {
            tableW.selectCheckBoxInTable(itemName);
        }
        buttonW.waitUntilButtonEnabled(buttonText);
        bot.button(buttonText).click();
        // waitUntilShellCloses(shellName);
    }

    public void confirmShellWithTable(String title, String itemName,
        String buttonText) throws RemoteException {
        // waitUntilShellActive(shellName);
        try {
            bot.shell(title).bot().table().select(itemName);
            buttonW.waitUntilButtonEnabled(buttonText);
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
        treeW.waitUntilIsTreeItemInTreeExisted(rootOfTreeNode);
        SWTBotTreeItem treeItem = bot.tree(0).getTreeItem(rootOfTreeNode);
        treeW.waitUntilIsSubItemInTreeItemExisted(treeItem, teeNode);
        treeItem.getNode(teeNode).select();
        buttonW.waitUntilButtonEnabled(buttonText);
        bot.button(buttonText).click();
        // waitUntilShellCloses(shellName);
    }

    public void confirmShellDelete(String buttonName) throws RemoteException {
        if (!activateShell(CONFIRM_DELETE))
            waitUntilShellActive(CONFIRM_DELETE);
        confirmShell(CONFIRM_DELETE, buttonName);
    }

    /**********************************************
     * 
     * states
     * 
     **********************************************/
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

    public boolean existsTableItemInShell(String title, String label)
        throws RemoteException {
        activateShell(title);
        return tableW.existsTableItem(label);
    }

    /**********************************************
     * 
     * waits until
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

    public void waitsUntilIsShellClosed(String title) throws RemoteException {
        waitUntil(SarosConditions.isShellClosed(bot, title));
        bot.sleep(10);
    }

    public void waitShortUntilIsShellClosed(String title)
        throws RemoteException {
        waitShortUntil(SarosConditions.isShellClosed(bot, title));
    }

    public void waitLongUntilShellClosed(String title) throws RemoteException {
        waitLongUntil(SarosConditions.isShellClosed(bot, title));
    }

}
