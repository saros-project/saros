package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.conditions;

import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.views.sarosViews.SarosView;

public class ExistsChatMessage extends DefaultCondition {

    private String jid;
    private String message;
    private SarosView sarosView;

    ExistsChatMessage(SarosView sarosView, String jid, String message) {
        this.sarosView = sarosView;
        this.jid = jid;
        this.message = message;
    }

    public String getFailureMessage() {
        return null;
    }

    public boolean test() throws Exception {
        return sarosView.compareChatMessage(jid, message);
    }
}
