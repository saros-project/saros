package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.conditions;

import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;

import de.fu_berlin.inf.dpp.stf.server.sarosSWTBot.SarosSWTBot;

public class ExistNoInvitationProgress extends DefaultCondition {

    private SarosSWTBot bot;

    ExistNoInvitationProgress(SarosSWTBot bot) {
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
        try {
            bot.toolbarButton();
            return false;
        } catch (WidgetNotFoundException e) {
            return true;
        }

    }
}
