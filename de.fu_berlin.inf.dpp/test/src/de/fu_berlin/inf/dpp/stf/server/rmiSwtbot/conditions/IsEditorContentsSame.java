package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.conditions;

import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;

import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.RmiSWTWorkbenchBot;

public class IsEditorContentsSame extends DefaultCondition {

    private RmiSWTWorkbenchBot bot1;
    private String otherContent;
    private String[] filePath;

    IsEditorContentsSame(RmiSWTWorkbenchBot bot, String otherConent,
        String... filePath) {
        this.bot1 = bot;
        this.otherContent = otherConent;
        this.filePath = filePath;

    }

    public String getFailureMessage() {
        return null;
    }

    public boolean test() throws Exception {
        return bot1.getEclipseEditorObject().getTextOfEditor(filePath)
            .equals(otherContent);
    }
}
