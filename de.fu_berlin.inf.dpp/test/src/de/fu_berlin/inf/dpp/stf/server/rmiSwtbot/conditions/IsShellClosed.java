package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.conditions;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;

public class IsShellClosed extends DefaultCondition {

    private SWTWorkbenchBot bot;
    private String title;

    IsShellClosed(SWTWorkbenchBot bot, String title) {
        this.title = title;
        this.bot = bot;
    }

    public String getFailureMessage() {

        return "Can't find the shell " + title;
    }

    public boolean test() throws Exception {
        final SWTBotShell[] shells = bot.shells();
        for (SWTBotShell shell : shells) {
            if (shell.getText().equals(title))
                return false;
        }
        return true;
    }
    // public boolean test() throws Exception {
    // try {
    // bot.shell(title);
    // return false;
    // } catch (WidgetNotFoundException e) {
    // return true;
    // }
    // }
}