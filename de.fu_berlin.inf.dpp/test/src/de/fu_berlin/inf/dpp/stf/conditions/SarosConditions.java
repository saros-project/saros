package de.fu_berlin.inf.dpp.stf.conditions;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.waits.ICondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;

public class SarosConditions extends Conditions {

    public static ICondition isConnect(SWTWorkbenchBot bot) {
        return new IsConnect(bot);
    }

    public static ICondition existTreeItem(SWTBotTreeItem treeItem,
        String nodeName) {
        return new ExistTreeItem(treeItem, nodeName);
    }

    public static ICondition existTree(SWTBotTree tree, String nodeName) {
        return new ExistTree(tree, nodeName);
    }

    public static ICondition ShellActive(SWTWorkbenchBot bot, String title) {
        return new ShellActive(bot, title);
    }

}
