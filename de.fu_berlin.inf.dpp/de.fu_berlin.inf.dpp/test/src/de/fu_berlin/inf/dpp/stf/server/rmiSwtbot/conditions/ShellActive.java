package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.conditions;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;

public class ShellActive extends DefaultCondition {

    private String title;
    private SWTWorkbenchBot bot;

    ShellActive(SWTWorkbenchBot bot, String title) {
        this.title = title;
        this.bot = bot;
    }

    public String getFailureMessage() {
        return null;
    }

    public boolean test() throws Exception {
        // In conditions, only use methods that return immediately (no waiting)
        return bot.activeShell().getText().equals(title);
    }
}
