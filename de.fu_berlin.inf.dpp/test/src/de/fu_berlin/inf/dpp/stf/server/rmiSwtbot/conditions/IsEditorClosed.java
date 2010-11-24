package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.conditions;

import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;

import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench.EditorComponent;

public class IsEditorClosed extends DefaultCondition {

    private EditorComponent editor;
    private String name;

    IsEditorClosed(EditorComponent editor, String name) {

        this.name = name;
        this.editor = editor;
    }

    public String getFailureMessage() {

        return null;
    }

    public boolean test() throws Exception {
        return !editor.isFileOpen(name);
    }

}
