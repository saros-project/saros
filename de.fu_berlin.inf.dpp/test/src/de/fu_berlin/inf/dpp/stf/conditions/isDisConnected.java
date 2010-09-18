package de.fu_berlin.inf.dpp.stf.conditions;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarButton;

import de.fu_berlin.inf.dpp.stf.sarosswtbot.SarosConstant;

public class isDisConnected extends DefaultCondition {

    private SWTWorkbenchBot bot1;

    isDisConnected(SWTWorkbenchBot bot) {

        this.bot1 = bot;
    }

    public String getFailureMessage() {

        return null;
    }

    public boolean test() throws Exception {

        for (SWTBotToolbarButton toolbarButton : bot1.viewByTitle(
            SarosConstant.VIEW_TITLE_ROSTER).getToolbarButtons()) {
            if (toolbarButton.getToolTipText().matches("Connect")) {
                return true;
            }
        }

        return false;
    }

}
