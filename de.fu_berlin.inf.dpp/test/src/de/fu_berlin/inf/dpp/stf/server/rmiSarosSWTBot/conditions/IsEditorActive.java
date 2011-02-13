package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.conditions;

import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.finder.remoteWidgets.Editor;

public class IsEditorActive extends DefaultCondition {

    private Editor editorPart;
    private String fileName;

    IsEditorActive(Editor editor, String fileName) {
        this.fileName = fileName;
        this.editorPart = editor;
    }

    public String getFailureMessage() {
        return "Editor " + fileName + " is not active.";
    }

    public boolean test() throws Exception {
        return editorPart.isEditorActive(fileName);
    }
}
