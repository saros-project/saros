package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.conditions;

import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;

import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.noGUI.ExStateObject;

public class isSessionclosed extends DefaultCondition {

    private ExStateObject state;

    isSessionclosed(ExStateObject state) {

        this.state = state;
    }

    public String getFailureMessage() {

        return "It seems that the session is still open";
    }

    public boolean test() throws Exception {
        if (state.isInSession()) {
            return false;
        }
        return true;
    }
}
