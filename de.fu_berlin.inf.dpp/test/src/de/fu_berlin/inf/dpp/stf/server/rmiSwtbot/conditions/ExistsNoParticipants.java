package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.conditions;

import java.util.List;

import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.noGUI.ExStateObject;

public class ExistsNoParticipants extends DefaultCondition {

    private List<JID> jidsOfAllParticipants;
    private ExStateObject state;

    ExistsNoParticipants(ExStateObject state, List<JID> jidsOfAllParticipants) {
        this.jidsOfAllParticipants = jidsOfAllParticipants;
        this.state = state;
    }

    public String getFailureMessage() {

        return "Hi guy, are you sure, all the participants have already leave the session? "
            + " It seems that there are still some participants in the session";
    }

    public boolean test() throws Exception {
        for (JID jid : jidsOfAllParticipants) {
            if (state.isParticipant(jid))
                return false;
        }
        return true;
    }
}
