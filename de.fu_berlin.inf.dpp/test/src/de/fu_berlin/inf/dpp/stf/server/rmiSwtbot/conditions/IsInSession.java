package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.conditions;

import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;

import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.noGUI.SarosState;

public class IsInSession extends DefaultCondition {

    private SarosState state;

    IsInSession(SarosState state) {
        this.state = state;
    }

    public String getFailureMessage() {

        return null;
    }

    public boolean test() throws Exception {

        if (state.isInSession())
            return true;

        return false;
    }

}
