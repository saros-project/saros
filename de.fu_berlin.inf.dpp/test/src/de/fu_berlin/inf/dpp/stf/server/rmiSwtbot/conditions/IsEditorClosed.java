package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.conditions;

import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;

import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench.EditorComponent;

public class IsEditorClosed extends DefaultCondition {

    private EditorComponent editorComponent;
    private String fileName;

    IsEditorClosed(EditorComponent editorComponent, String name) {

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
