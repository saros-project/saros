package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noExportedPages;

import static org.eclipse.swtbot.swt.finder.waits.Conditions.shellCloses;
import static org.eclipse.swtbot.swt.finder.waits.Conditions.tableHasRows;

import org.apache.log4j.Logger;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.waits.ICondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTableItem;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;

import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.conditions.SarosConditions;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.conditions.SarosSWTBotPreferences;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.RmiSWTWorkbenchBot;
import de.fu_berlin.inf.dpp.stf.swtbot.saros.finder.SarosSWTBot;

public class WaitUntilObject {
    private static final transient Logger log = Logger
        .getLogger(WaitUntilObject.class);
    private RmiSWTWorkbenchBot rmiBot;
    private static SarosSWTBot bot = new SarosSWTBot();

    public WaitUntilObject(RmiSWTWorkbenchBot rmiBot) {
        this.rmiBot = rmiBot;

    }

    public void waitUntilShellCloses(SWTBotShell shell) {
        waitUntil(shellCloses(shell));
        bot.sleep(10);
    }

    public void waitUntilShellCloses(String shellText) {
        waitUntil(SarosConditions.isShellClosed(bot, shellText));
        bot.sleep(10);
    }

    public void waitUntilEditorActive(String name) {
        waitUntil(SarosConditions.isEditorActive(bot, name));
    }

    public void waitUntilTableHasRows(int row) {
        waitUntil(tableHasRows(bot.table(), row));
    }

    public void waitUntilTableItemExisted(SWTBotTable table,
        String tableItemName) {
        waitUntil(SarosConditions.existTableItem(table, tableItemName));
    }

    public void waitUntilTreeItemExisted(SWTBotTreeItem treeItem,
        String nodeName) {
        waitUntil(SarosConditions.existTreeItem(treeItem, nodeName));
    }

    public void waitUntilTreeExisted(SWTBotTree tree, String nodeName) {
        waitUntil(SarosConditions.existTree(tree, nodeName));
    }

    public void waitUntilButtonEnabled(String mnemonicText) {
        waitUntil(Conditions.widgetIsEnabled(bot.button(mnemonicText)));
        // try {
        // while (!delegate.button(mnemonicText).isEnabled()) {
        // delegate.sleep(100);
        // }
        // } catch (Exception e) {
        // // next window opened
        // }
    }

    public void waitUnitButtonWithTooltipTextEnabled(String tooltipText) {
        waitUntil(Conditions
            .widgetIsEnabled(bot.buttonWithTooltip(tooltipText)));
    }

    public void waitUntilContextMenuOfTableItemEnabled(
        SWTBotTableItem tableItem, String context) {
        waitUntil(SarosConditions.ExistContextMenuOfTableItem(tableItem,
            context));
    }

    public void waitUntilShellActive(String title) {
        waitUntil(SarosConditions.ShellActive(bot, title));
        // if (!isShellActive(title))
        // throw new RemoteException("Couldn't activate shell \"" + title
        // + "\"");
    }

    public void waitUntil(ICondition condition) {
        bot.waitUntil(condition, SarosSWTBotPreferences.SAROS_TIMEOUT);
    }
}
