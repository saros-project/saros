package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.conditions;

import java.util.List;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.waits.ICondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarButton;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.basicWidgets.Table;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.editor.Editor;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.views.sarosViews.ChatViewImp;
import de.fu_berlin.inf.dpp.stf.server.sarosSWTBot.SarosSWTBot;

/**
 * This is a factory class to create some conditions provided with STF.
 */
public class SarosConditions extends Conditions {

    public static ICondition isConnect(List<SWTBotToolbarButton> buttons,
        String tooltipText) {
        return new IsConnect(buttons, tooltipText);
    }

    public static ICondition isDisConnected(List<SWTBotToolbarButton> buttons,
        String tooltipText) {
        return new isDisConnected(buttons, tooltipText);
    }

    public static ICondition ShellActive(SWTWorkbenchBot bot, String title) {
        return new ShellActive(bot, title);
    }

    public static ICondition isShellOpen(SWTWorkbenchBot bot, String title) {
        return new IsShellOpen(bot, title);
    }

    public static ICondition existTableItem(Table table, String tableItemName) {
        return new ExistTableItem(table, tableItemName);
    }

    public static ICondition ExistContextMenuOfTableItem(Table table,
        String itemName, String contextName) {
        return new ExistContextMenuOfTableItem(table, itemName, contextName);
    }

    /**********************************************
     * 
     * conditions for editor
     * 
     **********************************************/
    public static ICondition isEditorOpen(Editor editor, String name) {
        return new IsEditorOpen(editor, name);
    }

    public static ICondition isEditorActive(Editor editor, String name) {
        return new isEditorActive(editor, name);
    }

    public static ICondition isEditorClosed(Editor editor, String name) {
        return new IsEditorClosed(editor, name);
    }

    public static ICondition isShellClosed(SWTWorkbenchBot bot, String name) {
        return new IsShellClosed(bot, name);
    }

    public static ICondition isViewActive(SWTWorkbenchBot bot, String name) {
        return new isViewActive(bot, name);
    }

    public static ICondition isFileContentsSame(Editor state,
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

    public static ICondition isChatMessageExist(ChatViewImp chatV, String jid,
        String message) {
        return new IsChatMessageExist(chatV, jid, message);
    }

    public static ICondition existNoInvitationProgress(SarosSWTBot bot) {
        return new ExistNoInvitationProgress(bot);
    }

    public static ICondition isJavaEditorContentsSame(Editor editor,
        String projectName, String packageName, String className,
        String otherContent) {
        return new IsJavaEditorContentsSame(editor, projectName, packageName,
            className, otherContent);
    }

    public static ICondition isEditorContentsSame(Editor editor,
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
