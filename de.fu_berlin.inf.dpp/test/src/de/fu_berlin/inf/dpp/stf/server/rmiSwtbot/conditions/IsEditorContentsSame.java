package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.conditions;

import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;

import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench.EclipseEditorObject;

public class IsEditorContentsSame extends DefaultCondition {

    private EclipseEditorObject editor;
    private String otherContent;
    private String[] filePath;

    IsEditorContentsSame(EclipseEditorObject editor, String otherConent,
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
