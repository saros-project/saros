package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.conditions;

import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;

import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.IRmiSWTWorkbenchBot;

public class IsEditorClosed extends DefaultCondition {

    private IRmiSWTWorkbenchBot bot1;
    private String name;

    IsEditorClosed(IRmiSWTWorkbenchBot bot, String name) {

        this.name = name;
        this.bot1 = bot;
    }

    public String getFailureMessage() {

        return null;
    }

    public boolean test() throws Exception {
        return !bot1.isFileOpen(name);
    }

}
