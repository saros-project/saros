package de.fu_berlin.inf.dpp.stf.conditions;

import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;

import de.fu_berlin.inf.dpp.stf.swtbot.SarosSWTWorkbenchBot;

public class ExistNoInvitationProgress extends DefaultCondition {

    private SarosSWTWorkbenchBot bot;

    ExistNoInvitationProgress(SarosSWTWorkbenchBot bot) {
        this.bot = bot;
    }

    public String getFailureMessage() {
        return null;
    }

    public boolean test() throws Exception {
        SWTBotView view = bot.viewByTitle("Progress");
        view.setFocus();
        view.toolbarButton("Remove All Finished Operations").click();
        SWTBot bot = view.bot();
        if (bot.text().getText().matches("No operations to display.*"))
            return true;

        return false;
    }
}
