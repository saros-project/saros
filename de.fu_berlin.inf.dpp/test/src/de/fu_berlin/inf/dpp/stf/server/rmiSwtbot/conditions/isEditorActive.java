package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.conditions;

import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;

import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench.EditorComponent;

public class isEditorActive extends DefaultCondition {

    private EditorComponent editorPart;
    private String fileName;

    isEditorActive(EditorComponent editor, String fileName) {
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
