package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.conditions;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;

public class isViewActive extends DefaultCondition {

    private SWTWorkbenchBot bot1;
    private String name;

    isViewActive(SWTWorkbenchBot bot, String name) {

        this.name = name;
        this.bot1 = bot;
    }

    public String getFailureMessage() {

        return null;
    }

    public boolean test() throws Exception {
        return bot1.activeView().getTitle().equals(name);
        // try {
        // return bot1.viewByTitle(name).isActive();
        //
        // } catch (WidgetNotFoundException e) {
        // return false;
        // }

    }

}
