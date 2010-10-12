package de.fu_berlin.inf.dpp.stf.conditions;

import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarButton;

import de.fu_berlin.inf.dpp.stf.sarosswtbot.SarosConstant;
import de.fu_berlin.inf.dpp.stf.swtbot.SarosSWTWorkbenchBot;

public class isDisConnected extends DefaultCondition {

    private SarosSWTWorkbenchBot bot;

    isDisConnected(SarosSWTWorkbenchBot bot) {
        this.bot = bot;
    }

    public String getFailureMessage() {

        return null;
    }

    public boolean test() throws Exception {
        // if (state.isConnectedByXMPP())
        // return false;
        // else
        // return true;

        for (SWTBotToolbarButton toolbarButton : bot.viewByTitle(
            SarosConstant.VIEW_TITLE_ROSTER).getToolbarButtons()) {
            if (toolbarButton.getToolTipText().matches("Connect")) {
                return true;
            }
        }

        return false;
    }
}
