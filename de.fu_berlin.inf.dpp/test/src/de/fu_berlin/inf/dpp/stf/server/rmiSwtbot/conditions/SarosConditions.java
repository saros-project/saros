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
import de.fu_berlin.inf.dpp.stf.sarosSWTBot.SarosSWTBot;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noGUI.EclipseStateObjectImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.noGUI.SarosStateObject;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.ChatViewObjectImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench.EclipseEditorObject;

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

    public static ICondition isEditorOpen(EclipseEditorObject editor,
        String name) {
        return new IsEditorOpen(editor, name);
    }

    public static ICondition isEditorClosed(EclipseEditorObject editor,
        String name) {
        return new IsEditorClosed(editor, name);
    }

    public static ICondition isSessionClosed(SarosStateObject state) {
        return new isSessionclosed(state);
    }

    public static ICondition isInSession(SarosStateObject state) {
        return new IsInSession(state);
    }

    public static ICondition isShellClosed(SWTWorkbenchBot bot, String name) {
        return new IsShellClosed(bot, name);
    }

    public static ICondition isViewActive(SWTWorkbenchBot bot, String name) {
        return new isViewActive(bot, name);
    }

    public static ICondition isClassContentsSame(EclipseStateObjectImp state,
        String projectName, String pkg, String className,
        String otherClassContent) {
        return new IsClassContentsSame(state, projectName, pkg, className,
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

    public static ICondition existNoParticipant(SarosStateObject state,
        List<JID> jids) {
        return new ExistNoParticipants(state, jids);
    }

    public static ICondition isChatMessageExist(ChatViewObjectImp chatV,
        String jid, String message) {
        return new IsChatMessageExist(chatV, jid, message);
    }

    public static ICondition existNoInvitationProgress(SarosSWTBot bot) {
        return new ExistNoInvitationProgress(bot);
    }

    public static ICondition isJavaEditorContentsSame(EclipseEditorObject editor,
        String projectName, String packageName, String className,
        String otherContent) {
        return new IsJavaEditorContentsSame(editor, projectName, packageName,
            className, otherContent);
    }

    public static ICondition isEditorContentsSame(EclipseEditorObject editor,
        String otherContent, String... filePath) {
        return new IsEditorContentsSame(editor, otherContent, filePath);
    }

    public static ICondition isFollowingUser(SarosStateObject state,
        String plainJID) {
        return new IsFollowingUser(state, plainJID);
    }
}
