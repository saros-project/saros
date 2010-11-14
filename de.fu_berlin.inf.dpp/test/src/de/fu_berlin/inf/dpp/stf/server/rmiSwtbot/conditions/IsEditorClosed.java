package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.conditions;

import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;

import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench.ExEditorObject;

public class IsEditorClosed extends DefaultCondition {

    private ExEditorObject editor;
    private String name;

    IsEditorClosed(ExEditorObject editor, String name) {

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
