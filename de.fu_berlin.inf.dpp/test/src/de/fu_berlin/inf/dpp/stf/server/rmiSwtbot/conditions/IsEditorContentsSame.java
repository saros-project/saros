package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.conditions;

import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;

import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.EclipseControler;

public class IsEditorContentsSame extends DefaultCondition {

    private EclipseControler bot1;
    private String otherContent;
    private String[] filePath;

    IsEditorContentsSame(EclipseControler bot, String otherConent,
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
