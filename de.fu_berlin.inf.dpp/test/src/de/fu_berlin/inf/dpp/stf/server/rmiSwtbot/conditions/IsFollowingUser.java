package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.conditions;

import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;

import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.noGUI.SarosState;

public class IsFollowingUser extends DefaultCondition {

    private SarosState state;
    private String baseJID;

    IsFollowingUser(SarosState state, String plainJID) {
        this.state = state;
        this.baseJID = plainJID;
    }

    public String getFailureMessage() {

        return "You are not in follow mode or the followed user is not "
            + baseJID;
    }

    public boolean test() throws Exception {
        return state.isFollowingUser(baseJID);
    }

}