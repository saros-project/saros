package de.fu_berlin.inf.dpp.stf.conditions;

import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;

import de.fu_berlin.inf.dpp.stf.RMISwtbot.saros.ISarosState;

public class IsFollowingUser extends DefaultCondition {

    private ISarosState state;
    private String plainJID;

    IsFollowingUser(ISarosState state, String plainJID) {
        this.state = state;
        this.plainJID = plainJID;
    }

    public String getFailureMessage() {

        return null;
    }

    public boolean test() throws Exception {
        return state.isFollowingUser(plainJID);
    }

}