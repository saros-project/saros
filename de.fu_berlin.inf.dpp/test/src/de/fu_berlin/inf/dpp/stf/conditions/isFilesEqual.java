package de.fu_berlin.inf.dpp.stf.conditions;

import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;

import de.fu_berlin.inf.dpp.stf.swtbot.RmiSWTWorkbenchBot;

public class isFilesEqual extends DefaultCondition {

    private RmiSWTWorkbenchBot bot1;
    private String file;
    private String projectName;
    private String packageName;
    private String className;

    isFilesEqual(RmiSWTWorkbenchBot bot, String projectName,
        String packageName, String className, String file) {

        this.bot1 = bot;
        this.file = file;
        this.projectName = projectName;
        this.packageName = packageName;
        this.className = className;

    }

    public String getFailureMessage() {

        return null;
    }

    public boolean test() throws Exception {
        return bot1.getTextOfJavaEditor(projectName, packageName, className)
            .equals(file);
    }
}
