package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.conditions;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;

public class isMenuEnabled extends DefaultCondition {

    private SWTWorkbenchBot bot;
    private String[] labels;

    isMenuEnabled(SWTWorkbenchBot bot, String... labels) {

        this.bot = bot;
        this.labels = labels;

    }

    public String getFailureMessage() {

        return null;
    }

    public boolean test() throws Exception {
        try {
            SWTBotMenu menu = null;
            for (String label : labels) {
                SWTBotMenu menu2 = menu;
                if (menu2 == null)
                    bot.menu(label);
                else
                    menu2.menu(label);
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}
