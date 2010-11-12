package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.conditions;

import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;

import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.noGUI.SarosState;

public class IsFollowingUser extends DefaultCondition {

    private SarosState state;
    private String plainJID;

    IsFollowingUser(SarosState state, String plainJID) {
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