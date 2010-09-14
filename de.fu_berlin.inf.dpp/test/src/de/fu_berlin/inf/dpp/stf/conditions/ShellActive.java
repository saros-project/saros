package de.fu_berlin.inf.dpp.stf.conditions;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;

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
        try {
            SWTBotShell shell = bot.shell(title);
            return shell.isActive();
        } catch (WidgetNotFoundException e) {
            return false;
        }
    }
}
