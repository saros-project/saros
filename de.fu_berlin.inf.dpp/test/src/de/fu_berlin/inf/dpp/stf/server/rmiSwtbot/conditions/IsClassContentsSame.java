package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.conditions;

import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;

import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noGUI.EclipseStateImp;

public class IsClassContentsSame extends DefaultCondition {

    private EclipseStateImp state;

    private String projectName;
    private String pkg;
    private String className;
    private String otherClassContent;

    IsClassContentsSame(EclipseStateImp state, String projectName, String pkg,
        String className, String otherClassContent) {
        this.state = state;
        this.projectName = projectName;
        this.pkg = pkg;
        this.className = className;
        this.otherClassContent = otherClassContent;

    }

    public String getFailureMessage() {

        return null;
    }

    public boolean test() throws Exception {
        String classContent = state
            .getClassContent(projectName, pkg, className);
        return classContent.equals(otherClassContent);
    }
}
