package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.conditions;

import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;

import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.ExChatViewObjectImp;

public class IsChatMessageExist extends DefaultCondition {

    private String jid;
    private String message;
    private ExChatViewObjectImp chatV;

    IsChatMessageExist(ExChatViewObjectImp chatV, String jid, String message) {
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
