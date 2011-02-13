package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.conditions;

import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.views.sarosViews.ChatViewImp;

public class ExistsChatMessage extends DefaultCondition {

    private String jid;
    private String message;
    private ChatViewImp chatV;

    ExistsChatMessage(ChatViewImp chatV, String jid, String message) {
        this.chatV = chatV;
        this.jid = jid;
        this.message = message;
    }

    public String getFailureMessage() {
        return null;
    }

    public boolean test() throws Exception {
        return chatV.compareChatMessage(jid, message);
    }
}
