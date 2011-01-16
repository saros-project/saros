package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.conditions;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;

public class isViewActive extends DefaultCondition {

    private SWTWorkbenchBot bot;
    private String name;

    isViewActive(SWTWorkbenchBot bot, String name) {

        this.name = name;
        this.bot = bot;
    }

    public String getFailureMessage() {

        return null;
    }

    public boolean test() throws Exception {
        return bot.activeView().getTitle().equals(name);
    }

}
