package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.conditions;

import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;

import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noExportedObjects.EditorObject;

public class isEditorActive extends DefaultCondition {

    private EditorObject editor;
    private String fileName;

    isEditorActive(EditorObject editor, String fileName) {
        this.fileName = fileName;
        this.editor = editor;
    }

    public String getFailureMessage() {

        return null;
    }

    public boolean test() throws Exception {
        try {
            SWTBotEditor e = editor.getEditor(fileName);
            return e.isActive();
        } catch (WidgetNotFoundException e) {
            return false;
        }

    }

}
