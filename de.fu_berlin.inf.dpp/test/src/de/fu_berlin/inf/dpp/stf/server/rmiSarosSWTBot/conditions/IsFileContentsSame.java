package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.conditions;

import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.finder.remoteWidgets.Editor;

public class IsFileContentsSame extends DefaultCondition {

    private Editor state;

    private String[] fileNodes;
    private String otherClassContent;

    IsFileContentsSame(Editor state, String otherClassContent,
        String... fileNodes) {
        this.state = state;
        this.fileNodes = fileNodes;
        this.otherClassContent = otherClassContent;

    }

    public String getFailureMessage() {
        return null;
    }

    public boolean test() throws Exception {
        String classContent = state.getFileContent(fileNodes);
        return classContent.equals(otherClassContent);
    }
}
