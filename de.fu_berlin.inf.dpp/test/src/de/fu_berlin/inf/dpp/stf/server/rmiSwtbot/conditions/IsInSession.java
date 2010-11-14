package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.conditions;

import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;

import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.noGUI.ExStateObject;

public class IsInSession extends DefaultCondition {

    private ExStateObject state;

    IsInSession(ExStateObject state) {
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
