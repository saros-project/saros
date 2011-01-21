package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.conditions;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;

public class IsShellActive extends DefaultCondition {

    private String title;
    private SWTWorkbenchBot bot;

    IsShellActive(SWTWorkbenchBot bot, String title) {
        this.title = title;
        this.bot = bot;
    }

    public String getFailureMessage() {
        return "Shell " + title + " not found.";
    }

    public boolean test() throws Exception {
        // In conditions, only use methods that return immediately (no waiting)
        return bot.activeShell().getText().equals(title);
    }
}
