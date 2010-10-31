package de.fu_berlin.inf.dpp.stf.conditions;

import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarButton;

import de.fu_berlin.inf.dpp.stf.swtbot.saros.finder.SarosSWTBot;

public class IsConnect extends DefaultCondition {

    private SarosSWTBot bot;

    IsConnect(SarosSWTBot bot) {
        this.bot = bot;
    }

    public String getFailureMessage() {

        return null;
    }

    public boolean test() throws Exception {
        // if (state.isConnectedByXMPP())
        // return true;
        // else
        // return false;
        for (SWTBotToolbarButton toolbarButton : bot.viewByTitle("Roster")
            .getToolbarButtons()) {
            if (toolbarButton.getToolTipText().matches("Disconnect.*")) {
                return true;
            }
        }

        return false;
    }
}
