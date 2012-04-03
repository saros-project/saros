package de.fu_berlin.inf.dpp.activities.serializable;

import org.joda.time.DateTime;
import org.joda.time.Duration;

import de.fu_berlin.inf.dpp.User;
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

    public PingPongActivityDataObject(JID source) {
        super(source);
    }

    public PingPongActivityDataObject(JID source, JID initiator,
        DateTime departureTime) {
        super(source);
        this.initiator = initiator;
        this.departureTime = departureTime;
    }

    public Duration getRoundtripTime() {
        return new Duration(departureTime, new DateTime());
    }

    public JID getInitiator() {
        return initiator;
    }

    public DateTime getDepartureTime() {
        return departureTime;
    }

    public static PingPongActivityDataObject create(User localUser) {
        return new PingPongActivityDataObject(localUser.getJID(), localUser
            .getJID(), new DateTime());
    }

    public IActivityDataObject createPong(User localUser) {
        return new PingPongActivityDataObject(localUser.getJID(),
            getInitiator(), this.getDepartureTime());
    }

    @Override
    public String toString() {
        return (getSource().equals(getInitiator()) ? "Ping" : "Pong")
            + "Activity(initiator=" + getInitiator() + ",departed="
            + departureTime.toString("HH:mm:ss,SSS") + ")";
    }

    public IActivity getActivity(ISarosSession sarosSession) {
        return new PingPongActivity(sarosSession.getUser(source),
            sarosSession.getUser(initiator), departureTime);
    }
}
