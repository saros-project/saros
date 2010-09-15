package de.fu_berlin.inf.dpp.stf.conditions;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;

public class IsShellClosed extends DefaultCondition {

    private SWTWorkbenchBot bot;
    private String title;

    IsShellClosed(SWTWorkbenchBot bot, String title) {
        this.title = title;
        this.bot = bot;
    }

    public String getFailureMessage() {

        return null;
    }

    public boolean test() throws Exception {
        try {
            bot.shell(title);
            return false;
        } catch (WidgetNotFoundException e) {
            return true;
        }
    }
}