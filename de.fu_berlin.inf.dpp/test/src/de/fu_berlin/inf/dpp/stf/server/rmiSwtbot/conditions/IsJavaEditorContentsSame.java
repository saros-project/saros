package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.conditions;

import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;

import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench.ExEditorObject;

public class IsJavaEditorContentsSame extends DefaultCondition {

    private ExEditorObject editor;
    private String file;
    private String projectName;
    private String packageName;
    private String className;

    IsJavaEditorContentsSame(ExEditorObject editor, String projectName,
        String packageName, String className, String otherConent) {
        this.editor = editor;
        this.file = otherConent;
        this.projectName = projectName;
        this.packageName = packageName;
        this.className = className;

    }

    public String getFailureMessage() {

        return null;
    }

    public boolean test() throws Exception {
        return editor.getTextOfJavaEditor(projectName, packageName, className)
            .equals(file);
    }
}
