package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.conditions;

import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;

import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.saros.noGUI.ISarosState;

public class IsInSession extends DefaultCondition {

    private ISarosState state;

    IsInSession(ISarosState state) {
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
