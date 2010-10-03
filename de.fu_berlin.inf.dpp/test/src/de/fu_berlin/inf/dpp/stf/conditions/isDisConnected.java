package de.fu_berlin.inf.dpp.stf.conditions;

import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;

import de.fu_berlin.inf.dpp.stf.sarosswtbot.ISarosState;

public class isDisConnected extends DefaultCondition {

    private ISarosState state;

    isDisConnected(ISarosState state) {

        this.state = state;
    }

    public String getFailureMessage() {

        return null;
    }

    public boolean test() throws Exception {
        if (state.isConnectedByXMPP())
            return false;
        else
            return true;

        // for (SWTBotToolbarButton toolbarButton : bot1.viewByTitle(
        // SarosConstant.VIEW_TITLE_ROSTER).getToolbarButtons()) {
        // if (toolbarButton.getToolTipText().matches("Connect")) {
        // return true;
        // }
        // }
        //
        // return false;
    }
}
