package de.fu_berlin.inf.dpp.stf.conditions;

import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;

import de.fu_berlin.inf.dpp.stf.sarosswtbot.ISarosState;

public class IsConnect extends DefaultCondition {

    private ISarosState state;

    IsConnect(ISarosState state) {

        this.state = state;
    }

    public String getFailureMessage() {

        return null;
    }

    public boolean test() throws Exception {
        if (state.isConnectedByXMPP())
            return true;
        else
            return false;
        // for (SWTBotToolbarButton toolbarButton : bot1.viewByTitle("Roster")
        // .getToolbarButtons()) {
        // if (toolbarButton.getToolTipText().matches("Disconnect.*")) {
        // return true;
        // }
        // }
        //
        // return false;
    }
}
