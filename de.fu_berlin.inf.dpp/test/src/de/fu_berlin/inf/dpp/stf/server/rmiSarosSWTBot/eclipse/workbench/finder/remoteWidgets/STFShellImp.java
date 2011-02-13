package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.finder.remoteWidgets;

import java.rmi.RemoteException;
import java.util.Map;

import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.results.StringResult;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.conditions.SarosConditions;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.finder.STFBot;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.finder.STFBotImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.sarosFinder.remoteComponents.EclipseComponentImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.sarosFinder.remoteComponents.views.sarosViews.SessionViewImp;

public class STFShellImp extends EclipseComponentImp implements STFBotShell {
    private static transient STFShellImp self;

    public final static String TEXT_FIELD_TYPE_FILTER_TEXT = "type filter text";

    private SWTBotShell swtBotShell;
    private String shellTitle;

    // Title of Shells

    /**
     * {@link SessionViewImp} is a singleton, but inheritance is possible.
     */
    public static STFShellImp getInstance() {
        if (self != null)
            return self;
        self = new STFShellImp();
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

    public boolean activate() throws RemoteException {
        swtBotShell.activate();
        return true;
        // SWTBotShell[] shells = bot.shells();
        // for (SWTBotShell shell : shells) {
        // if (shell.getText().equals(shellTitle)) {
        // log.debug("STFBotShell \"" + shellTitle + "\" found.");
        // if (!shell.isActive()) {
        // shell.activate();
        // // waitUntilShellActive(title);
        // }
        // return true;
        // }
        // }
        // log.error("No shell found matching \"" + title + "\"!");
        // return false;
    }

    public boolean activateAndWait() throws RemoteException {
        if (!activate())
            bot().waitUntilShellOpen(shellTitle);
        return activate();

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

    public void closeShell() throws RemoteException {
        swtBotShell.close();
    }

    public void confirm(String buttonText) throws RemoteException {
        if (activate()) {
            bot_().button(buttonText).click();
        }
    }

    public void confirmShellAndWait(String buttonText) throws RemoteException {
        activateAndWait();
        confirm(buttonText);
    }

    public void confirmShellWithTree(String buttonText, String... nodes)
        throws RemoteException {
        activate();
        bot_().tree().selectTreeItem(nodes);
        bot_().button(buttonText).waitUntilIsEnabled();
        bot_().button(buttonText).click();
    }

    public void confirmShellWithTextField(String textLabel, String text,
        String buttonText) throws RemoteException {
        activate();
        stfText.setTextInTextWithLabel(textLabel, text);
        bot_().button(buttonText).waitUntilIsEnabled();
        bot_().button(buttonText).click();
    }

    public void confirmWithTextFieldAndWait(Map<String, String> labelsAndTexts,
        String buttonText) throws RemoteException {
        activateAndWait();
        for (String label : labelsAndTexts.keySet()) {
            String text = labelsAndTexts.get(label);
            stfText.setTextInTextWithLabel(text, label);
        }
        bot_().button(buttonText).waitUntilIsEnabled();
        bot_().button(buttonText).click();

    }

    public void confirmShellWithTreeWithWaitingExpand(String buttonText,
        String... nodes) throws RemoteException {
        SWTBotTree tree = bot.tree();
        log.info("allItems " + tree.getAllItems().length);
        bot_().tree().selectTreeItemAndWait(nodes);
        bot_().button(buttonText).click();
    }

    public void confirmWindowWithCheckBox(String buttonText, boolean isChecked)
        throws RemoteException {
        activate();
        if (isChecked)
            bot.checkBox().click();
        bot_().button(buttonText).click();

    }

    public void confirmWindowWithCheckBoxs(String buttonText,
        String... itemNames) throws RemoteException {
        waitUntilActive();
        for (String itemName : itemNames) {
            stfTable.selectCheckBoxInTable(itemName);
        }
        bot_().button(buttonText).waitUntilIsEnabled();
        bot_().button(buttonText).click();
    }

    public void confirmShellWithTable(String itemName, String buttonText)
        throws RemoteException {
        // waitUntilShellActive(shellName);
        try {
            bot.table().select(itemName);
            bot_().button(buttonText).waitUntilIsEnabled();
            bot_().button(buttonText).click();
            // waitUntilShellCloses(shellName);
        } catch (WidgetNotFoundException e) {
            log.error("tableItem" + itemName + "can not be fund!");
        }
    }

    public void confirmShellWithTreeWithFilterText(String rootOfTreeNode,
        String teeNode, String buttonText) throws RemoteException {
        // waitUntilShellActive(shellName);
        bot.text(TEXT_FIELD_TYPE_FILTER_TEXT).setText(teeNode);
        bot_().tree().waitUntilSubItemExists(rootOfTreeNode);
        // SWTBotTreeItem treeItem = bot.tree(0).getTreeItem(rootOfTreeNode);
        // bot_().tree().waitUntilSubItemExists(teeNode);
        // treeItem.getNode(teeNode).select();
        bot_().tree().selectTreeItem(rootOfTreeNode, teeNode);
        bot_().button(buttonText).waitUntilIsEnabled();
        bot_().button(buttonText).click();
        // waitUntilShellCloses(shellName);
    }

    /**********************************************
     * 
     * states
     * 
     **********************************************/
    public boolean isActive() throws RemoteException {

        return swtBotShell.isActive();
    }

    public String getTextOfActiveShell() throws RemoteException {
        final SWTBotShell activeShell = bot.activeShell();
        return activeShell == null ? null : activeShell.getText();
    }

    public String getErrorMessageInShell() throws RemoteException {

        final String errorMessage = UIThreadRunnable
            .syncExec(new StringResult() {
                public String run() {
                    WizardDialog dialog = (WizardDialog) swtBotShell.widget
                        .getData();
                    return dialog.getErrorMessage();
                }
            });
        if (errorMessage == null) {
            throw new WidgetNotFoundException("Could not find errorMessage!");
        }
        return errorMessage;
    }

    public boolean existsTableItemInShell(String label) throws RemoteException {
        activate();
        // return bo_t()..stfTable.existsTableItem(label);
        return false;
    }

    /**********************************************
     * 
     * waits until
     * 
     **********************************************/

    public void waitUntilActive() throws RemoteException {
        waitUntil(SarosConditions.ShellActive(swtBotShell));
        // if (!isShellActive(title))
        // throw new RemoteException("Couldn't activate shell \"" + title
        // + "\"");
    }

    public void waitShortUntilIsShellClosed() throws RemoteException {
        waitShortUntil(SarosConditions.isShellClosed(swtBotShell));
    }

    public void waitLongUntilShellClosed() throws RemoteException {
        waitLongUntil(SarosConditions.isShellClosed(swtBotShell));
    }

    public void setShellTitle(String title) throws RemoteException {
        // if (shellTitle == null || !shellTitle.equals(title)) {
        this.shellTitle = title;
        swtBotShell = bot.shell(title);
        // }

    }

    public STFBot bot_() {
        STFBotImp botImp = STFBotImp.getInstance();
        botImp.setBot(swtBotShell.bot());
        return botImp;
    }
}
