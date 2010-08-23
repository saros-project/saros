package de.fu_berlin.inf.dpp.stf.conditions;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarButton;

public class IsConnect extends DefaultCondition {

    private SWTWorkbenchBot bot1;

    IsConnect(SWTWorkbenchBot bot) {

        this.bot1 = bot;
    }

    public String getFailureMessage() {

        return null;
    }

    public boolean test() throws Exception {

        for (SWTBotToolbarButton toolbarButton : bot1.viewByTitle("Roster")
            .getToolbarButtons()) {
            if (toolbarButton.getToolTipText().matches("Disconnect.*")) {
                return true;
            }
        }

        return false;
    }

}
