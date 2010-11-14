package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.conditions;

import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;

import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.noGUI.ExStateObject;

public class IsFollowingUser extends DefaultCondition {

    private ExStateObject state;
    private String baseJID;

    IsFollowingUser(ExStateObject state, String plainJID) {
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