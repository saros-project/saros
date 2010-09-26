package de.fu_berlin.inf.dpp.stf.conditions;

import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;

import de.fu_berlin.inf.dpp.stf.sarosswtbot.ISarosState;

public class isSessionclosed extends DefaultCondition {

    private ISarosState state;

    isSessionclosed(ISarosState state) {

        this.state = state;
    }

    public String getFailureMessage() {

        return null;
    }

    public boolean test() throws Exception {
        System.out.println("existSession() == " + state.existSession());
        if (state.existSession()) {
            return false;
        }
        return true;
    }
}
