package de.fu_berlin.inf.dpp.stf.conditions;

import java.util.List;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.waits.ICondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTableItem;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.sarosswtbot.ISarosState;
import de.fu_berlin.inf.dpp.stf.swtbot.RmiSWTWorkbenchBot;

public class SarosConditions extends Conditions {

    public static ICondition isConnect(ISarosState state) {
        return new IsConnect(state);
    }

    public static ICondition isDisConnected(ISarosState state) {
        return new isDisConnected(state);
    }

    public static ICondition existTreeItem(SWTBotTreeItem treeItem,
        String nodeName) {
        return new ExistTreeItem(treeItem, nodeName);
    }

    public static ICondition existTree(SWTBotTree tree, String... nodeNames) {
        return new ExistTree(tree, nodeNames);
    }

    public static ICondition ShellActive(SWTWorkbenchBot bot, String title) {
        return new ShellActive(bot, title);
    }

    public static ICondition existTableItem(SWTBotTable table,
        String tableItemName) {
        return new ExistTableItem(table, tableItemName);
    }

    public static ICondition ExistContextMenuOfTableItem(
        SWTBotTableItem tableItem, String text) {
        return new ExistContextMenuOfTableItem(tableItem, text);
    }

    public static ICondition isEditorActive(SWTWorkbenchBot bot, String name) {
        return new isEditorActive(bot, name);
    }

    public static ICondition isSessionClosed(ISarosState state) {
        return new isSessionclosed(state);
    }

    public static ICondition isInSession(ISarosState state) {
        return new IsInSession(state);
    }

    public static ICondition isShellClosed(SWTWorkbenchBot bot, String name) {
        return new IsShellClosed(bot, name);
    }

    public static ICondition isViewActive(SWTWorkbenchBot bot, String name) {
        return new isViewActive(bot, name);
    }

    public static ICondition isFilesEqual(RmiSWTWorkbenchBot bot,
        String projectName, String packageName, String className, String name) {
        return new isFilesEqual(bot, projectName, packageName, className, name);
    }

    public static ICondition isNotInSVN(String projectName) {
        return new IsNotInSVN(projectName);
    }

    public static ICondition isInSVN(String projectName) {
        return new IsInSVN(projectName);
    }

    public static ICondition isResourceExist(String resourcePath) {
        return new ExistResource(resourcePath);
    }

    public static ICondition isResourceNotExist(String resourcePath) {
        return new ExistNoResource(resourcePath);
    }

    public static ICondition existNoParticipant(ISarosState state,
        List<JID> jids) {
        return new ExistNoParticipants(state, jids);
    }
}
