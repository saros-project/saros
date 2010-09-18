package de.fu_berlin.inf.dpp.stf.conditions;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;

public class hasContactWith {

    private SWTWorkbenchBot bot;

    hasContactWith(SWTWorkbenchBot bot) {

        this.bot = bot;
    }

    public String getFailureMessage() {

        return null;
    }

    public boolean test() throws Exception {
        // if (!bot.isConnectedByXmppGuiCheck())
        // return false;
        // SWTBotTreeItem contact_added = selectBuddy(contact);
        // return contact_added != null &&
        // contact_added.getText().equals(contact);
        return true;
    }
}
