package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.conditions;

import java.util.List;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.waits.ICondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTableItem;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.sarosSWTBot.SarosSWTBot;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noExportedObjects.EditorPart;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noGUI.StateImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.noGUI.SarosState;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.ChatViewComponentImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench.EditorComponent;

public class SarosConditions extends Conditions {

    public static ICondition isConnect(List<SWTBotToolbarButton> buttons,
        String tooltipText) {
        return new IsConnect(buttons, tooltipText);
    }

    public static ICondition isDisConnected(List<SWTBotToolbarButton> buttons,
        String tooltipText) {
        return new isDisConnected(buttons, tooltipText);
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

    public static ICondition isShellOpen(SWTWorkbenchBot bot, String title) {
        return new IsShellOpen(bot, title);
    }

    public static ICondition existTableItem(SWTBotTable table,
        String tableItemName) {
        return new ExistTableItem(table, tableItemName);
    }

    public static ICondition ExistContextMenuOfTableItem(
        SWTBotTableItem tableItem, String text) {
        return new ExistContextMenuOfTableItem(tableItem, text);
    }

    public static ICondition isEditorActive(EditorPart editor, String name) {
        return new isEditorActive(editor, name);
    }

    public static ICondition isEditorOpen(EditorComponent editor, String name) {
        return new IsEditorOpen(editor, name);
    }

    public static ICondition isEditorClosed(EditorComponent editor, String name) {
        return new IsEditorClosed(editor, name);
    }

    public static ICondition isSessionClosed(SarosState state) {
        return new isSessionclosed(state);
    }

    public static ICondition isInSession(SarosState state) {
        return new IsInSession(state);
    }

    public static ICondition isShellClosed(SWTWorkbenchBot bot, String name) {
        return new IsShellClosed(bot, name);
    }

    public static ICondition isViewActive(SWTWorkbenchBot bot, String name) {
        return new isViewActive(bot, name);
    }

    public static ICondition isClassContentsSame(StateImp state,
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

    public static ICondition existsNoParticipants(SarosState state,
        List<JID> jidsOfAllParticipants) {
        return new ExistsNoParticipants(state, jidsOfAllParticipants);
    }

    public static ICondition isChatMessageExist(ChatViewComponentImp chatV,
        String jid, String message) {
        return new IsChatMessageExist(chatV, jid, message);
    }

    public static ICondition existNoInvitationProgress(SarosSWTBot bot) {
        return new ExistNoInvitationProgress(bot);
    }

    public static ICondition isJavaEditorContentsSame(EditorComponent editor,
        String projectName, String packageName, String className,
        String otherContent) {
        return new IsJavaEditorContentsSame(editor, projectName, packageName,
            className, otherContent);
    }

    public static ICondition isEditorContentsSame(EditorComponent editor,
        String otherContent, String... filePath) {
        return new IsEditorContentsSame(editor, otherContent, filePath);
    }

    public static ICondition isFollowingUser(SarosState state, String plainJID) {
        return new IsFollowingUser(state, plainJID);
    }

    public static ICondition isRevisionSame(String fullPath, String reversionID) {
        return new IsReversionSame(fullPath, reversionID);
    }
}
