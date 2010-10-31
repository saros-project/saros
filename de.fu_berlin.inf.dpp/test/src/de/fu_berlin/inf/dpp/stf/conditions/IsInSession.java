package de.fu_berlin.inf.dpp.stf.conditions;

import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;

import de.fu_berlin.inf.dpp.stf.RMISwtbot.saros.ISarosState;

public class IsInSession extends DefaultCondition {

    private ISarosState state;

    IsInSession(ISarosState state) {
        this.state = state;
    }

    public String getFailureMessage() {

        return null;
    }

    public boolean test() throws Exception {

        if (state.existSession())
            return true;

        return false;
    }

}
