package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.conditions;

import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.editor.Editor;

public class IsEditorClosed extends DefaultCondition {

    private Editor editorComponent;
    private String fileName;

    IsEditorClosed(Editor editorComponent, String name) {

        this.fileName = name;
        this.editorComponent = editorComponent;
    }

    public String getFailureMessage() {
        return "The editor " + fileName + " is not open.";
    }

    public boolean test() throws Exception {
        return !editorComponent.isEditorOpen(fileName);
    }

}
