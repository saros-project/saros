package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.conditions;

import java.util.List;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.waits.ICondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTableItem;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.IRmiSWTWorkbenchBot;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.RmiSWTWorkbenchBot;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.saros.ISarosRmiSWTWorkbenchBot;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.saros.noGUI.ISarosState;
import de.fu_berlin.inf.dpp.stf.swtbot.saros.finder.SarosSWTBot;

public class SarosConditions extends Conditions {

    public static ICondition isConnect(SarosSWTBot bot) {
        return new IsConnect(bot);
    }

    public static ICondition isDisConnected(SarosSWTBot bot) {
        return new isDisConnected(bot);
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

    public static ICondition isEditorOpen(IRmiSWTWorkbenchBot bot, String name) {
        return new IsEditorOpen(bot, name);
    }

    public static ICondition isEditorClosed(IRmiSWTWorkbenchBot bot, String name) {
        return new IsEditorClosed(bot, name);
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

    public static ICondition isClassContentsSame(IRmiSWTWorkbenchBot bot,
        String projectName, String pkg, String className,
        String otherClassContent) {
        return new IsClassContentsSame(bot, projectName, pkg, className,
            otherClassContent);
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

    public static ICondition isChatMessageExist(ISarosRmiSWTWorkbenchBot bot,
        String jid, String message) {
        return new IsChatMessageExist(bot, jid, message);
    }

    public static ICondition existNoInvitationProgress(SarosSWTBot bot) {
        return new ExistNoInvitationProgress(bot);
    }

    public static ICondition isJavaEditorContentsSame(RmiSWTWorkbenchBot bot,
        String projectName, String packageName, String className,
        String otherContent) {
        return new IsJavaEditorContentsSame(bot, projectName, packageName,
            className, otherContent);
    }

    public static ICondition isEditorContentsSame(RmiSWTWorkbenchBot bot,
        String otherContent, String... filePath) {
        return new IsEditorContentsSame(bot, otherContent, filePath);
    }

    public static ICondition isFollowingUser(ISarosState state, String plainJID) {
        return new IsFollowingUser(state, plainJID);
    }
}
