package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.conditions;

import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;

import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.saros.noGUI.ISarosState;

public class isSessionclosed extends DefaultCondition {

    private ISarosState state;

    isSessionclosed(ISarosState state) {

        this.state = state;
    }

    public String getFailureMessage() {

        return null;
    }

    public boolean test() throws Exception {
        System.out.println("existSession() == " + state.isInSession());
        if (state.isInSession()) {
            return false;
        }
        return true;
    }
}
