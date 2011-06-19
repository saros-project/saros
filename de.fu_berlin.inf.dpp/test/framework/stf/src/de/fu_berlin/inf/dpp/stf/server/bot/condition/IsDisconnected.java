package de.fu_berlin.inf.dpp.stf.server.bot.condition;


import java.util.List;

import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarButton;

public class IsDisconnected extends DefaultCondition {

    private List<SWTBotToolbarButton> buttons;
    private String tooltipText;

    IsDisconnected(List<SWTBotToolbarButton> buttons, String tooltipText) {
        this.buttons = buttons;
        this.tooltipText = tooltipText;
    }

    public String getFailureMessage() {

        return null;
    }

    public boolean test() throws Exception {

        for (SWTBotToolbarButton toolbarButton : buttons) {
            if (toolbarButton.getToolTipText().matches(tooltipText)) {
                return true;
            }
        }
        return false;
    }
}
