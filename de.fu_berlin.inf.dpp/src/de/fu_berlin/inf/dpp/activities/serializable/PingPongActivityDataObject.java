package de.fu_berlin.inf.dpp.activities.serializable;

import org.joda.time.DateTime;

import de.fu_berlin.inf.dpp.activities.business.IActivity;
import de.fu_berlin.inf.dpp.activities.business.PingPongActivity;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.project.ISarosSession;

/**
 * TODO No XStream annotations. How does this look like when serialized!?
 * Especially the DateTime instance.
 */
public class PingPongActivityDataObject extends AbstractActivityDataObject {

    protected JID initiator;
    protected DateTime departureTime;

    public PingPongActivityDataObject(JID source, JID initiator,
        DateTime departureTime) {
        super(source);
        this.initiator = initiator;
        this.departureTime = departureTime;
    }

    @Override
    public String toString() {
        return (getSource().equals(initiator) ? "Ping" : "Pong")
            + "Activity(initiator=" + initiator + ",departed="
            + departureTime.toString("HH:mm:ss,SSS") + ")";
    }

    @Override
    public IActivity getActivity(ISarosSession sarosSession) {
        return new PingPongActivity(sarosSession.getUser(source),
            sarosSession.getUser(initiator), departureTime);
    }
}
