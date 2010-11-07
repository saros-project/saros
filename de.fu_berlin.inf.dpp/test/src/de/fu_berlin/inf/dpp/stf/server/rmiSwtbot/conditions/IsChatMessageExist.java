package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.conditions;

import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;

import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.ISarosRmiSWTWorkbenchBot;

public class IsChatMessageExist extends DefaultCondition {

    private String jid;
    private String message;
    private ISarosRmiSWTWorkbenchBot bot;

    IsChatMessageExist(ISarosRmiSWTWorkbenchBot bot, String jid, String message) {
        this.bot = bot;
        this.jid = jid;
        this.message = message;
    }

    public String getFailureMessage() {
        return null;
    }

    public boolean test() throws Exception {
        return bot.compareChatMessage(jid, message);
    }
}
