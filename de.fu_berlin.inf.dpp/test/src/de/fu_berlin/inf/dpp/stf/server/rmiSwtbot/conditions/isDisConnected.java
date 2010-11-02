package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.conditions;

import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarButton;

import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.SarosConstant;
import de.fu_berlin.inf.dpp.stf.swtbot.saros.finder.SarosSWTBot;

public class isDisConnected extends DefaultCondition {

    private SarosSWTBot bot;

    isDisConnected(SarosSWTBot bot) {
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
