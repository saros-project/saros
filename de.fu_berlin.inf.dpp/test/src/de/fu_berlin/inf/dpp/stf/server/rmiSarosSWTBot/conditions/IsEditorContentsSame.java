package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.conditions;

import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.finder.remoteWidgets.STFBotEditor;

public class IsEditorContentsSame extends DefaultCondition {

    private STFBotEditor editor;
    private String otherContent;
    private String[] filePath;

    IsEditorContentsSame(STFBotEditor editor, String otherConent,
        String... filePath) {
        this.editor = editor;
        this.otherContent = otherConent;
        this.filePath = filePath;

    }

    public String getFailureMessage() {
        return null;
    }

    public boolean test() throws Exception {
        return editor.getTextOfEditor(filePath).equals(otherContent);
    }
}
