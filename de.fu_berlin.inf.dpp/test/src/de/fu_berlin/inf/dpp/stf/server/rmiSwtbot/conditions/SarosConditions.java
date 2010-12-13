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

import de.fu_berlin.inf.dpp.stf.sarosSWTBot.SarosSWTBot;
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

    /**********************************************
     * 
     * conditions for editor
     * 
     **********************************************/
    public static ICondition isEditorOpen(EditorComponent editor, String name) {
        return new IsEditorOpen(editor, name);
    }

    public static ICondition isEditorActive(EditorComponent editor, String name) {
        return new isEditorActive(editor, name);
    }

    public static ICondition isEditorClosed(EditorComponent editor, String name) {
        return new IsEditorClosed(editor, name);
    }

    public static ICondition isShellClosed(SWTWorkbenchBot bot, String name) {
        return new IsShellClosed(bot, name);
    }

    public static ICondition isViewActive(SWTWorkbenchBot bot, String name) {
        return new isViewActive(bot, name);
    }

    public static ICondition isFileContentsSame(EditorComponent state,
        String otherClassContent, String... fileNodes) {
        return new IsFileContentsSame(state, otherClassContent, fileNodes);
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

    public static ICondition isRevisionSame(String fullPath, String revisionID) {
        return new IsRevisionSame(fullPath, revisionID);
    }

    public static ICondition isUrlSame(String fullPath, String url) {
        return new IsUrlSame(fullPath, url);
    }
}
