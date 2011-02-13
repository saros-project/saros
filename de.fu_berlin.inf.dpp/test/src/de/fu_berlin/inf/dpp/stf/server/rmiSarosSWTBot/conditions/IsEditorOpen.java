package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.conditions;

import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.finder.remoteWidgets.STFBotEditor;

public class IsEditorOpen extends DefaultCondition {

    private STFBotEditor editor;
    private String name;

    IsEditorOpen(STFBotEditor editor, String name) {
        this.name = name;
        this.editor = editor;
    }

    public String getFailureMessage() {

        return null;
    }

    public boolean test() throws Exception {
        return editor.isEditorOpen(name);
    }

}
