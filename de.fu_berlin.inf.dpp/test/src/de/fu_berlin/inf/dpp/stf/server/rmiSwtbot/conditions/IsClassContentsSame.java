package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.conditions;

import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;

import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.IRmiSWTWorkbenchBot;

public class IsClassContentsSame extends DefaultCondition {

    private IRmiSWTWorkbenchBot bot1;

    private String projectName;
    private String pkg;
    private String className;
    private String otherClassContent;

    IsClassContentsSame(IRmiSWTWorkbenchBot bot, String projectName,
        String pkg, String className, String otherClassContent) {
        this.bot1 = bot;
        this.projectName = projectName;
        this.pkg = pkg;
        this.className = className;
        this.otherClassContent = otherClassContent;

    }

    public String getFailureMessage() {

        return null;
    }

    public boolean test() throws Exception {
        String classContent = bot1.getEclipseState().getClassContent(
            projectName, pkg, className);
        return classContent.equals(otherClassContent);
    }
}
