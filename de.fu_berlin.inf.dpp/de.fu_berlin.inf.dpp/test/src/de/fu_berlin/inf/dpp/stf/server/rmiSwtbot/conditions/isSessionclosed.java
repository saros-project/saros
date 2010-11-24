package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.conditions;

import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;

import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.noGUI.SarosState;

public class isSessionclosed extends DefaultCondition {

    private SarosState state;

    isSessionclosed(SarosState state) {

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
