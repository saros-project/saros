package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.conditions;

import java.util.List;

import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.noGUI.ExStateObject;

public class ExistNoParticipants extends DefaultCondition {

    private List<JID> jids;
    private ExStateObject state;

    ExistNoParticipants(ExStateObject state, List<JID> jids) {
        this.jids = jids;
        this.state = state;
    }

    public String getFailureMessage() {

        return null;
    }

    public boolean test() throws Exception {
        for (JID jid : jids) {
            if (state.isParticipant(jid))
                return false;
        }
        return true;
    }
}
