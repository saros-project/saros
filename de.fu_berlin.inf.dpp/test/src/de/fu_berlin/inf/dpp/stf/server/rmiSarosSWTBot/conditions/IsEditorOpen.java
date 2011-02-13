package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.conditions;

import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.finder.remoteWidgets.Editor;

public class IsEditorOpen extends DefaultCondition {

    private Editor editor;
    private String name;

    IsEditorOpen(Editor editor, String name) {
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
