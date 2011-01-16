package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.conditions;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;

public class IsShellOpen extends DefaultCondition {
    private SWTWorkbenchBot bot;
    private String title;

    IsShellOpen(SWTWorkbenchBot bot, String title) {
        this.title = title;
        this.bot = bot;
    }

    public String getFailureMessage() {
        return "Can't find the shell " + title;
    }

    public boolean test() throws Exception {
        SWTBotShell[] shells = bot.shells();
        for (SWTBotShell shell : shells) {
            if (shell.getText().equals(title)) {
                return true;
            }
        }
        return false;
    }
}
