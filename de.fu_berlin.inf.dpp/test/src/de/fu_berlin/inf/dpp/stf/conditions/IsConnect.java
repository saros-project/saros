package de.fu_berlin.inf.dpp.stf.conditions;

import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarButton;

import de.fu_berlin.inf.dpp.stf.swtbot.SarosSWTWorkbenchBot;

public class IsConnect extends DefaultCondition {

    private SarosSWTWorkbenchBot bot;

    IsConnect(SarosSWTWorkbenchBot bot) {
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
