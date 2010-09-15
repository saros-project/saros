package de.fu_berlin.inf.dpp.stf.conditions;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;

public class isEditorActive extends DefaultCondition {

    private SWTWorkbenchBot bot1;
    private String name;

    isEditorActive(SWTWorkbenchBot bot, String name) {

        this.name = name;
        this.bot1 = bot;
    }

    public String getFailureMessage() {

        return null;
    }

    public boolean test() throws Exception {

        try {
            SWTBotEditor editor = bot1.editorByTitle(name);
            return editor.isActive();
            // return true;
        } catch (WidgetNotFoundException e) {
            return false;
        }

    }

}
