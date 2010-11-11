package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noExportedPages;

import static org.eclipse.swtbot.swt.finder.waits.Conditions.shellCloses;

import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;

import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.conditions.SarosConditions;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.EclipseObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.SarosRmiSWTWorkbenchBot;

public class WindowObject extends EclipseObject {

    public WindowObject(SarosRmiSWTWorkbenchBot rmiBot) {
        super(rmiBot);
    }

    public String getCurrentActiveShell() {
        final SWTBotShell activeShell = bot.activeShell();
        return activeShell == null ? null : activeShell.getText();
    }

    public boolean isTableItemInWindowExist(String title, String label) {
        activateShellWithText(title);
        return tableObject.existTableItem(label);
    }

    public boolean activateShellWithText(String title) {
        SWTBotShell[] shells = bot.shells();
        for (SWTBotShell shell : shells) {
            if (shell.getText().equals(title)) {
                log.debug("shell found");
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
        // waitUntilShellActive(shellName);
        SWTBotTree tree = bot.tree();
        log.info("allItems " + tree.getAllItems().length);
        treeObject.selectTreeWithLabelsWithWaitungExpand(tree, nodes);
        // basicObject.waitUntilButtonEnabled(buttonText);
        bot.button(buttonText).click();
        // waitUntilShellCloses(shellName);
    }
}
