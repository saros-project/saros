package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.conditions;

import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;

import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.noGUI.ExStateObject;

public class IsFollowingUser extends DefaultCondition {

    private ExStateObject state;
    private String plainJID;

    IsFollowingUser(ExStateObject state, String plainJID) {
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